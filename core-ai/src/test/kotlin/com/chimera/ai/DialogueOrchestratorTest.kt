package com.chimera.ai

import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DialogueOrchestratorTest {

    private lateinit var orchestrator: DialogueOrchestrator
    private lateinit var fakeProvider: FakeDialogueProvider

    private val contract = SceneContract(
        sceneId = "test_scene",
        sceneTitle = "Test Scene",
        npcId = "npc_test",
        npcName = "Test NPC",
        setting = "a test room"
    )

    private val defaultState = CharacterState(characterId = "npc_test", saveSlotId = 1)

    @Before
    fun setup() {
        fakeProvider = FakeDialogueProvider()
        orchestrator = DialogueOrchestrator(fakeProvider)
    }

    @Test
    fun `generateTurn returns valid result from fallback`() = runTest {
        val result = orchestrator.generateTurn(
            contract,
            PlayerInput("Hello"),
            defaultState
        )
        assertTrue(result.npcLine.isNotBlank())
        assertTrue(orchestrator.isFallbackActive)
    }

    @Test
    fun `output validation clamps relationship delta`() = runTest {
        // Kind input with positive disposition should get positive delta
        val kindState = defaultState.copy(dispositionToPlayer = 0.5f)
        val result = orchestrator.generateTurn(
            contract,
            PlayerInput("Thank you friend, I trust you"),
            kindState,
            turnHistory = listOf(
                DialogueTurnResult(npcLine = "previous line")
            )
        )
        // Delta should be clamped to [-0.25, 0.25]
        assertTrue(result.relationshipDelta >= -0.25f)
        assertTrue(result.relationshipDelta <= 0.25f)
    }

    @Test
    fun `output validation limits memory candidates to 3`() = runTest {
        val result = orchestrator.generateTurn(
            contract,
            PlayerInput("Tell me everything you know"),
            defaultState
        )
        assertTrue(result.memoryCandidates.size <= 3)
    }

    @Test
    fun `output validation replaces blank npc line`() = runTest {
        // FakeDialogueProvider shouldn't produce blank lines, but the
        // orchestrator validates just in case
        val result = orchestrator.generateTurn(
            contract,
            PlayerInput("test input"),
            defaultState
        )
        assertTrue(result.npcLine.isNotBlank())
    }

    @Test
    fun `generateIntents returns non-empty list`() = runTest {
        val intents = orchestrator.generateIntents(contract, defaultState)
        assertTrue(intents.isNotEmpty())
        assertTrue(intents.size <= 5)
    }

    @Test
    fun `isFallbackActive is true when no primary provider set`() = runTest {
        orchestrator.generateTurn(contract, PlayerInput("test"), defaultState)
        assertTrue(orchestrator.isFallbackActive)
    }

    @Test
    fun `threatening input produces negative delta`() = runTest {
        val result = orchestrator.generateTurn(
            contract,
            PlayerInput("I will destroy you"),
            defaultState
        )
        assertTrue(result.relationshipDelta < 0f)
    }

    @Test
    fun `kind input with high disposition produces grateful response`() = runTest {
        val kindState = defaultState.copy(dispositionToPlayer = 0.5f)
        val result = orchestrator.generateTurn(
            contract,
            PlayerInput("I want to help you, please trust me"),
            kindState,
            turnHistory = listOf(DialogueTurnResult(npcLine = "opening"))
        )
        assertEquals("grateful", result.emotion)
    }

    @Test
    fun `question input produces thoughtful response`() = runTest {
        val result = orchestrator.generateTurn(
            contract,
            PlayerInput("What happened here?"),
            defaultState,
            turnHistory = listOf(DialogueTurnResult(npcLine = "opening"))
        )
        assertEquals("thoughtful", result.emotion)
    }
}
