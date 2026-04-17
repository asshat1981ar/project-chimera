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
import com.chimera.model.MapNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


data class MapUiState(
    val nodes: List<MapNode> = emptyList(),
    val factions: List<FactionStateEntity> = emptyList(),
    val selectedNode: MapNode? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val sceneInstanceDao: SceneInstanceDao,
    private val rumorPacketDao: RumorPacketDao,
    private val factionStateDao: FactionStateDao,
    private val characterStateDao: CharacterStateDao,
    private val mapNodeLoader: MultiActMapNodeLoader,
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

                // Load character states for relationship-based unlock checks
                val charStates = characterStateDao.observeBySlot(slotId)
                val dispositions = mutableMapOf<String, Float>()
                // Build a simple disposition lookup (using cached data from combine)

                val nodes = baseNodes.map { node ->
                    val isCompleted = node.sceneId in completedScenes
                    // Unlock if: node is default unlocked, OR a connected node is completed
                    val isUnlocked = node.isUnlocked || node.connectedTo.any { connId ->
                        baseNodes.find { it.id == connId }?.let { conn ->
                            conn.isUnlocked || conn.sceneId in completedScenes
                        } ?: false
                    }
                    // Fog-of-war reveal rules (computed, not persisted):
                    //  1. Node is the act-start node (isUnlocked=true in JSON) → always revealed
                    //  2. Node is completed → revealed
                    //  3. Any connected neighbor is completed → revealed
                    //  4. All other nodes → hidden (isRevealed=false)
                    val isRevealed = node.isUnlocked ||   // act-start nodes are pre-unlocked in JSON
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

                // Fog filter: only pass revealed nodes to the UI
                // Fog placeholder nodes are rendered by MapScreen from connectedTo refs
                val revealedNodes = nodes.filter { it.isRevealed }

                MapUiState(
                    nodes = revealedNodes,
                    factions = factions,
                    selectedNode = selected
                )
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
     * Used by MapScreen to render fog-of-war placeholder dots at the right positions.
     * Computed from [baseNodes] so positions are always available regardless of reveal state.
     */
    fun fogAdjacentPositions(visibleNodes: List<MapNode>): List<Pair<Float, Float>> {
        val visibleIds = visibleNodes.map { it.id }.toSet()
        return baseNodes
            .filter { candidate ->
                // Not already visible
                candidate.id !in visibleIds &&
                // Connected to at least one visible node
                candidate.connectedTo.any { it in visibleIds }
            }
            .map { it.xFraction to it.yFraction }
    }
}
