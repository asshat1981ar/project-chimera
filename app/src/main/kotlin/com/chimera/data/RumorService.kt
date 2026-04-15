package com.chimera.data

import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.RumorPacketDao
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.database.entity.RumorPacketEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages rumor lifecycle: creation from dialogue, heat decay on day passage,
 * and journal entry generation for newly discovered rumors.
 */
@Singleton
class RumorService @Inject constructor(
    private val rumorPacketDao: RumorPacketDao,
    private val journalEntryDao: JournalEntryDao
) {
    /**
     * Decay all rumor heat levels for a save slot. Called on day advancement
     * (e.g., after night events resolve).
     */
    suspend fun advanceDay(slotId: Long) {
        rumorPacketDao.decayAll(slotId, 0.9f)
    }

    /**
     * Create a new rumor from a dialogue scene and generate a journal entry for it.
     */
    suspend fun discoverRumor(
        slotId: Long,
        title: String,
        content: String,
        locationId: String,
        sourceNpc: String?
    ) {
        rumorPacketDao.insert(
            RumorPacketEntity(
                saveSlotId = slotId,
                title = title,
                content = content,
                locationId = locationId,
                sourceNpc = sourceNpc,
                heatLevel = 0.8f
            )
        )
        journalEntryDao.insert(
            JournalEntryEntity(
                saveSlotId = slotId,
                title = "Rumor: $title",
                body = content,
                category = "rumor"
            )
        )
    }
}
