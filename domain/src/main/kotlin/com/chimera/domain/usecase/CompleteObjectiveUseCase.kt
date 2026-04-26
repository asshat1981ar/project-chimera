package com.chimera.domain.usecase

import com.chimera.data.repository.QuestRepository
import javax.inject.Inject

/**
 * Complete a quest objective by its scene, NPC, or map node trigger.
 */
class CompleteObjectiveUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    suspend fun byObjectiveId(questId: Long, objectiveId: Long) {
        questRepository.completeObjective(questId, objectiveId)
    }

    suspend fun byScene(questId: Long, sceneId: String) {
        questRepository.completeObjectiveByScene(questId, sceneId)
    }

    suspend fun byNpc(questId: Long, npcId: String) {
        questRepository.completeObjectiveByNpc(questId, npcId)
    }

    suspend fun byMapNode(questId: Long, nodeId: String) {
        questRepository.completeObjectiveByMapNode(questId, nodeId)
    }
}
