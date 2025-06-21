package com.xai.chimera.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Enhanced Player entity with consciousness-inspired features
 */
@Entity(tableName = "players")
data class Player(
    @PrimaryKey val id: String,
    val name: String,
    val emotions: Map<String, Float> = emptyMap(),
    val dialogueHistory: List<DialogueEntry> = emptyList(),
    val conversationPersonality: ConversationPersonality = ConversationPersonality(),
    val emotionalProfile: EmotionalProfile = EmotionalProfile(),
    val memoryProfile: MemoryProfile = MemoryProfile()
)

data class DialogueEntry(
    val id: String,
    val text: String,
    val timestamp: Long,
    val emotions: Map<String, Float> = emptyMap(),
    val conversationContext: ConversationContext = ConversationContext(),
    val emotionalIntensity: Float = 0.5f,
    val topicTags: List<String> = emptyList()
)

/**
 * Represents player's conversational personality that evolves over time
 */
data class ConversationPersonality(
    val communicationStyle: CommunicationStyle = CommunicationStyle.BALANCED,
    val curiosityLevel: Float = 0.5f,
    val emotionalOpenness: Float = 0.5f,
    val humorPreference: HumorStyle = HumorStyle.MILD,
    val preferredConversationDepth: ConversationDepth = ConversationDepth.MEDIUM,
    val topicalInterests: Map<String, Float> = emptyMap()
)

/**
 * Emotional intelligence and patterns
 */
data class EmotionalProfile(
    val emotionalStability: Float = 0.5f,
    val empathyLevel: Float = 0.5f,
    val emotionalGrowthRate: Float = 0.1f,
    val predominantEmotions: List<String> = emptyList(),
    val emotionalPatterns: Map<String, List<Float>> = emptyMap()
)

/**
 * Memory and learning patterns
 */
data class MemoryProfile(
    val memoryRetentionRate: Float = 0.7f,
    val importantMemories: List<String> = emptyList(),
    val learningStyle: LearningStyle = LearningStyle.BALANCED,
    val personalityEvolutionRate: Float = 0.05f
)

/**
 * Context for individual conversations
 */
data class ConversationContext(
    val conversationGoal: String = "",
    val mood: String = "neutral",
    val relationshipDepth: Float = 0.0f,
    val previousTopicReferences: List<String> = emptyList()
)

enum class CommunicationStyle {
    DIRECT, DIPLOMATIC, EMPATHETIC, ANALYTICAL, BALANCED
}

enum class HumorStyle {
    NONE, MILD, WITTY, PLAYFUL, SARCASTIC
}

enum class ConversationDepth {
    SURFACE, MEDIUM, DEEP, PHILOSOPHICAL
}

enum class LearningStyle {
    EXPERIENTIAL, ANALYTICAL, SOCIAL, BALANCED
}
