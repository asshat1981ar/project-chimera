package com.chimera.ai

import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptAssemblerTest {

    private val contract = SceneContract(
        sceneId = "test",
        sceneTitle = "Test Scene",
        npcId = "npc1",
        npcName = "Test NPC",
        setting = "a dark room",
        stakes = "testing",
        forbiddenTopics = listOf("secret_topic"),
        allowedReveals = listOf("allowed_info")
    )

    private val charState = CharacterState(
        characterId = "npc1",
        saveSlotId = 1,
        dispositionToPlayer = 0.5f,
        emotionalState = mapOf("trust" to 0.8f, "fear" to 0.1f),
        activeArchetype = "SHIFTING_THE_BURDEN"
    )

    @Test
    fun `system prompt includes NPC name`() {
        val prompt = PromptAssembler.buildSystemPrompt(contract, charState)
        assertTrue(prompt.contains("Test NPC"))
    }

    @Test
    fun `system prompt includes setting`() {
        val prompt = PromptAssembler.buildSystemPrompt(contract, charState)
        assertTrue(prompt.contains("a dark room"))
    }

    @Test
    fun `system prompt includes forbidden topics`() {
        val prompt = PromptAssembler.buildSystemPrompt(contract, charState)
        assertTrue(prompt.contains("secret_topic"))
        assertTrue(prompt.contains("NEVER discuss"))
    }

    @Test
    fun `system prompt includes allowed reveals`() {
        val prompt = PromptAssembler.buildSystemPrompt(contract, charState)
        assertTrue(prompt.contains("allowed_info"))
    }

    @Test
    fun `system prompt includes disposition description`() {
        val prompt = PromptAssembler.buildSystemPrompt(contract, charState)
        assertTrue(prompt.contains("friendly") || prompt.contains("loyal"))
    }

    @Test
    fun `system prompt includes JSON output schema`() {
        val prompt = PromptAssembler.buildSystemPrompt(contract, charState)
        assertTrue(prompt.contains("npcLine"))
        assertTrue(prompt.contains("emotion"))
        assertTrue(prompt.contains("relationshipDelta"))
    }

    @Test
    fun `system prompt includes archetype`() {
        val prompt = PromptAssembler.buildSystemPrompt(contract, charState)
        assertTrue(prompt.contains("SHIFTING_THE_BURDEN"))
    }

    @Test
    fun `system prompt includes top emotions`() {
        val prompt = PromptAssembler.buildSystemPrompt(contract, charState)
        assertTrue(prompt.contains("trust"))
    }

    @Test
    fun `user message includes player input`() {
        val msg = PromptAssembler.buildUserMessage(
            PlayerInput("Hello there"),
            emptyList(),
            emptyList()
        )
        assertTrue(msg.contains("Hello there"))
    }

    @Test
    fun `user message includes memories when provided`() {
        val memories = listOf(
            MemoryShard(1, 1, "s1", "npc1", "Player showed kindness", emptyList(), 0.8f)
        )
        val msg = PromptAssembler.buildUserMessage(PlayerInput("Hi"), memories, emptyList())
        assertTrue(msg.contains("Player showed kindness"))
    }

    @Test
    fun `user message includes turn history`() {
        val history = listOf(
            DialogueTurnResult(npcLine = "Previous NPC line", emotion = "warm")
        )
        val msg = PromptAssembler.buildUserMessage(PlayerInput("Hi"), emptyList(), history)
        assertTrue(msg.contains("Previous NPC line"))
    }

    @Test
    fun `user message without context is minimal`() {
        val msg = PromptAssembler.buildUserMessage(PlayerInput("Just text"), emptyList(), emptyList())
        assertTrue(msg.contains("Just text"))
        assertFalse(msg.contains("Memories"))
        assertFalse(msg.contains("Recent dialogue"))
    }

    @Test
    fun `intent prompt includes NPC name and disposition`() {
        val prompt = PromptAssembler.buildIntentPrompt(contract, charState, emptyList())
        assertTrue(prompt.contains("Test NPC"))
        assertTrue(prompt.contains("friendly") || prompt.contains("loyal"))
    }

    @Test
    fun `intent prompt requests JSON array format`() {
        val prompt = PromptAssembler.buildIntentPrompt(contract, charState, emptyList())
        assertTrue(prompt.contains("JSON array"))
    }

    @Test
    fun `system prompt with no forbidden topics omits section`() {
        val noForbidden = contract.copy(forbiddenTopics = emptyList())
        val prompt = PromptAssembler.buildSystemPrompt(noForbidden, charState)
        assertFalse(prompt.contains("NEVER discuss"))
    }

    @Test
    fun `hostile disposition produces correct label`() {
        val hostile = charState.copy(dispositionToPlayer = -0.8f)
        val prompt = PromptAssembler.buildSystemPrompt(contract, hostile)
        assertTrue(prompt.contains("hostile"))
    }
}
