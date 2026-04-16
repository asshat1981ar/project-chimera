package com.chimera.feature.party

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.MemoryShardDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartyMember(
    val character: CharacterEntity,
    val state: CharacterStateEntity?,
    val recentMemories: List<MemoryShardEntity> = emptyList()
)

data class PartyUiState(
    val members: List<PartyMember> = emptyList(),
    val selectedMember: PartyMember? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val memoryShardDao: MemoryShardDao,
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
                _selectedId,
                _memoryCache
            ) { characters, states, selectedId, memoryCache ->
                val stateMap = states.associateBy { it.characterId }
                val nonPlayerChars = characters.filter { !it.isPlayerCharacter }
                val members = nonPlayerChars.map { char ->
                    PartyMember(
                        character = char,
                        state = stateMap[char.id],
                        recentMemories = memoryCache[char.id] ?: emptyList()
                    )
                }
                val selected = selectedId?.let { id ->
                    members.find { it.character.id == id }
                }
                PartyUiState(members = members, selectedMember = selected, isLoading = false)
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
        viewModelScope.launch {
            val slotId = gameSessionManager.activeSlotId.value ?: return@launch
            val memories = memoryShardDao.getTopMemories(slotId, characterId, 10)
            _memoryCache.value = _memoryCache.value + (characterId to memories)
        }
    }
}
