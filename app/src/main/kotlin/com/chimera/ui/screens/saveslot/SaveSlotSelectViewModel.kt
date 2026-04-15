package com.chimera.ui.screens.saveslot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.SaveSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveSlotSelectViewModel @Inject constructor(
    private val saveSlotDao: SaveSlotDao,
    private val characterDao: CharacterDao,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    val saveSlots: StateFlow<List<SaveSlot>> = saveSlotDao.observeAll()
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createNewGame(slotIndex: Int, playerName: String, onCreated: (Long) -> Unit) {
        if (playerName.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = saveSlotDao.getByIndex(slotIndex)
            val slotId = saveSlotDao.upsert(
                SaveSlotEntity(
                    id = existing?.id ?: 0,
                    slotIndex = slotIndex,
                    playerName = playerName.trim(),
                    chapterTag = "prologue",
                    playtimeSeconds = 0,
                    lastPlayedAt = now,
                    createdAt = now,
                    isEmpty = false
                )
            )
            // Seed the player character
            characterDao.upsert(
                CharacterEntity(
                    id = "player_${slotId}",
                    saveSlotId = slotId,
                    name = playerName.trim(),
                    role = "PROTAGONIST",
                    isPlayerCharacter = true
                )
            )
            gameSessionManager.setActiveSlot(slotId)
            onCreated(slotId)
        }
    }

    fun selectSlot(slotId: Long, onSelected: (Long) -> Unit) {
        viewModelScope.launch {
            val slot = saveSlotDao.getById(slotId) ?: return@launch
            if (slot.isEmpty) return@launch
            saveSlotDao.upsert(slot.copy(lastPlayedAt = System.currentTimeMillis()))
            gameSessionManager.setActiveSlot(slotId)
            onSelected(slotId)
        }
    }

    fun deleteSave(slotId: Long) {
        viewModelScope.launch {
            val slot = saveSlotDao.getById(slotId) ?: return@launch
            // Reset slot to empty rather than deleting (keep the 3-slot structure)
            saveSlotDao.upsert(
                slot.copy(
                    playerName = "",
                    chapterTag = "prologue",
                    playtimeSeconds = 0,
                    isEmpty = true
                )
            )
            characterDao.deleteBySlot(slotId)
        }
    }
}
