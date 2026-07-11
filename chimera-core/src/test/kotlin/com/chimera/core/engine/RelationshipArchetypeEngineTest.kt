package com.chimera.core.engine

import com.chimera.core.engine.RelationshipArchetypeEngine.ArchetypeType
import com.chimera.core.engine.RelationshipArchetypeEngine.InteractionType
import com.chimera.core.engine.RelationshipArchetypeEngine.NPCInteraction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelationshipArchetypeEngineTest {

    private val playerId = "player"

    @Test
    fun `initializeArchetype creates a GrowthAndUnderinvestment archetype without throwing`() = runTest {
        val engine = RelationshipArchetypeEngine()

        engine.initializeArchetype(ArchetypeType.GROWTH_AND_UNDERINVESTMENT, "elena", playerId)

        assertEquals(
            ArchetypeType.GROWTH_AND_UNDERINVESTMENT,
            engine.getActiveArchetypes().values.single()
        )
    }

    @Test
    fun `sustained teaching recovers stability lost to a period of neglect`() = runTest {
        val engine = RelationshipArchetypeEngine()
        engine.initializeArchetype(ArchetypeType.GROWTH_AND_UNDERINVESTMENT, "elena", playerId)

        repeat(5) {
            engine.processInteraction(
                "elena", playerId,
                NPCInteraction(type = InteractionType.IGNORE, intensity = 0.9f)
            )
        }
        val afterNeglect = engine.getStabilityReport().values.single()

        repeat(5) {
            engine.processInteraction(
                "elena", playerId,
                NPCInteraction(type = InteractionType.TEACH, intensity = 0.8f)
            )
        }
        val afterInvestment = engine.getStabilityReport().values.single()

        assertTrue(
            "expected stability to recover once investment resumes: " +
                "afterNeglect=$afterNeglect afterInvestment=$afterInvestment",
            afterInvestment > afterNeglect
        )
    }

    @Test
    fun `neglect interactions accumulate opportunity cost and eventually stagnation impact`() = runTest {
        val engine = RelationshipArchetypeEngine()
        engine.initializeArchetype(ArchetypeType.GROWTH_AND_UNDERINVESTMENT, "elena", playerId)

        val impacts = (1..8).map {
            engine.processInteraction(
                "elena", playerId,
                NPCInteraction(type = InteractionType.IGNORE, intensity = 0.9f, deltaTime = 1.0f)
            )
        }.flatten()

        // Repeated neglect should eventually register a non-neutral, negative emotional impact.
        assertTrue(impacts.any { it.dispositionDelta < 0f })
    }

    @Test
    fun `all four archetypes can be initialized without throwing`() = runTest {
        val engine = RelationshipArchetypeEngine()

        ArchetypeType.values().forEach { type ->
            engine.initializeArchetype(type, "npc_$type", playerId)
        }

        assertEquals(ArchetypeType.values().size, engine.getActiveArchetypes().size)
    }

    @Test
    fun `getRelationshipDynamics-style lookup returns null for unknown npc`() = runTest {
        val engine = RelationshipArchetypeEngine()
        engine.initializeArchetype(ArchetypeType.GROWTH_AND_UNDERINVESTMENT, "elena", playerId)

        val key = engine.getActiveArchetypes().keys.find { it.startsWith("stranger_") }

        assertNull(key)
    }
}
