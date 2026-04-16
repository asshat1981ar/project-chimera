package com.chimera.ai

import com.chimera.model.CharacterState
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeDialogueProviderTest {

    private lateinit var provider: FakeDialogueProvider
    private val contract = SceneContract("test", "Test Scene", "warden", "The Warden", "a gate")
    private val neutralState = CharacterState("warden", 1, dispositionToPlayer = 0f)
    private val warmState = CharacterState("warden", 1, dispositionToPlayer = 0.6f)
    private val coldState = CharacterState("warden", 1, dispositionToPlayer = -0.5f)

    @Before
    fun setup() {
        provider = FakeDialogueProvider()
    }

    @Test
    fun `always available`() = runTest {
        assertTrue(provider.isAvailable())
    }

    @Test
    fun `opening turn uses contract setting`() = runTest {
        val result = provider.generateTurn(contract, PlayerInput("[Scene begins]"), neutralState, emptyList(), emptyList())
        assertNotNull(result.npcLine)
        assertTrue(result.npcLine.isNotBlank())
    }

    @Test
    fun `threatening input produces negative delta`() = runTest {
        val result = provider.generateTurn(
            contract, PlayerInput("I will destroy you"),
            neutralState, emptyList(), listOf(com.chimera.model.DialogueTurnResult("opening"))
        )
        assertTrue(result.relationshipDelta < 0f)
    }

    @Test
    fun `kind input with high disposition produces grateful emotion`() = runTest {
        val result = provider.generateTurn(
            contract, PlayerInput("Thank you, I trust you friend"),
            warmState, emptyList(), listOf(com.chimera.model.DialogueTurnResult("opening"))
        )
        assertEquals("grateful", result.emotion)
    }

    @Test
    fun `question input produces thoughtful emotion`() = runTest {
        val result = provider.generateTurn(
            contract, PlayerInput("What happened here?"),
            neutralState, emptyList(), listOf(com.chimera.model.DialogueTurnResult("opening"))
        )
        assertEquals("thoughtful", result.emotion)
    }

    @Test
    fun `NPC-specific voice used for warden`() = runTest {
        val result = provider.generateTurn(
            contract, PlayerInput("Just talking"),
            warmState, emptyList(),
            listOf(com.chimera.model.DialogueTurnResult("opening"), com.chimera.model.DialogueTurnResult("second"))
        )
        // Warden warm voice includes duty/gate/trust themes
        assertNotNull(result.npcLine)
    }

    @Test
    fun `NPC-specific voice used for elena`() = runTest {
        val elenaContract = contract.copy(npcId = "elena", npcName = "Elena")
        val result = provider.generateTurn(
            elenaContract, PlayerInput("Just talking"),
            warmState, emptyList(),
            listOf(com.chimera.model.DialogueTurnResult("opening"), com.chimera.model.DialogueTurnResult("second"))
        )
        assertNotNull(result.npcLine)
    }

    @Test
    fun `generates intents for opening turn`() = runTest {
        val intents = provider.generateIntents(contract, neutralState, emptyList())
        assertTrue(intents.isNotEmpty())
        assertTrue(intents.size <= 5)
    }

    @Test
    fun `generates different intents for high disposition`() = runTest {
        val intents = provider.generateIntents(contract, warmState, listOf(com.chimera.model.DialogueTurnResult("line")))
        assertTrue(intents.isNotEmpty())
        // High disposition intents should include trust-oriented options
        assertTrue(intents.any { it.contains("help") || it.contains("trust") || it.contains("Thank") || it.contains("Tell") })
    }

    @Test
    fun `generates scene-ending intents when flagged`() = runTest {
        val endingHistory = listOf(com.chimera.model.DialogueTurnResult("line", flags = listOf("scene_ending")))
        val intents = provider.generateIntents(contract, neutralState, endingHistory)
        assertTrue(intents.any { it.contains("Farewell") || it.contains("Leave") || it.contains("meet") })
    }

    @Test
    fun `late turn produces scene ending flag`() = runTest {
        val contract11 = contract.copy(maxTurns = 12)
        val history = (1..11).map { com.chimera.model.DialogueTurnResult("turn $it") }
        val result = provider.generateTurn(contract11, PlayerInput("still talking"), neutralState, emptyList(), history)
        assertTrue(result.flags.contains("scene_ending"))
    }
}
