package com.chimera.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.data.MultiActMapNodeLoader
import com.chimera.database.dao.CharacterStateDao
import kotlinx.serialization.json.Json
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.dao.RumorPacketDao
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.database.entity.FactionStateEntity
import com.chimera.domain.usecase.ObserveMapQuestMarkersUseCase
import com.chimera.model.MapNode
import com.chimera.model.MapQuestMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


data class MapUiState(
    val nodes: List<MapNode> = emptyList(),
    val factions: List<FactionStateEntity> = emptyList(),
    val selectedNode: MapNode? = null,
    val questMarkers: List<MapQuestMarker> = emptyList()
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val sceneInstanceDao: SceneInstanceDao,
    private val rumorPacketDao: RumorPacketDao,
    private val factionStateDao: FactionStateDao,
    private val characterStateDao: CharacterStateDao,
    private val mapNodeLoader: MultiActMapNodeLoader,
    private val observeMapQuestMarkers: ObserveMapQuestMarkersUseCase,
    gameSessionManager: GameSessionManager
) : ViewModel() {

    private val _selectedNode = MutableStateFlow<MapNode?>(null)
    private val baseNodes: List<MapNode> by lazy { mapNodeLoader.loadNodesSync() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MapUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(MapUiState())
            combine(
                rumorPacketDao.observeAll(slotId),
                factionStateDao.observeAll(slotId),
                _selectedNode
            ) { rumors, factions, selected ->
                val completedScenes = sceneInstanceDao.getBySlot(slotId)
                    .filter { it.status == "completed" }
                    .map { it.sceneId }
                    .toSet()

                val rumorsByLocation = rumors.groupBy { it.locationId }
                    .mapValues { (_, list) -> list.count { it.heatLevel > 0.3f } }

                val json = Json { ignoreUnknownKeys = true }
                val factionByLocation = factions.flatMap { faction ->
                    try {
                        val locations = json.decodeFromString<List<String>>(faction.controlledLocationsJson)
                        locations.map { it to faction.factionName }
                    } catch (_: Exception) {
                        emptyList()
                    }
                }.toMap()

                val nodes = baseNodes.map { node ->
                    val isCompleted = node.sceneId in completedScenes
                    val isUnlocked = node.isUnlocked || node.connectedTo.any { connId ->
                        baseNodes.find { it.id == connId }?.let { conn ->
                            conn.isUnlocked || conn.sceneId in completedScenes
                        } ?: false
                    }
                    val isRevealed = node.isUnlocked ||
                        isCompleted ||
                        node.connectedTo.any { connId ->
                            val neighbor = baseNodes.find { it.id == connId }
                            neighbor?.sceneId in completedScenes || neighbor?.isUnlocked == true
                        }
                    node.copy(
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        isRevealed = isRevealed,
                        rumorCount = rumorsByLocation[node.id] ?: 0,
                        faction = factionByLocation[node.id]
                    )
                }

                val revealedNodes = nodes.filter { it.isRevealed }
                Triple(revealedNodes, factions, selected)
            }.flatMapLatest { (revealedNodes, factions, selected) ->
                val nodeId = selected?.id
                val markersFlow = if (nodeId != null) {
                    observeMapQuestMarkers(nodeId)
                } else {
                    flowOf(emptyList())
                }
                markersFlow.map { markers ->
                    MapUiState(
                        nodes = revealedNodes,
                        factions = factions,
                        selectedNode = selected,
                        questMarkers = markers
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapUiState())

    fun selectNode(node: MapNode) {
        _selectedNode.value = node
    }

    fun clearSelection() {
        _selectedNode.value = null
    }

    /**
     * Returns (xFraction, yFraction) pairs for nodes that are NOT yet revealed
     * but are connected to at least one revealed node in [visibleNodes].
     */
    fun fogAdjacentPositions(visibleNodes: List<MapNode>): List<Pair<Float, Float>> {
        val visibleIds = visibleNodes.map { it.id }.toSet()
        return baseNodes
            .filter { candidate ->
                candidate.id !in visibleIds &&
                candidate.connectedTo.any { it in visibleIds }
            }
            .map { it.xFraction to it.yFraction }
    }
}
