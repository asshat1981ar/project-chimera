package com.chimera.feature.party

import com.chimera.domain.usecase.RelationshipDynamics
import com.chimera.database.entity.CharacterEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PartyIntegrationTest {

    @Test
    fun dispositionSnapshot_creation() {
        val snapshot = DispositionSnapshot(
            disposition = 0.5f,
            delta = 0.1f
        )

        assertEquals(0.5f, snapshot.disposition, 0.001f)
        assertEquals(0.1f, snapshot.delta, 0.001f)
        assertNotNull(snapshot.timestamp)
    }

    @Test
    fun relationshipDynamics_nullArchetype() {
        val dynamics = RelationshipDynamics(
            activeArchetype = null,
            stabilityIndex = 1.0f,
            feedbackLoops = emptyList(),
            archetypeDescription = ""
        )

        assertEquals(null, dynamics.activeArchetype)
        assertEquals(1.0f, dynamics.stabilityIndex)
    }

    @Test
    fun relationshipDynamics_withArchetype() {
        val dynamics = RelationshipDynamics(
            activeArchetype = "Escalation",
            stabilityIndex = 0.6f,
            feedbackLoops = listOf("Tension ↑", "Damage ↑"),
            archetypeDescription = "Competitive dynamics"
        )

        assertEquals("Escalation", dynamics.activeArchetype)
        assertTrue(dynamics.feedbackLoops.isNotEmpty())
    }

    @Test
    fun partyMember_includesNewFields() {
        val member = PartyMember(
            character = mockCharacterEntity(),
            state = null,
            dispositionHistory = listOf(DispositionSnapshot(disposition = 0.3f)),
            relationshipDynamics = RelationshipDynamics(
                activeArchetype = "Escalation",
                stabilityIndex = 0.6f,
                feedbackLoops = listOf("Tension ↑"),
                archetypeDescription = "Test"
            )
        )

        assertEquals(1, member.dispositionHistory.size)
        assertNotNull(member.relationshipDynamics)
    }

    private fun mockCharacterEntity() =
        CharacterEntity(
            id = "test",
            saveSlotId = 1L,
            name = "Test",
            role = "COMPANION",
            isPlayerCharacter = false
        )
}
