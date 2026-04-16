package com.chimera.ai

import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract

/**
 * Assembles LLM prompts from game context for NPC dialogue generation.
 * Used by all cloud AI adapters (Gemini, Groq, OpenRouter).
 */
object PromptAssembler {

    fun buildSystemPrompt(contract: SceneContract, characterState: CharacterState): String {
        return buildString {
            appendLine("You are ${contract.npcName} in a dark fantasy RPG called Chimera: Ashes of the Hollow King.")
            appendLine("Setting: ${contract.setting}")
            appendLine("Stakes: ${contract.stakes}")
            appendLine()
            appendLine("Your disposition toward the player: ${formatDisposition(characterState.dispositionToPlayer)}")
            if (characterState.activeArchetype != null) {
                appendLine("Your behavioral pattern: ${characterState.activeArchetype}")
            }
            if (characterState.emotionalState.isNotEmpty()) {
                val topEmotions = characterState.emotionalState.entries
                    .sortedByDescending { it.value }
                    .take(3)
                    .joinToString(", ") { "${it.key} (${(it.value * 100).toInt()}%)" }
                appendLine("Your current emotions: $topEmotions")
            }
            appendLine()
            if (contract.forbiddenTopics.isNotEmpty()) {
                appendLine("NEVER discuss: ${contract.forbiddenTopics.joinToString(", ")}")
            }
            if (contract.allowedReveals.isNotEmpty()) {
                appendLine("You may reveal information about: ${contract.allowedReveals.joinToString(", ")}")
            }
            appendLine()
            appendLine("Respond ONLY with valid JSON in this exact format:")
            appendLine("""{"npcLine":"your dialogue","emotion":"one_word_emotion","relationshipDelta":0.0,"flags":[],"memoryCandidates":[]}""")
            appendLine()
            appendLine("Rules:")
            appendLine("- npcLine: 1-3 sentences, in character, no meta-commentary")
            appendLine("- emotion: one of: neutral, warm, cold, hostile, hurt, grateful, guarded, thoughtful, curious, resolute")
            appendLine("- relationshipDelta: float between -0.25 and 0.25")
            appendLine("- flags: empty list unless scene should end (use [\"scene_ending\"])")
            appendLine("- memoryCandidates: 0-2 short summaries of this moment worth remembering")
        }
    }

    fun buildUserMessage(
        playerInput: PlayerInput,
        recentMemories: List<MemoryShard>,
        turnHistory: List<DialogueTurnResult>
    ): String {
        return buildString {
            if (recentMemories.isNotEmpty()) {
                appendLine("Memories from past encounters:")
                recentMemories.take(5).forEach { appendLine("- ${it.summary}") }
                appendLine()
            }
            if (turnHistory.isNotEmpty()) {
                appendLine("Recent dialogue in this scene:")
                turnHistory.takeLast(4).forEach { appendLine("NPC: ${it.npcLine}") }
                appendLine()
            }
            appendLine("Player says: ${playerInput.text}")
        }
    }

    fun buildIntentPrompt(
        contract: SceneContract,
        characterState: CharacterState,
        turnHistory: List<DialogueTurnResult>
    ): String {
        return buildString {
            appendLine("You are generating dialogue intent options for a player talking to ${contract.npcName}.")
            appendLine("Setting: ${contract.setting}")
            appendLine("NPC disposition: ${formatDisposition(characterState.dispositionToPlayer)}")
            appendLine("Turns so far: ${turnHistory.size}")
            appendLine()
            appendLine("Return a JSON array of exactly 4 short player intent strings (max 8 words each).")
            appendLine("Example: [\"I seek the truth.\",\"None of your concern.\",\"Tell me more.\",\"I should go.\"]")
        }
    }

    private fun formatDisposition(d: Float): String = when {
        d > 0.5f -> "loyal (${"%.1f".format(d)})"
        d > 0.2f -> "friendly (${"%.1f".format(d)})"
        d > -0.2f -> "neutral (${"%.1f".format(d)})"
        d > -0.5f -> "wary (${"%.1f".format(d)})"
        else -> "hostile (${"%.1f".format(d)})"
    }
}
