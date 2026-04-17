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
open class FakeDialogueProvider @Inject constructor() : DialogueProvider {

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
            else -> {
                val line = getNpcVoicedResponse(contract.npcId, disposition, turnCount)
                    ?: getGenericResponse(disposition, turnCount)
                DialogueTurnResult(
                    npcLine = line,
                    emotion = if (disposition > 0.2f) "warm" else if (disposition < -0.2f) "cold" else "neutral",
                    relationshipDelta = 0.01f
                )
            }
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

    private fun getNpcVoicedResponse(npcId: String, disposition: Float, turnCount: Int): String? {
        val voices = npcVoices[npcId] ?: return null
        val tier = when {
            disposition > 0.3f -> voices.warm
            disposition < -0.2f -> voices.cold
            else -> voices.neutral
        }
        return tier[turnCount % tier.size]
    }

    private data class NpcVoice(
        val warm: List<String>,
        val cold: List<String>,
        val neutral: List<String>
    )

    private val npcVoices = mapOf(
        "warden" to NpcVoice(
            warm = listOf(
                "Duty brought me to this gate, but you give me reason to hope it wasn't in vain.",
                "You carry yourself well. The Hollow respects strength -- and so do I.",
                "I've watched the gate for years. Few earn my trust. You have."
            ),
            cold = listOf(
                "The gate stays shut to those who cannot prove their worth. That includes you.",
                "Duty demands I stand here. It does not demand I tolerate fools.",
                "The gate remembers every soul that passed. It will remember you too -- briefly."
            ),
            neutral = listOf(
                "The gate has stood longer than any of us. It will outlast us all.",
                "I neither welcome nor refuse you. The Hollow decides who enters.",
                "Duty is its own reward. Or so they told me when they posted me here."
            )
        ),
        "elena" to NpcVoice(
            warm = listOf(
                "For you? A fair price. You've earned that much from me.",
                "You strike a fair deal, and that's rare in the Hollow. I like working with you.",
                "Business is business, but friendship? That's something I don't offer lightly. Consider it offered.",
                "I've saved something special. Not for sale -- a gift. Don't make me regret it."
            ),
            cold = listOf(
                "Everything has a price, and yours just went up.",
                "I trade with anyone who pays. That doesn't mean I have to enjoy the company.",
                "Coin talks. You don't. Pay or leave."
            ),
            neutral = listOf(
                "The Hollow runs on barter. What do you bring to the table?",
                "I've survived here longer than most soldiers. Commerce is its own kind of armor.",
                "Supply and demand, stranger. Right now, I'm the supply, and you're the demand."
            )
        ),
        "marcus" to NpcVoice(
            warm = listOf(
                "At ease. You've proven you're not the enemy. That's enough for now.",
                "I don't say this often, but you fight clean. The garrison could use someone like you.",
                "Stand with me on the wall sometime. I'll show you what we're really guarding against."
            ),
            cold = listOf(
                "State your business or move along. I don't have time for games.",
                "I've seen your kind before. All talk, no backbone when the shadows come.",
                "One wrong move and I'll have you in irons. Don't test me.",
                "The garrison doesn't tolerate threats. Neither do I."
            ),
            neutral = listOf(
                "The watchtower sees everything. Including you.",
                "I keep watch. That's my purpose. What's yours?",
                "Another traveler. The Hollow collects them like stones in a river."
            )
        ),
        "aria" to NpcVoice(
            warm = listOf(
                "Fascinating -- you understood that immediately. Most people stare blankly when I explain it.",
                "The texts suggest a connection between the corruption and the King's crown. I need your help to prove it.",
                "I rarely share my research, but you've earned access. Come, let me show you what I've found.",
                "You ask the questions I've been afraid to ask myself. That takes courage."
            ),
            cold = listOf(
                "Your ignorance is showing. Perhaps come back when you've read something. Anything.",
                "I don't share my work with the unworthy. And you are decidedly unworthy.",
                "The texts are clear on one point: knowledge belongs to those who seek it. You merely stumble."
            ),
            neutral = listOf(
                "The archives hold more answers than any living person. I merely interpret.",
                "Interesting. Not the answer I expected, but interesting nonetheless.",
                "Every ruin tells a story. I'm still reading this one."
            )
        ),
        "thorne" to NpcVoice(
            warm = listOf(
                "You don't know what it was like in the garrison. But you listen, and that counts for something.",
                "I deserted because staying meant dying for a lie. You're the first person who didn't judge me for it.",
                "Trust gets people killed. But I'm starting to think not trusting you might be worse."
            ),
            cold = listOf(
                "Another righteous outsider. Save the lectures -- I've heard them all.",
                "You want to know why I deserted? None of your damn business.",
                "Keep your distance. I didn't survive this long by making friends."
            ),
            neutral = listOf(
                "The garrison fell apart from the inside. Nobody talks about that.",
                "I sleep with one eye open. The Hollow teaches you that, or it kills you.",
                "Don't mistake my camp for hospitality. This is survival, not friendship."
            )
        ),
        "vessa" to NpcVoice(
            warm = listOf(
                "The hollow speaks through stone, and today it speaks of you with warmth.",
                "Faith is not comfort -- it is the courage to face what the darkness reveals. You have that courage.",
                "The shrine remembers your kindness. So do I.",
                "Come. Pray with me. Not to the old gods -- to whatever remains that is still good."
            ),
            cold = listOf(
                "The corruption sees your heart, outsider. It does not like what it finds.",
                "I tend this altar alone. Your presence profanes it.",
                "Faith demands sacrifice. You bring only demands."
            ),
            neutral = listOf(
                "The incense burns black because the hollow is sick. Not evil -- sick. There is a difference.",
                "I was a priestess before the fall. Now I am... something else.",
                "The old prayers don't work anymore. But I say them anyway."
            )
        ),
        "hollow_king" to NpcVoice(
            warm = listOf(
                "You remind me of who I was before the crown consumed me. That is... dangerous.",
                "Power is not given -- it is taken. But you... you might deserve what I offer.",
                "Kneel not in submission, but in recognition of what you could become."
            ),
            cold = listOf(
                "You dare stand before a king and offer nothing? Kneel or be forgotten.",
                "I have waited centuries. Your defiance is a heartbeat compared to my patience.",
                "The crown sees all who enter the Hollow. It found you... wanting.",
                "Insignificant. The echoes of my reign will outlast your brief flame."
            ),
            neutral = listOf(
                "I was a king once. Now I am an echo. But echoes still carry power.",
                "The throne remembers every soul that sought it. Will you seek, or will you flee?",
                "Time means nothing here. Your choices, however, mean everything."
            )
        ),
        "kael" to NpcVoice(
            warm = listOf(
                "Iron remembers the hands that shape it. Yours have earned my respect.",
                "You can't forge what you won't face. But you? You face everything.",
                "I've reforged weapons for cowards and kings. You're neither, and that's why I'll help."
            ),
            cold = listOf(
                "The fire doesn't care about your reasons. Neither do I.",
                "You come here wanting favors but offering nothing. The forge stays cold for the unworthy.",
                "I sealed the gate for a reason. Don't make me regret opening it for you."
            ),
            neutral = listOf(
                "Every piece of metal has a story. I just help it find the right ending.",
                "The forge is patient. I am not. What do you need?",
                "Iron remembers what flesh forgets. That's why I trust my work more than words."
            )
        ),
        "seren" to NpcVoice(
            warm = listOf(
                "We chose ash over chains, and you've proven you understand why.",
                "The Reforged don't kneel, but we stand with those who've earned it. You have.",
                "Build forward, not backward. That's always been our way -- and now it's yours too."
            ),
            cold = listOf(
                "The Reforged remember what the Hollow cost us. Your words sound too much like the old king's.",
                "We didn't survive the fall just to bow to another outsider with promises.",
                "Actions, not speeches. The Reforged weigh deeds, not intentions."
            ),
            neutral = listOf(
                "We chose ash over chains. Every day, we choose again.",
                "The Reforged don't kneel. But we listen. Speak.",
                "Build forward, not backward. That's the only law here."
            )
        ),
        "dara" to NpcVoice(
            warm = listOf(
                "The tide speaks your name with warmth. That is rare and precious.",
                "Faith is the courage to face what the deep reveals. You have that courage in abundance.",
                "The shore remembers kindness longer than storms. I will remember you."
            ),
            cold = listOf(
                "The tide speaks your name with caution. I listen to the tide.",
                "You walk on sacred shore with careless feet. The deep notices.",
                "The sea does not forgive. I learned that lesson. You should too."
            ),
            neutral = listOf(
                "The tide brings what it brings. I merely read the patterns.",
                "I was a tide-speaker before the corruption came. Now I speak for what remains.",
                "Salt and ash -- that's all we have left. But it's enough to build on."
            )
        ),
        "rook" to NpcVoice(
            warm = listOf(
                "You're the first person I've trusted with this. Don't make me regret it.",
                "I salvage things others throw away. Including friendships, apparently.",
                "We could make a good team. You find the trouble, I find the profit."
            ),
            cold = listOf(
                "Everyone's got an angle. Yours just isn't paying well enough for my time.",
                "I salvage for a living. Your trust issues aren't worth the effort.",
                "The cove has rules. First rule: don't waste my time."
            ),
            neutral = listOf(
                "Everything washes up eventually. Even answers.",
                "I trade in salvage and secrets. Which are you buying?",
                "The coast takes what it wants. I just pick through what's left."
            )
        ),
        "corruption" to NpcVoice(
            warm = listOf(
                "You begin to understand. Not resist -- understand. That is the first step toward unity.",
                "The crown showed the king the same truth I show you now. Power shared is power multiplied.",
                "You do not need to fear me. I am what you will become. And it is beautiful."
            ),
            cold = listOf(
                "You resist what you cannot comprehend. The tide will take you regardless.",
                "The king resisted too. He wore the crown anyway. You are no different.",
                "Every stone you throw returns as a wave. I am patient. The sea always wins.",
                "Insignificant. The coral grows around obstacles. You are merely an obstacle."
            ),
            neutral = listOf(
                "I am not evil. I am what happens when power has no vessel. I simply... am.",
                "The king built me from ambition and fear. I outgrew both. What will you outgrow?",
                "You seek to destroy what you do not understand. That is how the corruption began."
            )
        )
    )

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
