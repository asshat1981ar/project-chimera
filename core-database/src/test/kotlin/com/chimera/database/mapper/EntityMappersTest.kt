package com.chimera.database.mapper

import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.MemoryShardEntity
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.model.Character
import com.chimera.model.CharacterRole
import com.chimera.model.CharacterState
import com.chimera.model.MemoryShard
import com.chimera.model.SaveSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityMappersTest {

    @Test
    fun `SaveSlotEntity round trip preserves all fields`() {
        val model = SaveSlot(
            id = 1, slotIndex = 0, playerName = "Arthas",
            chapterTag = "act_1", playtimeSeconds = 3600,
            lastPlayedAt = 1000L, createdAt = 500L, isEmpty = false
        )
        val entity = model.toEntity()
        val restored = entity.toModel()
        assertEquals(model, restored)
    }

    @Test
    fun `CharacterEntity round trip preserves all fields`() {
        val model = Character(
            id = "npc_1", saveSlotId = 1, name = "Elena",
            title = "The Merchant", role = CharacterRole.NPC_ALLY,
            isPlayerCharacter = false, portraitResName = "elena_portrait"
        )
        val entity = model.toEntity()
        val restored = entity.toModel()
        assertEquals(model, restored)
    }

    @Test
    fun `CharacterEntity handles invalid role gracefully`() {
        val entity = CharacterEntity(
            id = "npc_bad", saveSlotId = 1, name = "Unknown",
            role = "INVALID_ROLE"
        )
        val model = entity.toModel()
        assertEquals(CharacterRole.NPC_NEUTRAL, model.role)
    }

    @Test
    fun `CharacterStateEntity clamps disposition to valid range`() {
        val entity = CharacterStateEntity(
            characterId = "npc_1", saveSlotId = 1,
            dispositionToPlayer = 5.0f, // out of range
            healthFraction = 2.0f // out of range
        )
        val model = entity.toModel()
        assertEquals(1.0f, model.dispositionToPlayer, 0.001f)
        assertEquals(1.0f, model.healthFraction, 0.001f)
    }

    @Test
    fun `CharacterStateEntity clamps negative disposition`() {
        val entity = CharacterStateEntity(
            characterId = "npc_1", saveSlotId = 1,
            dispositionToPlayer = -5.0f
        )
        val model = entity.toModel()
        assertEquals(-1.0f, model.dispositionToPlayer, 0.001f)
    }

    @Test
    fun `CharacterState round trip preserves emotional state`() {
        val model = CharacterState(
            characterId = "npc_1", saveSlotId = 1,
            emotionalState = mapOf("joy" to 0.8f, "fear" to 0.2f),
            archetypeVariables = mapOf("dependency" to 0.5f)
        )
        val entity = model.toEntity()
        val restored = entity.toModel()
        assertEquals(model.emotionalState, restored.emotionalState)
        assertEquals(model.archetypeVariables, restored.archetypeVariables)
    }

    @Test
    fun `MemoryShardEntity toModel maps all fields`() {
        val entity = MemoryShardEntity(
            id = 42, saveSlotId = 1, sceneId = "scene_1",
            characterId = "npc_1", summary = "Player showed kindness",
            importanceScore = 0.9f, createdAt = 12345L
        )
        val model = entity.toModel()
        assertEquals(42L, model.id)
        assertEquals(1L, model.saveSlotId)
        assertEquals("scene_1", model.sceneId)
        assertEquals("npc_1", model.characterId)
        assertEquals("Player showed kindness", model.summary)
        assertEquals(0.9f, model.importanceScore, 0.001f)
        assertEquals(12345L, model.createdAt)
    }

    @Test
    fun `MemoryShard round trip preserves core fields`() {
        val model = MemoryShard(
            id = 10, saveSlotId = 1, sceneId = "s1",
            characterId = "c1", summary = "Test memory",
            importanceScore = 0.7f, createdAt = 9999L
        )
        val entity = model.toEntity()
        val restored = entity.toModel()
        assertEquals(model.id, restored.id)
        assertEquals(model.summary, restored.summary)
        assertEquals(model.importanceScore, restored.importanceScore, 0.001f)
    }

    @Test
    fun `CharacterStateEntity handles empty JSON strings`() {
        val entity = CharacterStateEntity(
            characterId = "npc_1", saveSlotId = 1,
            emotionalStateJson = "{}",
            archetypeVariablesJson = "{}"
        )
        val model = entity.toModel()
        assertTrue(model.emotionalState.isEmpty())
        assertTrue(model.archetypeVariables.isEmpty())
    }
}
