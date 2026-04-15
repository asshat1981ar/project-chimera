package com.chimera.ai

import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deterministic dialogue provider using authored templates.
 * Used as the offline fallback and for development/testing.
 * Returns authored NPC responses with disposition-aware branching.
 */
@Singleton
class FakeDialogueProvider @Inject constructor() : DialogueProvider {

    override suspend fun generateTurn(
        contract: SceneContract,
        playerInput: PlayerInput,
        characterState: CharacterState,
        recentMemories: List<MemoryShard>,
        turnHistory: List<DialogueTurnResult>
    ): DialogueTurnResult {
        val disposition = characterState.dispositionToPlayer
        val turnCount = turnHistory.size

        // Select response based on disposition and input tone
        val inputLower = playerInput.text.lowercase()
        val isThreatening = inputLower.containsAny("threaten", "kill", "destroy", "fight", "attack")
        val isKind = inputLower.containsAny("help", "friend", "trust", "sorry", "please", "thank")
        val isQuestion = inputLower.contains("?") || inputLower.containsAny("why", "what", "how", "where", "who")

        return when {
            isThreatening && disposition < 0f -> DialogueTurnResult(
                npcLine = "You dare threaten me? After everything? I should have known better than to trust an outsider.",
                emotion = "hostile",
                relationshipDelta = -0.15f,
                flags = listOf("hostility_escalated"),
                memoryCandidates = listOf("Player threatened ${contract.npcName} while relationship was already strained")
            )
            isThreatening && disposition >= 0f -> DialogueTurnResult(
                npcLine = "I... didn't expect that from you. Perhaps I misjudged who you are.",
                emotion = "hurt",
                relationshipDelta = -0.10f,
                flags = listOf("trust_broken"),
                memoryCandidates = listOf("Player turned aggressive despite good standing with ${contract.npcName}")
            )
            isKind && disposition > 0.3f -> DialogueTurnResult(
                npcLine = "Your words carry weight with me, more than you might know. The Hollow tests everyone, but perhaps together we can endure it.",
                emotion = "grateful",
                relationshipDelta = 0.08f,
                memoryCandidates = listOf("Player showed kindness to ${contract.npcName} during the ${contract.sceneTitle}")
            )
            isKind && disposition <= 0.3f -> DialogueTurnResult(
                npcLine = "Kind words are cheap in the Hollow. But... I'll remember you said that.",
                emotion = "guarded",
                relationshipDelta = 0.05f,
                memoryCandidates = listOf("Player attempted to build trust with ${contract.npcName}")
            )
            isQuestion -> DialogueTurnResult(
                npcLine = getQuestionResponse(contract, turnCount),
                emotion = "thoughtful",
                relationshipDelta = 0.02f,
                memoryCandidates = listOf("Player asked questions about the ${contract.setting}")
            )
            turnCount == 0 -> DialogueTurnResult(
                npcLine = "The path you walk is not one taken lightly. Tell me -- what brought you to the ${contract.setting}?",
                emotion = "curious",
                relationshipDelta = 0f,
                directorNotes = "Opening turn, establish NPC voice"
            )
            turnCount >= contract.maxTurns - 1 -> DialogueTurnResult(
                npcLine = "We've spoken long enough. The shadows grow restless, and we both have places to be. Until next time.",
                emotion = "resolute",
                relationshipDelta = 0f,
                flags = listOf("scene_ending"),
                directorNotes = "Scene reaching max turns, wrap up"
            )
            else -> DialogueTurnResult(
                npcLine = getGenericResponse(disposition, turnCount),
                emotion = if (disposition > 0.2f) "warm" else if (disposition < -0.2f) "cold" else "neutral",
                relationshipDelta = 0.01f
            )
        }
    }

    override suspend fun generateIntents(
        contract: SceneContract,
        characterState: CharacterState,
        turnHistory: List<DialogueTurnResult>
    ): List<String> {
        val disposition = characterState.dispositionToPlayer
        val turnCount = turnHistory.size

        return when {
            turnCount == 0 -> listOf(
                "I seek the truth about the Hollow King.",
                "I have a debt to repay.",
                "Curiosity, nothing more.",
                "None of your concern."
            )
            turnHistory.lastOrNull()?.flags?.contains("scene_ending") == true -> listOf(
                "Farewell, and stay safe.",
                "We'll meet again.",
                "[Leave silently]"
            )
            disposition < -0.3f -> listOf(
                "I mean no harm.",
                "What would it take to earn your trust?",
                "Then we have nothing more to discuss.",
                "You'll regret this attitude."
            )
            disposition > 0.3f -> listOf(
                "Tell me more about yourself.",
                "What dangers lie ahead?",
                "I could use your help with something.",
                "Thank you for trusting me."
            )
            else -> listOf(
                "Tell me what you know.",
                "What do you make of all this?",
                "I have my own reasons.",
                "Let's get to the point."
            )
        }
    }

    override suspend fun isAvailable(): Boolean = true

    private fun getQuestionResponse(contract: SceneContract, turnCount: Int): String {
        val responses = listOf(
            "That's not a simple question. The ${contract.setting} holds many secrets, and not all of them want to be found.",
            "You ask the right questions, at least. Most who come here never think to ask at all.",
            "The answer depends on who you ask. The old stories say one thing. What I've seen says another.",
            "I could tell you, but the truth has a cost in the Hollow. Are you willing to pay it?"
        )
        return responses[turnCount % responses.size]
    }

    private fun getGenericResponse(disposition: Float, turnCount: Int): String {
        return when {
            disposition > 0.5f -> {
                val warm = listOf(
                    "I'm glad you're here. Not many allies remain in these lands.",
                    "You've proven yourself more than most. The Hollow hasn't broken you yet.",
                    "There's something I should tell you -- but not here. Meet me at camp tonight."
                )
                warm[turnCount % warm.size]
            }
            disposition < -0.3f -> {
                val cold = listOf(
                    "Speak your piece and be done with it. My patience wears thin.",
                    "Every word you say reminds me why I don't trust outsiders.",
                    "The Hollow has a way of revealing who people really are. I wonder what it will reveal about you."
                )
                cold[turnCount % cold.size]
            }
            else -> {
                val neutral = listOf(
                    "The road ahead is uncertain. But then, it always is in the Hollow.",
                    "I've seen things in these ruins that would turn your blood cold. Tread carefully.",
                    "Others have come before you. Most didn't last long. What makes you different?"
                )
                neutral[turnCount % neutral.size]
            }
        }
    }

    private fun String.containsAny(vararg words: String): Boolean =
        words.any { this.contains(it) }
}
