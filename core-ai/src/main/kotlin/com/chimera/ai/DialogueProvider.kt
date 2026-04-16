package com.chimera.ai

import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract

/**
 * Abstraction for dialogue generation. Implementations include:
 * - FakeDialogueProvider (authored templates, deterministic)
 * - Future: CloudDialogueProvider (AI-backed)
 * - Future: OnDeviceDialogueProvider (local model)
 */
interface DialogueProvider {

    /**
     * Generate one NPC response turn given the scene context and player input.
     * Must return a valid [DialogueTurnResult] or throw.
     */
    suspend fun generateTurn(
        contract: SceneContract,
        playerInput: PlayerInput,
        characterState: CharacterState,
        recentMemories: List<MemoryShard>,
        turnHistory: List<DialogueTurnResult>
    ): DialogueTurnResult

    /**
     * Generate quick intent options for the player based on scene context.
     */
    suspend fun generateIntents(
        contract: SceneContract,
        characterState: CharacterState,
        turnHistory: List<DialogueTurnResult>
    ): List<String>

    /** True if the provider is available and healthy. */
    suspend fun isAvailable(): Boolean
}
