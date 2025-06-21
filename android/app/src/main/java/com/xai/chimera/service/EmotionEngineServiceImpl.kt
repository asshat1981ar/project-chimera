package com.xai.chimera.service

import com.xai.chimera.api.DialogueResponse
import com.xai.chimera.domain.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * Enhanced EmotionEngine with consciousness-inspired features
 * Focuses on realistic emotional intelligence rather than abstract consciousness
 */
class EmotionEngineServiceImpl : EmotionEngineService {
    
    companion object {
        private const val EMOTION_DECAY_RATE = 0.95f
        private const val LEARNING_RATE = 0.1f
        private const val PERSONALITY_EVOLUTION_THRESHOLD = 0.05f
    }

    override suspend fun analyzeEmotions(text: String): Map<String, Float> {
        // Simulate emotion analysis with delay to represent processing
        delay(100)
        
        return analyzeTextForEmotions(text)
    }

    override suspend fun updatePlayerEmotionalState(
        playerId: String,
        dialogueResponse: DialogueResponse
    ) {
        // This would typically update database
        // For now, we'll demonstrate the logic
        
        val emotions = analyzeEmotions(dialogueResponse.text)
        
        // Apply emotional learning and adaptation
        applyEmotionalLearning(playerId, emotions, dialogueResponse.text)
    }

    override suspend fun getRecommendedEmotions(
        playerId: String,
        context: String
    ): Map<String, Float> {
        // Provide contextually appropriate emotional recommendations
        return generateContextualEmotions(context)
    }
    
    /**
     * Advanced: Analyze player's emotional journey over time
     */
    suspend fun analyzeEmotionalJourney(
        player: Player,
        timeWindowDays: Int = 7
    ): EmotionalJourneyInsight {
        val recentEntries = player.dialogueHistory.filter { entry ->
            val daysSince = (System.currentTimeMillis() - entry.timestamp) / (1000 * 60 * 60 * 24)
            daysSince <= timeWindowDays
        }
        
        return EmotionalJourneyInsight(
            emotionalTrend = calculateEmotionalTrend(recentEntries),
            stabilityScore = calculateEmotionalStability(recentEntries),
            growthIndicators = identifyGrowthIndicators(recentEntries),
            recommendedFocus = generateEmotionalRecommendations(recentEntries)
        )
    }
    
    /**
     * Evolve player personality based on conversation patterns
     */
    suspend fun evolvePersonality(
        currentPersonality: ConversationPersonality,
        recentInteractions: List<DialogueEntry>
    ): ConversationPersonality {
        val interactionPatterns = analyzeInteractionPatterns(recentInteractions)
        
        return currentPersonality.copy(
            curiosityLevel = adaptCuriosity(currentPersonality.curiosityLevel, interactionPatterns),
            emotionalOpenness = adaptEmotionalOpenness(currentPersonality.emotionalOpenness, interactionPatterns),
            communicationStyle = adaptCommunicationStyle(currentPersonality.communicationStyle, interactionPatterns),
            topicalInterests = evolveTopicalInterests(currentPersonality.topicalInterests, recentInteractions)
        )
    }
    
    /**
     * Generate conversation recommendations based on emotional state
     */
    suspend fun generateConversationRecommendations(
        player: Player
    ): ConversationRecommendations {
        val emotionalState = player.emotions
        val personality = player.conversationPersonality
        
        return ConversationRecommendations(
            suggestedTopics = generateTopicSuggestions(emotionalState, personality),
            conversationStyle = recommendConversationStyle(emotionalState, personality),
            emotionalSupport = assessEmotionalSupportNeeds(emotionalState),
            engagementLevel = calculateOptimalEngagementLevel(emotionalState, personality)
        )
    }
    
    // Private helper methods
    
    private fun analyzeTextForEmotions(text: String): Map<String, Float> {
        val emotions = mutableMapOf<String, Float>()
        
        // Simple keyword-based emotion detection (would be replaced with ML model)
        val emotionKeywords = mapOf(
            "joy" to listOf("happy", "excited", "wonderful", "great", "love", "amazing"),
            "sadness" to listOf("sad", "disappointed", "hurt", "lonely", "depressed"),
            "anger" to listOf("angry", "frustrated", "mad", "annoyed", "furious"),
            "fear" to listOf("scared", "afraid", "worried", "anxious", "nervous"),
            "surprise" to listOf("surprised", "shocked", "amazed", "unexpected"),
            "trust" to listOf("trust", "confident", "reliable", "secure", "safe"),
            "anticipation" to listOf("excited", "eager", "looking forward", "hope")
        )
        
        val lowerText = text.lowercase()
        
        emotionKeywords.forEach { (emotion, keywords) ->
            val matches = keywords.count { keyword -> lowerText.contains(keyword) }
            if (matches > 0) {
                emotions[emotion] = min(1.0f, matches * 0.3f + (Math.random() * 0.2f).toFloat())
            }
        }
        
        // Ensure we always have some baseline emotions
        if (emotions.isEmpty()) {
            emotions["neutral"] = 0.5f
        }
        
        return emotions
    }
    
    private suspend fun applyEmotionalLearning(
        playerId: String,
        emotions: Map<String, Float>,
        text: String
    ) {
        // Simulate emotional learning and adaptation
        // In real implementation, this would update the player's emotional profile
        delay(50)
    }
    
    private fun generateContextualEmotions(context: String): Map<String, Float> {
        // Generate appropriate emotions based on context
        return when {
            context.contains("achievement") -> mapOf("joy" to 0.8f, "pride" to 0.7f)
            context.contains("challenge") -> mapOf("determination" to 0.6f, "anticipation" to 0.5f)
            context.contains("loss") -> mapOf("sadness" to 0.6f, "acceptance" to 0.4f)
            else -> mapOf("curiosity" to 0.5f, "openness" to 0.4f)
        }
    }
    
    private fun calculateEmotionalTrend(entries: List<DialogueEntry>): EmotionalTrend {
        if (entries.isEmpty()) return EmotionalTrend.STABLE
        
        val intensities = entries.map { it.emotionalIntensity }
        val trend = (intensities.last() - intensities.first()) / intensities.size
        
        return when {
            trend > 0.1f -> EmotionalTrend.IMPROVING
            trend < -0.1f -> EmotionalTrend.DECLINING
            else -> EmotionalTrend.STABLE
        }
    }
    
    private fun calculateEmotionalStability(entries: List<DialogueEntry>): Float {
        if (entries.size < 2) return 0.5f
        
        val intensities = entries.map { it.emotionalIntensity }
        val variance = intensities.map { intensity ->
            val mean = intensities.average()
            (intensity - mean) * (intensity - mean)
        }.average()
        
        return max(0f, 1f - variance.toFloat())
    }
    
    private fun identifyGrowthIndicators(entries: List<DialogueEntry>): List<GrowthIndicator> {
        val indicators = mutableListOf<GrowthIndicator>()
        
        // Analyze for increasing emotional vocabulary
        val emotionVariety = entries.flatMap { it.emotions.keys }.distinct().size
        if (emotionVariety > 5) {
            indicators.add(GrowthIndicator.EMOTIONAL_VOCABULARY_EXPANSION)
        }
        
        // Analyze for increasing conversation depth
        val depthScores = entries.map { it.topicTags.size }
        if (depthScores.isNotEmpty() && depthScores.last() > depthScores.first()) {
            indicators.add(GrowthIndicator.CONVERSATION_DEPTH_INCREASE)
        }
        
        return indicators
    }
    
    private fun generateEmotionalRecommendations(entries: List<DialogueEntry>): List<String> {
        val recommendations = mutableListOf<String>()
        
        val predominantEmotions = entries.flatMap { it.emotions.keys }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
        
        predominantEmotions?.let { (emotion, _) ->
            when (emotion) {
                "sadness" -> recommendations.add("Consider exploring activities that bring joy")
                "anger" -> recommendations.add("Practice emotional regulation techniques")
                "fear" -> recommendations.add("Gradual exposure to comfortable challenges")
                else -> recommendations.add("Continue exploring emotional depth")
            }
        }
        
        return recommendations
    }
    
    private fun analyzeInteractionPatterns(interactions: List<DialogueEntry>): InteractionPatterns {
        return InteractionPatterns(
            averageResponseLength = interactions.map { it.text.length }.average(),
            topicDiversity = interactions.flatMap { it.topicTags }.distinct().size,
            emotionalRange = interactions.flatMap { it.emotions.values }.let { values ->
                if (values.isNotEmpty()) values.max() - values.min() else 0.0
            }.toFloat(),
            conversationInitiation = interactions.count { it.text.endsWith("?") }.toFloat() / interactions.size
        )
    }
    
    private fun adaptCuriosity(current: Float, patterns: InteractionPatterns): Float {
        val adjustment = if (patterns.conversationInitiation > 0.3f) 0.1f else -0.05f
        return (current + adjustment * LEARNING_RATE).coerceIn(0f, 1f)
    }
    
    private fun adaptEmotionalOpenness(current: Float, patterns: InteractionPatterns): Float {
        val adjustment = if (patterns.emotionalRange > 0.5f) 0.1f else -0.05f
        return (current + adjustment * LEARNING_RATE).coerceIn(0f, 1f)
    }
    
    private fun adaptCommunicationStyle(
        current: CommunicationStyle,
        patterns: InteractionPatterns
    ): CommunicationStyle {
        // Communication style evolves based on successful interaction patterns
        return when {
            patterns.averageResponseLength > 100 && patterns.topicDiversity > 3 -> CommunicationStyle.ANALYTICAL
            patterns.emotionalRange > 0.6f -> CommunicationStyle.EMPATHETIC
            patterns.conversationInitiation > 0.4f -> CommunicationStyle.DIRECT
            else -> current
        }
    }
    
    private fun evolveTopicalInterests(
        current: Map<String, Float>,
        interactions: List<DialogueEntry>
    ): Map<String, Float> {
        val newInterests = current.toMutableMap()
        
        interactions.forEach { entry ->
            entry.topicTags.forEach { topic ->
                val currentInterest = newInterests.getOrDefault(topic, 0f)
                newInterests[topic] = min(1f, currentInterest + 0.1f)
            }
        }
        
        // Decay unused interests
        newInterests.replaceAll { _, value -> value * EMOTION_DECAY_RATE }
        
        return newInterests.filter { it.value > 0.1f }
    }
    
    private fun generateTopicSuggestions(
        emotions: Map<String, Float>,
        personality: ConversationPersonality
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Suggest topics based on current emotional state
        emotions.maxByOrNull { it.value }?.let { (emotion, _) ->
            when (emotion) {
                "joy" -> suggestions.addAll(listOf("achievements", "future_plans", "gratitude"))
                "curiosity" -> suggestions.addAll(listOf("learning", "exploration", "discovery"))
                "contemplation" -> suggestions.addAll(listOf("philosophy", "meaning", "reflection"))
            }
        }
        
        // Add personality-based suggestions
        when (personality.communicationStyle) {
            CommunicationStyle.ANALYTICAL -> suggestions.addAll(listOf("problem_solving", "systems_thinking"))
            CommunicationStyle.EMPATHETIC -> suggestions.addAll(listOf("relationships", "emotional_growth"))
            else -> suggestions.addAll(listOf("daily_life", "interests"))
        }
        
        return suggestions.distinct()
    }
    
    private fun recommendConversationStyle(
        emotions: Map<String, Float>,
        personality: ConversationPersonality
    ): ConversationStyleRecommendation {
        val dominantEmotion = emotions.maxByOrNull { it.value }?.key ?: "neutral"
        
        return ConversationStyleRecommendation(
            recommendedDepth = when (dominantEmotion) {
                "contemplation", "sadness" -> ConversationDepth.DEEP
                "excitement", "joy" -> ConversationDepth.MEDIUM
                else -> personality.preferredConversationDepth
            },
            recommendedTone = when (dominantEmotion) {
                "anger", "frustration" -> ConversationTone.CALM_SUPPORTIVE
                "sadness" -> ConversationTone.GENTLE_EMPATHETIC
                "joy" -> ConversationTone.ENTHUSIASTIC
                else -> ConversationTone.BALANCED
            },
            suggestedApproach = generateConversationApproach(emotions, personality)
        )
    }
    
    private fun assessEmotionalSupportNeeds(emotions: Map<String, Float>): EmotionalSupportAssessment {
        val negativeEmotions = emotions.filter { (emotion, _) ->
            emotion in listOf("sadness", "anger", "fear", "frustration", "loneliness")
        }
        
        val supportLevel = when {
            negativeEmotions.values.maxOrNull() ?: 0f > 0.7f -> SupportLevel.HIGH
            negativeEmotions.values.maxOrNull() ?: 0f > 0.4f -> SupportLevel.MODERATE
            else -> SupportLevel.LOW
        }
        
        return EmotionalSupportAssessment(
            supportLevel = supportLevel,
            recommendedInterventions = generateSupportRecommendations(negativeEmotions),
            encouragementLevel = calculateEncouragementLevel(emotions)
        )
    }
    
    private fun calculateOptimalEngagementLevel(
        emotions: Map<String, Float>,
        personality: ConversationPersonality
    ): Float {
        val emotionalEnergy = emotions.values.sum()
        val personalityFactor = personality.curiosityLevel * 0.5f + personality.emotionalOpenness * 0.5f
        
        return (emotionalEnergy * 0.6f + personalityFactor * 0.4f).coerceIn(0.2f, 1.0f)
    }
    
    private fun generateConversationApproach(
        emotions: Map<String, Float>,
        personality: ConversationPersonality
    ): ConversationApproach {
        return ConversationApproach(
            questioningStyle = when (personality.curiosityLevel) {
                in 0.7f..1.0f -> QuestioningStyle.EXPLORATORY
                in 0.4f..0.7f -> QuestioningStyle.GUIDED
                else -> QuestioningStyle.MINIMAL
            },
            responseStyle = when (personality.communicationStyle) {
                CommunicationStyle.DIRECT -> ResponseStyle.CONCISE
                CommunicationStyle.EMPATHETIC -> ResponseStyle.REFLECTIVE
                CommunicationStyle.ANALYTICAL -> ResponseStyle.DETAILED
                else -> ResponseStyle.BALANCED
            },
            emotionalMirroring = calculateEmotionalMirroring(emotions, personality)
        )
    }
    
    private fun generateSupportRecommendations(negativeEmotions: Map<String, Float>): List<String> {
        return negativeEmotions.keys.flatMap { emotion ->
            when (emotion) {
                "sadness" -> listOf("emotional_validation", "gentle_encouragement")
                "anger" -> listOf("emotional_regulation", "perspective_taking")
                "fear" -> listOf("reassurance", "gradual_exposure")
                "frustration" -> listOf("problem_solving", "patience_practice")
                else -> listOf("general_support")
            }
        }.distinct()
    }
    
    private fun calculateEncouragementLevel(emotions: Map<String, Float>): Float {
        val positiveEmotions = emotions.filter { (emotion, _) ->
            emotion in listOf("joy", "excitement", "confidence", "pride", "hope")
        }
        
        return if (positiveEmotions.isEmpty()) 0.7f else positiveEmotions.values.average().toFloat()
    }
    
    private fun calculateEmotionalMirroring(
        emotions: Map<String, Float>,
        personality: ConversationPersonality
    ): Float {
        return personality.emotionalOpenness * 0.8f + 0.2f
    }
}

// Supporting data classes for enhanced emotion engine

data class EmotionalJourneyInsight(
    val emotionalTrend: EmotionalTrend,
    val stabilityScore: Float,
    val growthIndicators: List<GrowthIndicator>,
    val recommendedFocus: List<String>
)

data class ConversationRecommendations(
    val suggestedTopics: List<String>,
    val conversationStyle: ConversationStyleRecommendation,
    val emotionalSupport: EmotionalSupportAssessment,
    val engagementLevel: Float
)

data class ConversationStyleRecommendation(
    val recommendedDepth: ConversationDepth,
    val recommendedTone: ConversationTone,
    val suggestedApproach: ConversationApproach
)

data class EmotionalSupportAssessment(
    val supportLevel: SupportLevel,
    val recommendedInterventions: List<String>,
    val encouragementLevel: Float
)

data class ConversationApproach(
    val questioningStyle: QuestioningStyle,
    val responseStyle: ResponseStyle,
    val emotionalMirroring: Float
)

data class InteractionPatterns(
    val averageResponseLength: Double,
    val topicDiversity: Int,
    val emotionalRange: Float,
    val conversationInitiation: Float
)

enum class EmotionalTrend {
    IMPROVING, STABLE, DECLINING
}

enum class GrowthIndicator {
    EMOTIONAL_VOCABULARY_EXPANSION,
    CONVERSATION_DEPTH_INCREASE,
    EMPATHY_DEVELOPMENT,
    SELF_AWARENESS_GROWTH
}

enum class ConversationTone {
    ENTHUSIASTIC, CALM_SUPPORTIVE, GENTLE_EMPATHETIC, BALANCED, PROFESSIONAL
}

enum class SupportLevel {
    LOW, MODERATE, HIGH
}

enum class QuestioningStyle {
    MINIMAL, GUIDED, EXPLORATORY
}

enum class ResponseStyle {
    CONCISE, BALANCED, DETAILED, REFLECTIVE
}