package com.chimera.ai

import android.util.Log
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates dialogue turn generation with automatic fallback.
 * Tries the primary provider first, falls back to FakeDialogueProvider on failure.
 */
@Singleton
class DialogueOrchestrator @Inject constructor(
    private val fallbackProvider: FakeDialogueProvider
) {
    // Primary provider will be injected when cloud AI is implemented
    private var primaryProvider: DialogueProvider? = null
    private var _isFallbackActive = false
    val isFallbackActive: Boolean get() = _isFallbackActive

    fun setPrimaryProvider(provider: DialogueProvider) {
        primaryProvider = provider
    }

    suspend fun generateTurn(
        contract: SceneContract,
        playerInput: PlayerInput,
        characterState: CharacterState,
        recentMemories: List<MemoryShard> = emptyList(),
        turnHistory: List<DialogueTurnResult> = emptyList()
    ): DialogueTurnResult {
        val primary = primaryProvider
        if (primary != null) {
            try {
                if (primary.isAvailable()) {
                    val result = primary.generateTurn(
                        contract, playerInput, characterState, recentMemories, turnHistory
                    )
                    _isFallbackActive = false
                    return validateAndClamp(result)
                }
            } catch (e: Exception) {
                Log.w("DialogueOrchestrator", "Primary provider failed, falling back", e)
            }
        }

        // Fallback path
        _isFallbackActive = true
        return validateAndClamp(
            fallbackProvider.generateTurn(
                contract, playerInput, characterState, recentMemories, turnHistory
            )
        )
    }

    suspend fun generateIntents(
        contract: SceneContract,
        characterState: CharacterState,
        turnHistory: List<DialogueTurnResult> = emptyList()
    ): List<String> {
        val primary = primaryProvider
        if (primary != null) {
            try {
                if (primary.isAvailable()) {
                    return primary.generateIntents(contract, characterState, turnHistory)
                }
            } catch (e: Exception) {
                Log.w("DialogueOrchestrator", "Primary intents failed, falling back", e)
            }
        }
        return fallbackProvider.generateIntents(contract, characterState, turnHistory)
    }

    /**
     * Validate and clamp AI output to prevent hallucinated deltas or bad flags.
     */
    private fun validateAndClamp(result: DialogueTurnResult): DialogueTurnResult {
        return result.copy(
            npcLine = result.npcLine.ifBlank { "(The NPC says nothing.)" },
            relationshipDelta = result.relationshipDelta.coerceIn(-0.25f, 0.25f),
            memoryCandidates = result.memoryCandidates.take(3)
        )
    }
}
