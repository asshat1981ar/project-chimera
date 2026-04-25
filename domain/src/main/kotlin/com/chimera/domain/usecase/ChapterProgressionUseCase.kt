package com.chimera.domain.usecase

import com.chimera.data.GameSessionManager
import com.chimera.data.repository.DialogueRepository
import com.chimera.data.repository.SaveRepository
import javax.inject.Inject

/**
 * Derives the player's current chapter tag from completed scenes and
 * persists it to the active [SaveSlot.chapterTag].
 *
 * The chapter tag drives:
 *  - [MultiActMapNodeLoader] — which act map JSON to load
 *  - [HomeScreen] — the "Chapter: ___" display string
 *  - Act progression gating
 *
 * Chapter detection logic (in order of priority):
 *  1. If any Act-3 climax scene is completed  → "act3"
 *  2. If any Act-2 climax scene is completed  → "act3" (entering act 3)
 *  3. If ashen_gate scene is completed        → "act2"
 *  4. If hollow_approach scene is completed   → "act2" (entering act 2)
 *  5. If any act-1 scene beyond prologue done → "act1"
 *  6. Default                                 → "prologue"
 *
 * Call this use case after every scene completion inside [SubmitDialogueTurnUseCase]
 * or [StartSceneUseCase].
 *
 * Bridge tags for cinematic transitions:
 *  - hollow_approach_complete → triggers act1_finale cinematic
 *  - act2_climax_complete     → triggers act2_finale cinematic
 *  - act3_begun               → marks act3 as started
 */
class ChapterProgressionUseCase @Inject constructor(
    private val dialogueRepository: DialogueRepository,
    private val saveRepository: SaveRepository,
    private val gameSessionManager: GameSessionManager
) {
    /**
     * Evaluate chapter progression for the active slot and update the chapter
     * tag if the player has advanced.
     *
     * Returns the new chapter tag (or the existing one if unchanged).
     */
    suspend operator fun invoke(): String {
        val slotId = gameSessionManager.activeSlotId.value ?: return "prologue"
        val completed = dialogueRepository.getCompletedSceneIds(slotId)

        val newTag = when {
            ACT3_CLIMAX_SCENES.any { it in completed }   -> "act3"
            ACT2_CLIMAX_SCENES.any { it in completed }   -> "act3"
            ACT2_ENTRY_SCENES.any { it in completed }    -> "act2"
            ACT1_MIDGAME_SCENES.any { it in completed }  -> "act1"
            else                                          -> "prologue"
        }

        // Only write if changed — avoids a spurious DB update on every turn
        val current = saveRepository.getSlot(slotId)?.chapterTag ?: "prologue"
        if (newTag != current) {
            saveRepository.updateChapterTag(slotId, newTag)
        }

        return newTag
    }

    /**
     * Check if a cinematic transition should be triggered based on completed scenes.
     * Returns the cinematic scene ID if one should play, or null otherwise.
     */
    suspend fun getCinematicTransition(): String? {
        val slotId = gameSessionManager.activeSlotId.value ?: return null
        val completed = dialogueRepository.getCompletedSceneIds(slotId)

        // Check for bridge tags that trigger cinematic transitions
        return when {
            // Act 1 → Act 2 transition: after hollow_approach but before act2 scenes
            "hollow_approach" in completed &&
            !CINEMATIC_1_COMPLETE_TAGS.any { it in completed } -> "act1_finale"

            // Act 2 → Act 3 transition: after act2_climax but before act3 scenes
            "act2_climax" in completed &&
            !CINEMATIC_2_COMPLETE_TAGS.any { it in completed } -> "act2_finale"

            // Act 3 opening: after act2_finale cinematic
            "act2_finale" in completed &&
            !CINEMATIC_3_COMPLETE_TAGS.any { it in completed } -> "act3_opening"

            else -> null
        }
    }

    /**
     * Mark a cinematic transition as complete by recording the bridge tag.
     */
    suspend fun markCinematicComplete(cinematicSceneId: String) {
        val slotId = gameSessionManager.activeSlotId.value ?: return
        val bridgeTag = when (cinematicSceneId) {
            "act1_finale" -> "hollow_approach_complete"
            "act2_finale" -> "act2_climax_complete"
            "act3_opening" -> "act3_begun"
            else -> return
        }
        dialogueRepository.markSceneComplete(slotId, bridgeTag)
    }

    companion object {
        /** Completing any of these marks the player as being in Act 2. */
        private val ACT2_ENTRY_SCENES = setOf("hollow_approach", "ashen_gate")

        /** Completing any of these marks the player as being in Act 3. */
        private val ACT2_CLIMAX_SCENES = setOf("act2_climax", "ashen_throne")

        /** Completing any of these marks the player as having finished Act 3 / endgame. */
        private val ACT3_CLIMAX_SCENES = setOf("act3_climax", "heart_of_tide")

        /** Scenes in Act 1 that are beyond the opening prologue. */
        private val ACT1_MIDGAME_SCENES = setOf(
            "outer_ruins_1", "watchtower_1", "merchants_1", "deep_hollow_1",
            "thorne_encounter", "vessa_shrine", "elena_recruitment",
            "warden_betrayal"
        )

        /** Tags indicating Act 1→2 cinematic has been viewed. */
        private val CINEMATIC_1_COMPLETE_TAGS = setOf("hollow_approach_complete", "act1_finale")

        /** Tags indicating Act 2→3 cinematic has been viewed. */
        private val CINEMATIC_2_COMPLETE_TAGS = setOf("act2_climax_complete", "act2_finale")

        /** Tags indicating Act 3 opening cinematic has been viewed. */
        private val CINEMATIC_3_COMPLETE_TAGS = setOf("act3_begun", "act3_opening")
    }
}
