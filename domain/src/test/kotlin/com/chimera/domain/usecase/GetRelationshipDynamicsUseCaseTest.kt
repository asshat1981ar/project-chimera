package com.chimera.domain.usecase

import com.chimera.core.engine.RelationshipArchetypeEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRelationshipDynamicsUseCaseTest {

    private lateinit var archetypeEngine: RelationshipArchetypeEngine
    private lateinit var useCase: GetRelationshipDynamicsUseCase

    @Before
    fun setUp() {
        archetypeEngine = RelationshipArchetypeEngine()
        useCase = GetRelationshipDynamicsUseCase(archetypeEngine)
    }

    @Test
    fun returnsNullArchetypeWhenNoneActive() = runTest {
        val result = useCase("test_npc")

        assertEquals(null, result.activeArchetype)
        assertEquals(1.0f, result.stabilityIndex)
        assertTrue(result.feedbackLoops.isEmpty())
    }

    @Test
    fun returnsEscalationArchetypeData() = runTest {
        archetypeEngine.initializeArchetype(
            RelationshipArchetypeEngine.ArchetypeType.ESCALATION,
            "test_npc",
            "player"
        )

        val result = useCase("test_npc")

        assertEquals("ESCALATION", result.activeArchetype)
        assertTrue(result.feedbackLoops.isNotEmpty())
        assertTrue(result.archetypeDescription.isNotEmpty())
    }

    @Test
    fun feedbackLoopsMatchArchetypeType() = runTest {
        archetypeEngine.initializeArchetype(
            RelationshipArchetypeEngine.ArchetypeType.SHIFTING_THE_BURDEN,
            "warden",
            "player"
        )

        val result = useCase("warden")

        assertTrue(result.feedbackLoops.contains("Quick fixes ↑"))
        assertTrue(result.feedbackLoops.contains("Root cause ↓"))
    }
}
