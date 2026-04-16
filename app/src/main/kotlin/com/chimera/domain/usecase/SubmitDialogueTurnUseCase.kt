package com.chimera.domain.usecase

import com.chimera.ai.DialogueOrchestrator
import com.chimera.data.repository.CharacterRepository
import com.chimera.data.repository.DialogueRepository
import com.chimera.database.entity.MemoryShardEntity
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import javax.inject.Inject

data class TurnOutcome(
    val result: DialogueTurnResult,
    val isFallback: Boolean,
    val updatedDisposition: Float?
)

class SubmitDialogueTurnUseCase @Inject constructor(
    private val orchestrator: DialogueOrchestrator,
    private val dialogueRepository: DialogueRepository,
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(
        slotId: Long,
        sceneId: String,
        contract: SceneContract,
        playerInput: PlayerInput,
        characterState: CharacterState,
        recentMemories: List<MemoryShard>,
        turnHistory: List<DialogueTurnResult>
    ): TurnOutcome {
        // Persist player turn
        dialogueRepository.persistTurn(slotId, sceneId, "player", playerInput.text, "")

        // Generate NPC response
        val result = orchestrator.generateTurn(
            contract, playerInput, characterState, recentMemories, turnHistory
        )

        // Persist NPC turn
        dialogueRepository.persistTurn(slotId, sceneId, contract.npcId, result.npcLine, result.emotion)

        // Batch insert memory candidates
        if (result.memoryCandidates.isNotEmpty()) {
            val shards = result.memoryCandidates.map { summary ->
                MemoryShardEntity(
                    saveSlotId = slotId,
                    sceneId = sceneId,
                    characterId = contract.npcId,
                    summary = summary,
                    importanceScore = 0.6f
                )
            }
            dialogueRepository.insertMemoryShards(shards)
        }

        // Apply relationship delta
        var updatedDisposition: Float? = null
        if (result.relationshipDelta != 0f) {
            characterRepository.adjustDisposition(contract.npcId, result.relationshipDelta)
            updatedDisposition = characterRepository.getCharacterState(contract.npcId)?.dispositionToPlayer
        }

        // Handle companion recruitment
        if (result.flags.contains("recruit_companion")) {
            characterRepository.promoteToCompanion(contract.npcId)
        }

        return TurnOutcome(
            result = result,
            isFallback = orchestrator.isFallbackActive,
            updatedDisposition = updatedDisposition
        )
    }
}
