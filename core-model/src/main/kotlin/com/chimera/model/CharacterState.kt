package com.chimera.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterState(
    val characterId: String,
    val saveSlotId: Long,
    val healthFraction: Float = 1.0f,
    val dispositionToPlayer: Float = 0.0f,
    val emotionalState: Map<String, Float> = emptyMap(),
    val activeArchetype: String? = null,
    val archetypeVariables: Map<String, Float> = emptyMap(),
    val lastInteractionEpoch: Long = 0L
) {
    companion object {
        const val DISPOSITION_MIN = -1.0f
        const val DISPOSITION_MAX = 1.0f
    }
}
