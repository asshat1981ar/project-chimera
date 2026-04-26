package com.chimera.ai

import com.chimera.model.Quest
import com.chimera.model.StoryArc
import com.chimera.model.StoryBeat

/**
 * Generates procedural storylines, quests, and narrative beats.
 * Can be backed by local rules or an AI model.
 */
interface StorylineGenerator {

    /**
     * Generate a quest chain appropriate for the current world state and player level.
     */
    suspend fun generateQuestChain(
        context: QuestGenerationContext
    ): List<Quest>

    /**
     * Generate the next narrative beat based on what just happened.
     */
    suspend fun generateStoryBeat(
        context: StoryBeatContext
    ): StoryBeat

    /**
     * True if the generator is available to serve requests.
     */
    suspend fun isAvailable(): Boolean
}

data class QuestGenerationContext(
    val playerLevel: Int,
    val regionTags: List<String>,
    val activeQuestIds: List<String>,
    val completedQuestIds: List<String>,
    val recentEvents: List<String>,
    val tone: String = "dark fantasy"
)

data class StoryBeatContext(
    val currentArc: StoryArc,
    val recentChoice: String,
    val playerDisposition: Map<String, Float>,
    val worldState: Map<String, Any>
)
