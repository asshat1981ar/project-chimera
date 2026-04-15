package com.chimera.ui.screens.saveslot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.data.NpcSeeder
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.SaveSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveSlotSelectViewModel @Inject constructor(
    private val saveSlotDao: SaveSlotDao,
    private val characterDao: CharacterDao,
    private val npcSeeder: NpcSeeder,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val saveSlots: StateFlow<List<SaveSlot>> = saveSlotDao.observeAll()
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createNewGame(slotIndex: Int, playerName: String, onCreated: (Long) -> Unit) {
        if (playerName.isBlank()) return
        viewModelScope.launch {
            try {
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
                characterDao.upsert(
                    CharacterEntity(
                        id = "player_${slotId}",
                        saveSlotId = slotId,
                        name = playerName.trim(),
                        role = "PROTAGONIST",
                        isPlayerCharacter = true
                    )
                )
                // Seed NPCs from assets for this save slot
                npcSeeder.seedNpcsForSlot(slotId)
                gameSessionManager.setActiveSlot(slotId)
                onCreated(slotId)
            } catch (e: Exception) {
                Log.e("SaveSlotVM", "Failed to create new game", e)
                _error.value = "Failed to create save"
            }
        }
    }

    fun selectSlot(slotId: Long, onSelected: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val slot = saveSlotDao.getById(slotId) ?: return@launch
                if (slot.isEmpty) return@launch
                saveSlotDao.upsert(slot.copy(lastPlayedAt = System.currentTimeMillis()))
                gameSessionManager.setActiveSlot(slotId)
                onSelected(slotId)
            } catch (e: Exception) {
                Log.e("SaveSlotVM", "Failed to select slot", e)
                _error.value = "Failed to load save"
            }
        }
    }

    fun deleteSave(slotId: Long) {
        viewModelScope.launch {
            try {
                val slot = saveSlotDao.getById(slotId) ?: return@launch
                saveSlotDao.upsert(
                    slot.copy(
                        playerName = "",
                        chapterTag = "prologue",
                        playtimeSeconds = 0,
                        isEmpty = true
                    )
                )
                characterDao.deleteBySlot(slotId)
            } catch (e: Exception) {
                Log.e("SaveSlotVM", "Failed to delete save", e)
                _error.value = "Failed to delete save"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
