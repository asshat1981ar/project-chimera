package com.chimera.domain.usecase

import com.chimera.data.repository.JournalRepository
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.model.DialogueTurnResult
import com.chimera.model.SceneContract
import javax.inject.Inject

class GenerateSceneSummaryUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(
        slotId: Long,
        contract: SceneContract,
        turnResults: List<DialogueTurnResult>
    ) {
        val turnCount = turnResults.size
        val summary = if (turnCount > 1) {
            "Spoke with ${contract.npcName} at ${contract.setting}. " +
            "The conversation spanned $turnCount exchanges."
        } else {
            "A brief encounter with ${contract.npcName}."
        }

        // Story entry
        journalRepository.insertEntry(
            JournalEntryEntity(
                saveSlotId = slotId,
                title = contract.sceneTitle,
                body = summary,
                category = "story",
                sceneId = contract.sceneId,
                characterId = contract.npcId
            )
        )

        // Companion entry on recruitment
        if (turnResults.any { it.flags.contains("recruit_companion") }) {
            journalRepository.insertEntry(
                JournalEntryEntity(
                    saveSlotId = slotId,
                    title = "${contract.npcName} Joins",
                    body = "${contract.npcName} has agreed to join your cause.",
                    category = "companion",
                    characterId = contract.npcId
                )
            )
        }
    }
}
