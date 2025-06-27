package com.xai.chimera.service

import com.xai.chimera.api.DialogueApiService
import com.xai.chimera.api.DialogueRequest
import com.xai.chimera.api.DialogueResponse
import com.xai.chimera.dao.PlayerDao
import com.xai.chimera.domain.*
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * Simplified consciousness-aware DialogGPT service
 * Provides enhanced dialogue with basic consciousness features
 */
class ConsciousnessIntegratedDialogGPTService(
    private val apiService: DialogueApiService,
    private val playerDao: PlayerDao,
    private val emotionEngine: EmotionEngineService
) {
    
    companion object {
        private const val PROCESSING_TIMEOUT = 3000L
        private const val AWARENESS_THRESHOLD = 0.7f
    }
    
    private var conversationCounter = 0
    
    /**
     * Generate enhanced dialogue with basic consciousness awareness
     */
    suspend fun generateEnhancedDialogue(
        playerId: String,
        prompt: String,
        context: String
    ): EnhancedDialogueResponse = withTimeout(PROCESSING_TIMEOUT) {
        
        conversationCounter++
        
        val player = playerDao.getPlayer(playerId) ?: throw IllegalArgumentException("Player not found")
        
        // Simple context enhancement based on player history
        val enhancedContext = buildEnhancedContext(prompt, context, player)
        
        // Create dialogue request with player context
        val request = DialogueRequest(
            prompt = prompt,
            context = enhancedContext,
            options = mapOf(
                "emotions" to player.emotions,
                "personality" to mapOf(
                    "communication_style" to player.conversationPersonality.communicationStyle.name,
                    "curiosity_level" to player.conversationPersonality.curiosityLevel,
                    "emotional_openness" to player.conversationPersonality.emotionalOpenness
                )
            )
        )
        
        // Generate dialogue response
        val response = apiService.generateDialogue(request)
        val dialogueResponse = if (response.isSuccessful && response.body() != null) {
            response.body() ?: throw RuntimeException("Empty response body")
        } else {
            throw RuntimeException("Failed to generate dialogue: ${response.errorBody()?.string()}")
        }
        
        // Update player emotional state
        emotionEngine.updatePlayerEmotionalState(playerId, dialogueResponse)
        
        // Update player with new dialogue entry
        val updatedPlayer = updatePlayerWithDialogue(player, dialogueResponse)
        playerDao.updatePlayer(updatedPlayer)
        
        EnhancedDialogueResponse(
            response = dialogueResponse,
            awarenessLevel = calculateAwarenessLevel(player),
            conversationContext = enhancedContext,
            processingMetadata = SimpleProcessingMetadata(
                conversationCounter = conversationCounter,
                processingTime = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Get conversation insights for a player
     */
    suspend fun getConversationInsights(playerId: String): ConversationInsights {
        val player = playerDao.getPlayer(playerId) ?: throw IllegalArgumentException("Player not found")
        
        return ConversationInsights(
            emotionalTrends = analyzeEmotionalTrends(player.dialogueHistory),
            topicPreferences = extractTopicPreferences(player.dialogueHistory),
            conversationPatterns = identifyConversationPatterns(player.dialogueHistory),
            personalityGrowth = calculatePersonalityGrowth(player)
        )
    }
    
    // Private helper methods
    
    private fun buildEnhancedContext(
        prompt: String,
        context: String,
        player: Player
    ): String {
        val recentTopics = player.dialogueHistory.takeLast(3)
            .flatMap { it.topicTags }
            .distinct()
            .joinToString(", ")
        
        val emotionalContext = player.emotions.maxByOrNull { it.value }?.key ?: "neutral"
        
        return "$context | Recent topics: $recentTopics | Current mood: $emotionalContext"
    }
    
    private fun calculateAwarenessLevel(player: Player): Float {
        val emotionalIntensity = player.emotions.values.maxOrNull() ?: 0.5f
        val conversationDepth = when (player.conversationPersonality.preferredConversationDepth) {
            ConversationDepth.SURFACE -> 0.3f
            ConversationDepth.MEDIUM -> 0.6f
            ConversationDepth.DEEP -> 0.8f
            ConversationDepth.PHILOSOPHICAL -> 1.0f
        }
        
        return (emotionalIntensity + conversationDepth + player.conversationPersonality.curiosityLevel) / 3f
    }
    
    private fun updatePlayerWithDialogue(
        player: Player,
        dialogueResponse: DialogueResponse
    ): Player {
        val newEntry = DialogueEntry(
            id = "dialogue_${System.currentTimeMillis()}",
            text = dialogueResponse.text,
            timestamp = System.currentTimeMillis(),
            emotions = dialogueResponse.emotions,
            emotionalIntensity = dialogueResponse.emotions.values.maxOrNull() ?: 0.5f,
            topicTags = extractTopicTags(dialogueResponse.text)
        )
        
        return player.copy(
            emotions = dialogueResponse.emotions,
            dialogueHistory = (player.dialogueHistory + newEntry).takeLast(20)
        )
    }
    
    private fun analyzeEmotionalTrends(dialogueHistory: List<DialogueEntry>): Map<String, Float> {
        if (dialogueHistory.isEmpty()) return emptyMap()
        
        val emotionTrends = mutableMapOf<String, Float>()
        dialogueHistory.takeLast(10).forEach { entry ->
            entry.emotions.forEach { (emotion, intensity) ->
                emotionTrends[emotion] = (emotionTrends[emotion] ?: 0f) + intensity
            }
        }
        
        return emotionTrends.mapValues { it.value / min(10, dialogueHistory.size) }
    }
    
    private fun extractTopicPreferences(dialogueHistory: List<DialogueEntry>): Map<String, Int> {
        return dialogueHistory.flatMap { it.topicTags }
            .groupingBy { it }
            .eachCount()
    }
    
    private fun identifyConversationPatterns(dialogueHistory: List<DialogueEntry>): List<String> {
        val patterns = mutableListOf<String>()
        
        if (dialogueHistory.size >= 5) {
            val recentEmotionalIntensity = dialogueHistory.takeLast(5)
                .map { it.emotionalIntensity }
                .average()
            
            when {
                recentEmotionalIntensity > 0.7f -> patterns.add("high_emotional_engagement")
                recentEmotionalIntensity < 0.3f -> patterns.add("calm_conversation_style")
                else -> patterns.add("balanced_emotional_tone")
            }
        }
        
        return patterns
    }
    
    private fun calculatePersonalityGrowth(player: Player): Float {
        return player.memoryProfile.personalityEvolutionRate * 
               (player.dialogueHistory.size * 0.1f).coerceAtMost(1.0f)
    }
    
    private fun extractTopicTags(text: String): List<String> {
        val tags = mutableListOf<String>()
        val lowerText = text.lowercase()
        
        if (lowerText.contains("feel") || lowerText.contains("emotion")) tags.add("emotional")
        if (lowerText.contains("think") || lowerText.contains("consider")) tags.add("cognitive")
        if (lowerText.contains("wonder") || lowerText.contains("curious")) tags.add("exploratory")
        if (lowerText.contains("understand") || lowerText.contains("realize")) tags.add("insight")
        
        return tags
    }
}

// Simplified data classes

data class EnhancedDialogueResponse(
    val response: DialogueResponse,
    val awarenessLevel: Float,
    val conversationContext: String,
    val processingMetadata: SimpleProcessingMetadata
)

data class ConversationInsights(
    val emotionalTrends: Map<String, Float>,
    val topicPreferences: Map<String, Int>,
    val conversationPatterns: List<String>,
    val personalityGrowth: Float
)

data class SimpleProcessingMetadata(
    val conversationCounter: Int,
    val processingTime: Long
)