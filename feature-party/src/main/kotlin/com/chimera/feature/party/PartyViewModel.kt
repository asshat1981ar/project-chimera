package com.chimera.feature.party

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.dao.MemoryShardDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.FactionStateEntity
import com.chimera.database.entity.MemoryShardEntity
import com.chimera.domain.usecase.GetRelationshipDynamicsUseCase
import com.chimera.domain.usecase.RelationshipDynamics
import com.chimera.feature.party.DispositionSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartyMember(
    val character: CharacterEntity,
    val state: CharacterStateEntity?,
    val recentMemories: List<MemoryShardEntity> = emptyList(),
    val dispositionHistory: List<DispositionSnapshot> = emptyList(),
    val relationshipDynamics: RelationshipDynamics? = null
)

data class PartyUiState(
    val members: List<PartyMember> = emptyList(),
    val factions: List<FactionStateEntity> = emptyList(),
    val selectedMember: PartyMember? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val memoryShardDao: MemoryShardDao,
    private val factionStateDao: FactionStateDao,
    private val gameSessionManager: GameSessionManager,
    private val getRelationshipDynamics: GetRelationshipDynamicsUseCase
) : ViewModel() {

    private val _selectedId = MutableStateFlow<String?>(null)
    private val _memoryCache = MutableStateFlow<Map<String, List<MemoryShardEntity>>>(emptyMap())
    private val _dispositionHistory = MutableStateFlow<Map<String, List<DispositionSnapshot>>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PartyUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(PartyUiState(isLoading = false))
            combine(
                characterDao.observeBySlot(slotId),
                characterStateDao.observeBySlot(slotId),
                factionStateDao.observeAll(slotId),
                _selectedId,
                _memoryCache,
                _dispositionHistory
            ) { args ->
                val characters = args[0] as List<CharacterEntity>
                val states = args[1] as List<CharacterStateEntity>
                val factions = args[2] as List<FactionStateEntity>
                val selectedId = args[3] as String?
                val memoryCache = args[4] as Map<String, List<MemoryShardEntity>>
                val dispositionHistory = args[5] as Map<String, List<DispositionSnapshot>>

                val stateMap = states.associateBy { it.characterId }
                val members = characters
                    .filter { character -> !character.isPlayerCharacter }
                    .map { char ->
                        PartyMember(
                            character = char,
                            state = stateMap[char.id],
                            recentMemories = memoryCache[char.id] ?: emptyList(),
                            dispositionHistory = dispositionHistory[char.id] ?: emptyList(),
                            relationshipDynamics = getRelationshipDynamics(char.id)
                        )
                    }
                val selected = selectedId?.let { memberId -> members.find { member -> member.character.id == memberId } }
                PartyUiState(
                    members = members,
                    factions = factions,
                    selectedMember = selected,
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PartyUiState())

    fun selectMember(characterId: String) {
        _selectedId.value = characterId
        loadMemories(characterId)
    }

    fun clearSelection() {
        _selectedId.value = null
    }

    private fun loadMemories(characterId: String) {
        val slotId = gameSessionManager.activeSlotId.value ?: return
        viewModelScope.launch {
            val memories = memoryShardDao.getTopMemories(slotId, characterId, limit = 10)
            _memoryCache.update { cache -> cache + (characterId to memories) }
        }
    }

    fun recordDispositionSnapshot(characterId: String) {
        viewModelScope.launch {
            val slotId = gameSessionManager.activeSlotId.value ?: return@launch
            val state = characterStateDao.getByCharacterId(characterId) ?: return@launch
            val currentDisposition = state.dispositionToPlayer

            _dispositionHistory.update { history ->
                val existing = history[characterId] ?: emptyList()
                val snapshot = DispositionSnapshot(
                    disposition = currentDisposition,
                    delta = if (existing.isNotEmpty()) currentDisposition - existing.last().disposition else 0f
                )
                val updated = (existing + snapshot).takeLast(10)
                history + (characterId to updated)
            }
        }
    }
}
