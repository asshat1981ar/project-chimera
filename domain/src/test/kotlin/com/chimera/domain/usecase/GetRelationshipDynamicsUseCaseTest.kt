package com.chimera.domain.usecase

import com.chimera.core.engine.RelationshipArchetypeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetRelationshipDynamicsUseCaseTest {

    private lateinit var archetypeEngine: RelationshipArchetypeEngine
    private lateinit var useCase: GetRelationshipDynamicsUseCase

    @Before
    fun setUp() {
        archetypeEngine = mock()
        useCase = GetRelationshipDynamicsUseCase(archetypeEngine)
    }

    @Test
    fun returnsNullArchetypeWhenNoneActive() {
        whenever(archetypeEngine.getActiveArchetypes()).thenReturn(emptyMap())

        val result = useCase("test_npc")

        assertEquals(null, result.activeArchetype)
        assertEquals(1.0f, result.stabilityIndex)
        assertTrue(result.feedbackLoops.isEmpty())
    }

    @Test
    fun returnsEscalationArchetypeData() {
        val mockArchetype = mock<RelationshipArchetypeEngine.ArchetypeType>()
        whenever(mockArchetype.name).thenReturn("ESCALATION")
        whenever(mockArchetype.description).thenReturn("Escalation description")
        whenever(archetypeEngine.getActiveArchetypes()).thenReturn(mapOf("test_npc_player" to mockArchetype))
        whenever(archetypeEngine.getStabilityReport()).thenReturn(mapOf("test_npc_player" to 0.8f))

        val result = useCase("test_npc")

        assertEquals("ESCALATION", result.activeArchetype)
        assertTrue(result.feedbackLoops.isNotEmpty())
        assertTrue(result.archetypeDescription.isNotEmpty())
    }

    @Test
    fun feedbackLoopsMatchArchetypeType() {
        val mockArchetype = mock<RelationshipArchetypeEngine.ArchetypeType>()
        whenever(mockArchetype.name).thenReturn("SHIFTING_THE_BURDEN")
        whenever(mockArchetype.description).thenReturn("Shifting the burden description")
        whenever(archetypeEngine.getActiveArchetypes()).thenReturn(mapOf("warden_player" to mockArchetype))
        whenever(archetypeEngine.getStabilityReport()).thenReturn(emptyMap())

        val result = useCase("warden")

        assertTrue(result.feedbackLoops.contains("Quick fixes ↑"))
        assertTrue(result.feedbackLoops.contains("Root cause ↓"))
    }
}
