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

    @Test
    fun returnsGrowthAndUnderinvestmentArchetypeData() {
        // Regression guard: GROWTH_AND_UNDERINVESTMENT used to be unimplemented in
        // RelationshipArchetypeEngine.initializeArchetype and would never actually reach here
        // in production, but the use case itself must not special-case it away.
        //
        // Uses the real enum constant rather than mock<ArchetypeType>(): Mockito's enum mocking
        // does not change which `when` branch an enum `when` matches on, so a mocked
        // ArchetypeType with a stubbed .name still resolves feedback loops for whichever
        // constant the mock happens to proxy underneath.
        val archetype = RelationshipArchetypeEngine.ArchetypeType.GROWTH_AND_UNDERINVESTMENT
        whenever(archetypeEngine.getActiveArchetypes()).thenReturn(mapOf("elena_player" to archetype))
        whenever(archetypeEngine.getStabilityReport()).thenReturn(mapOf("elena_player" to 0.6f))

        val result = useCase("elena")

        assertEquals("GROWTH AND UNDERINVESTMENT", result.activeArchetype)
        assertTrue(result.feedbackLoops.contains("Growth potential ↓"))
        assertTrue(result.feedbackLoops.contains("Investment ↓"))
    }
}
