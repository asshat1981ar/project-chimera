package com.chimera.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelValidationTest {

    @Test
    fun `CharacterState disposition bounds are correct`() {
        assertEquals(-1.0f, CharacterState.DISPOSITION_MIN, 0.001f)
        assertEquals(1.0f, CharacterState.DISPOSITION_MAX, 0.001f)
    }

    @Test
    fun `SaveSlot default chapter is prologue`() {
        val slot = SaveSlot(slotIndex = 0, playerName = "Test")
        assertEquals("prologue", slot.chapterTag)
    }

    @Test
    fun `SaveSlot default is empty`() {
        val slot = SaveSlot(slotIndex = 0, playerName = "")
        assertTrue(slot.isEmpty)
    }

    @Test
    fun `CharacterRole enum has all expected values`() {
        val roles = CharacterRole.values().map { it.name }
        assertTrue(roles.contains("PROTAGONIST"))
        assertTrue(roles.contains("COMPANION"))
        assertTrue(roles.contains("NPC_ALLY"))
        assertTrue(roles.contains("NPC_NEUTRAL"))
        assertTrue(roles.contains("NPC_HOSTILE"))
        assertTrue(roles.contains("FACTION_LEADER"))
    }

    @Test
    fun `DialogueTurnResult defaults are safe`() {
        val result = DialogueTurnResult(npcLine = "test")
        assertEquals("neutral", result.emotion)
        assertEquals(0f, result.relationshipDelta, 0.001f)
        assertTrue(result.flags.isEmpty())
        assertTrue(result.memoryCandidates.isEmpty())
    }

    @Test
    fun `SceneContract default maxTurns is 12`() {
        val contract = SceneContract("id", "title", "npc", "NPC Name", "setting")
        assertEquals(12, contract.maxTurns)
    }

    @Test
    fun `SceneContract default forbiddenTopics is empty`() {
        val contract = SceneContract("id", "title", "npc", "NPC", "setting")
        assertTrue(contract.forbiddenTopics.isEmpty())
    }

    @Test
    fun `MemoryShard default importance is 0_5`() {
        val shard = MemoryShard(saveSlotId = 1, sceneId = "s", characterId = "c", summary = "test")
        assertEquals(0.5f, shard.importanceScore, 0.001f)
    }

    @Test
    fun `PlayerInput default is not quick intent`() {
        val input = PlayerInput(text = "hello")
        assertEquals(false, input.isQuickIntent)
    }

    @Test
    fun `GameEvent sealed hierarchy has expected subclasses`() {
        val slotSelected = GameEvent.SaveSlotSelected(1)
        val sceneEntered = GameEvent.SceneEntered("s1")
        val relationshipChanged = GameEvent.RelationshipChanged("npc", 0.1f, 0.5f)

        assertTrue(slotSelected is GameEvent)
        assertTrue(sceneEntered is GameEvent)
        assertTrue(relationshipChanged is GameEvent)
    }

    @Test
    fun `QuestObjectiveStatus blocking statuses exclude completed optional`() {
        assertTrue(QuestObjectiveStatus.ACTIVE.blocksQuestCompletion)
        assertTrue(QuestObjectiveStatus.HIDDEN.blocksQuestCompletion)
        assertFalse(QuestObjectiveStatus.COMPLETED.blocksQuestCompletion)
        assertFalse(QuestObjectiveStatus.OPTIONAL_COMPLETED.blocksQuestCompletion)
    }

    @Test
    fun `ActiveObjectiveSummary primary action defaults to none`() {
        val summary = ActiveObjectiveSummary(
            questId = 1L,
            objectiveId = 2L,
            title = "Reach the Processional",
            storyContext = "The Warden's warning points toward the throne road."
        )

        assertEquals(ObjectivePrimaryAction.NONE, summary.primaryAction)
    }
}
