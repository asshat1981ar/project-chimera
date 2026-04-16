package com.chimera.domain.usecase

import com.chimera.ai.DialogueOrchestrator
import com.chimera.data.SceneLoader
import com.chimera.data.repository.CharacterRepository
import com.chimera.data.repository.DialogueRepository
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import javax.inject.Inject

data class SceneInitResult(
    val contract: SceneContract,
    val openingTurn: DialogueTurnResult,
    val intents: List<String>,
    val characterState: CharacterState,
    val recentMemories: List<MemoryShard>,
    val sceneInstanceId: Long,
    val isFallback: Boolean
)

class StartSceneUseCase @Inject constructor(
    private val sceneLoader: SceneLoader,
    private val dialogueRepository: DialogueRepository,
    private val characterRepository: CharacterRepository,
    private val orchestrator: DialogueOrchestrator
) {
    suspend operator fun invoke(slotId: Long, sceneId: String): SceneInitResult {
        val contract = sceneLoader.getScene(sceneId) ?: SceneContract(
            sceneId = sceneId,
            sceneTitle = "Unknown Scene",
            npcId = "unknown",
            npcName = "Stranger",
            setting = "an unfamiliar place"
        )

        val instanceId = dialogueRepository.createSceneInstance(slotId, sceneId, contract.npcId)

        val charState = characterRepository.getCharacterState(contract.npcId)
            ?: CharacterState(characterId = contract.npcId, saveSlotId = slotId)

        val memories = dialogueRepository.getRecentMemories(slotId, contract.npcId, 5)

        val openingInput = PlayerInput(text = "[Scene begins]", isQuickIntent = true)
        val openingTurn = orchestrator.generateTurn(
            contract, openingInput, charState, memories, emptyList()
        )

        dialogueRepository.persistTurn(slotId, sceneId, contract.npcId, openingTurn.npcLine, openingTurn.emotion)

        val intents = orchestrator.generateIntents(contract, charState, listOf(openingTurn))

        return SceneInitResult(
            contract = contract,
            openingTurn = openingTurn,
            intents = intents,
            characterState = charState,
            recentMemories = memories,
            sceneInstanceId = instanceId,
            isFallback = orchestrator.isFallbackActive
        )
    }
}
