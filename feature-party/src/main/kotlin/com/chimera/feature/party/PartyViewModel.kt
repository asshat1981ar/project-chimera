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
    val recentMemories: List<MemoryShardEntity> = emptyList()
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
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val _selectedId = MutableStateFlow<String?>(null)
    private val _memoryCache = MutableStateFlow<Map<String, List<MemoryShardEntity>>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PartyUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(PartyUiState(isLoading = false))
            combine(
                characterDao.observeBySlot(slotId),
                characterStateDao.observeBySlot(slotId),
                factionStateDao.observeAll(slotId),
                _selectedId,
                _memoryCache
            ) { characters, states, factions, selectedId, memoryCache ->
                val stateMap = states.associateBy { it.characterId }
                val members = characters
                    .filter { !it.isPlayerCharacter }
                    .map { char ->
                        PartyMember(
                            character = char,
                            state = stateMap[char.id],
                            recentMemories = memoryCache[char.id] ?: emptyList()
                        )
                    }
                val selected = selectedId?.let { id -> members.find { it.character.id == id } }
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
}
