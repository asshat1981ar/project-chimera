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
    }
}
