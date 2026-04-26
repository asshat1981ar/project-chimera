package com.chimera.model

import kotlinx.serialization.Serializable

/**
 * Represents a narrative arc within the game's storyline.
 */
@Serializable
data class StoryArc(
    val id: String,
    val tag: String, // e.g. "hollow_awakening", "personal_redemption"
    val title: String,
    val description: String,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val priority: Int = 0
)

/**
 * A single beat (moment) within a story arc.
 */
@Serializable
data class StoryBeat(
    val id: String,
    val description: String,
    val type: String, // e.g. "world_event", "plot_hook", "character_moment"
    val consequence: String,
    val choices: List<String> = emptyList()
)

/**
 * Reward given on quest completion.
 */
@Serializable
data class Reward(
    val type: String, // e.g. "xp", "currency", "artifact", "ability"
    val amount: Int = 0,
    val itemId: String? = null
)
