package com.chimera.domain.usecase

import com.chimera.data.GameSessionManager
import com.chimera.data.repository.SaveRepository
import com.chimera.database.dao.SceneInstanceDao
import javax.inject.Inject

/**
 * Advances the active slot's chapter tag based on completed scene milestones.
 *
 * Progression rules:
 *  - prologue → act1 once the prologue scene is completed
 *  - act1 → act2 once the act1 finale scene is completed
 *  - act2 → act3 once the act2 finale scene is completed
 */
class ChapterProgressionUseCase @Inject constructor(
    private val saveRepository: SaveRepository,
    private val sceneInstanceDao: SceneInstanceDao,
    private val gameSessionManager: GameSessionManager
) {
    suspend operator fun invoke() {
        val slotId = gameSessionManager.activeSlotId.value ?: return
        val slot = saveRepository.getSlot(slotId) ?: return

        val completedSceneIds = sceneInstanceDao.getBySlot(slotId)
            .filter { it.status == "completed" }
            .map { it.sceneId }
            .toSet()

        val newTag = when (slot.chapterTag) {
            "prologue" -> if (ACT1_ENTRY_SCENE in completedSceneIds) "act1" else null
            "act1"     -> if (ACT2_ENTRY_SCENE in completedSceneIds) "act2" else null
            "act2"     -> if (ACT3_ENTRY_SCENE in completedSceneIds) "act3" else null
            else       -> null
        }

        if (newTag != null) {
            saveRepository.updateChapterTag(slotId, newTag)
        }
    }

    companion object {
        const val ACT1_ENTRY_SCENE = "prologue_scene_1"
        const val ACT2_ENTRY_SCENE = "hollow_approach_complete"
        const val ACT3_ENTRY_SCENE = "ashen_gate_opened"
    }
}
