package com.chimera.domain.usecase

import com.chimera.core.engine.RelationshipArchetypeEngine
import javax.inject.Inject
import javax.inject.Singleton

data class RelationshipDynamics(
    val activeArchetype: String?,
    val stabilityIndex: Float,
    val feedbackLoops: List<String>,
    val archetypeDescription: String
)

@Singleton
class GetRelationshipDynamicsUseCase @Inject constructor(
    private val archetypeEngine: RelationshipArchetypeEngine
) {
    companion object {
        private const val DEFAULT_PLAYER_ID = "player"
    }

    operator fun invoke(npcId: String, playerId: String = DEFAULT_PLAYER_ID): RelationshipDynamics {
        val archetypes = archetypeEngine.getActiveArchetypes()
        val key = archetypes.keys.find { it.startsWith("${npcId}_") }

        if (key == null) {
            return RelationshipDynamics(
                activeArchetype = null,
                stabilityIndex = 1.0f,
                feedbackLoops = emptyList(),
                archetypeDescription = ""
            )
        }

        val type = archetypes[key] ?: return RelationshipDynamics(
            activeArchetype = null,
            stabilityIndex = 1.0f,
            feedbackLoops = emptyList(),
            archetypeDescription = ""
        )
        val stability = archetypeEngine.getStabilityReport()[key] ?: 1.0f

        return RelationshipDynamics(
            activeArchetype = type.name.replace("_", " "),
            stabilityIndex = stability,
            feedbackLoops = generateFeedbackLoops(type),
            archetypeDescription = type.description
        )
    }

    private fun generateFeedbackLoops(type: RelationshipArchetypeEngine.ArchetypeType): List<String> {
        return when (type) {
            RelationshipArchetypeEngine.ArchetypeType.SHIFTING_THE_BURDEN ->
                listOf("Quick fixes ↑", "Root cause ↓", "Dependency ↑")
            RelationshipArchetypeEngine.ArchetypeType.ESCALATION ->
                listOf("Tension ↑", "Relationship damage ↑", "Retaliation cycle")
            RelationshipArchetypeEngine.ArchetypeType.GROWTH_AND_UNDERINVESTMENT ->
                listOf("Growth potential ↓", "Investment ↓")
            RelationshipArchetypeEngine.ArchetypeType.FIXES_THAT_FAIL ->
                listOf("Problem ↓ (temporary)", "Side effects ↑")
        }
    }
}
