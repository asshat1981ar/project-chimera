package com.chimera.domain.usecase

import com.chimera.data.repository.CharacterRepository
import com.chimera.data.repository.JournalRepository
import com.chimera.database.entity.JournalEntryEntity
import javax.inject.Inject
import kotlin.math.abs

class ApplyRelationshipDeltaUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val journalRepository: JournalRepository
) {
    /**
     * Apply a relationship delta and optionally create a journal entry
     * if the change is significant enough.
     */
    suspend operator fun invoke(
        slotId: Long,
        characterId: String,
        characterName: String,
        delta: Float,
        context: String = ""
    ) {
        characterRepository.adjustDisposition(characterId, delta)

        if (abs(delta) >= JOURNAL_THRESHOLD) {
            val direction = if (delta > 0) "warmed to" else "grown colder toward"
            journalRepository.insertEntry(
                JournalEntryEntity(
                    saveSlotId = slotId,
                    title = "$characterName's Regard",
                    body = "$characterName has $direction you${if (context.isNotBlank()) " after $context" else ""}.",
                    category = "companion",
                    characterId = characterId
                )
            )
        }
    }

    companion object {
        const val JOURNAL_THRESHOLD = 0.1f
    }
}
