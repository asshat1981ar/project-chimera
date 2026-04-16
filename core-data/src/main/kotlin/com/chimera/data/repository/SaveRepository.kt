package com.chimera.data.repository

import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.SaveSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveRepository @Inject constructor(
    private val saveSlotDao: SaveSlotDao,
    private val characterDao: CharacterDao
) {
    fun observeAllSlots(): Flow<List<SaveSlot>> =
        saveSlotDao.observeAll().map { slots -> slots.map { it.toModel() } }

    suspend fun getSlot(id: Long): SaveSlot? =
        saveSlotDao.getById(id)?.toModel()

    suspend fun createSlot(slotIndex: Int, playerName: String): Long {
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
                id = "player_$slotId",
                saveSlotId = slotId,
                name = playerName.trim(),
                role = "PROTAGONIST",
                isPlayerCharacter = true
            )
        )
        return slotId
    }

    suspend fun deleteSlot(slotId: Long) {
        val slot = saveSlotDao.getById(slotId) ?: return
        saveSlotDao.upsert(
            slot.copy(playerName = "", chapterTag = "prologue", playtimeSeconds = 0, isEmpty = true)
        )
        characterDao.deleteBySlot(slotId)
    }

    suspend fun updateLastPlayed(slotId: Long) {
        val slot = saveSlotDao.getById(slotId) ?: return
        saveSlotDao.upsert(slot.copy(lastPlayedAt = System.currentTimeMillis()))
    }

    suspend fun addPlaytime(slotId: Long, seconds: Long) {
        val slot = saveSlotDao.getById(slotId) ?: return
        saveSlotDao.upsert(
            slot.copy(
                playtimeSeconds = slot.playtimeSeconds + seconds,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }
}
