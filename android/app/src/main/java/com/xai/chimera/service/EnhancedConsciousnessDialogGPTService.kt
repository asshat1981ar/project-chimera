package com.xai.chimera.service

import android.content.Context
import com.xai.chimera.api.DialogueApiService
import com.xai.chimera.api.DialogueRequest
import com.xai.chimera.api.DialogueResponse
import com.xai.chimera.dao.PlayerDao
import com.xai.chimera.domain.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

/**
 * Enhanced consciousness-aware DialogGPT service with automated metrics collection
 * and analysis integration
 */
class EnhancedConsciousnessDialogGPTService(
    private val context: Context,
    private val apiService: DialogueApiService,
    private val playerDao: PlayerDao,
    private val emotionEngine: EmotionEngineService
) {
    
    companion object {
        private const val PROCESSING_TIMEOUT = 3000L
        private const val AWARENESS_THRESHOLD = 0.7f
        private const val DEFAULT_CHUNK_SIZE = 1024
    }
    
    // Integrated analysis system
    private val integratedAnalysisService by lazy { 
        IntegratedAnalysisService(context) 
    }
    
    private var conversationCounter = 0
    
    /**
     * Initialize the service with automated analysis
     */
    suspend fun initialize(enableAnalysis: Boolean = true) {
        if (enableAnalysis) {
            integratedAnalysisService.initializeAnalysisSystem(
                enablePeriodicAnalysis = true,
                periodicAnalysisHours = 6L
            )
        }
    }
    
    /**
     * Generate enhanced dialogue with integrated metrics collection and analysis
     */
    suspend fun generateEnhancedDialogue(
        playerId: String,
        prompt: String,
        context: String,
        chunkSize: Int = DEFAULT_CHUNK_SIZE
    ): EnhancedDialogueResponse = withTimeout(PROCESSING_TIMEOUT) {
        
        // Use integrated metrics collection for the entire operation
        return@withTimeout integratedAnalysisService.executeWithMetrics(
            operation = "enhanced_dialogue_generation",
            chunkSize = chunkSize
        ) {
            performDialogueGeneration(playerId, prompt, context)
        }
    }
    
    /**
     * Internal dialogue generation with detailed metrics tracking
     */
    private suspend fun performDialogueGeneration(
        playerId: String,
        prompt: String,
        context: String
    ): EnhancedDialogueResponse {
        
        conversationCounter++
        
        val player = playerDao.getPlayer(playerId) ?: throw IllegalArgumentException("Player not found")
        
        // Measure emotion processing time
        var emotionProcessingTime = 0L
        val updatedEmotions = measureTimeMillis {
            emotionProcessingTime = measureTimeMillis {
                emotionEngine.updatePlayerEmotions(player, prompt)
            }
        }
        
        // Apply dynamic adjustments from analysis
        val adjustments = integratedAnalysisService.getStoredAdjustments()
        val adjustedChunkSize = applyChunkSizeAdjustment(DEFAULT_CHUNK_SIZE, adjustments)
        val timeoutMultiplier = adjustments["timeout_multiplier"] ?: 1.0f
        
        // Measure consciousness processing
        var consciousnessUpdateTime = 0L
        val awarenessLevel = measureTimeMillis {
            consciousnessUpdateTime = measureTimeMillis {
                calculateAwarenessLevel(player)
            }
        }
        
        // Enhanced context with consciousness awareness
        val enhancedContext = buildEnhancedContext(prompt, context, player, awarenessLevel)
        
        // Measure API response time
        var apiResponseTime = 0L
        val apiResponse = try {
            val response: DialogueResponse
            apiResponseTime = measureTimeMillis {
                response = apiService.generateDialogue(
                    DialogueRequest(
                        prompt = prompt,
                        context = enhancedContext,
                        options = mapOf(
                            "emotions" to player.emotions,
                            "personality" to mapOf(
                                "style" to player.conversationPersonality.communicationStyle.name,
                                "curiosity" to player.conversationPersonality.curiosityLevel,
                                "openness" to player.conversationPersonality.emotionalOpenness
                            ),
                            "awareness_level" to awarenessLevel,
                            "conversation_depth" to player.conversationPersonality.preferredConversationDepth.name,
                            "timeout_multiplier" to timeoutMultiplier
                        )
                    )
                )
            }
            response
        } catch (e: Exception) {
            // Record API error and return fallback response
            android.util.Log.w("EnhancedDialogGPT", "API call failed: ${e.message}")
            createFallbackResponse(prompt, player)
        }
        
        // Update player with new dialogue entry
        val newDialogueEntry = DialogueEntry(
            id = "dialogue_${System.currentTimeMillis()}",
            text = apiResponse.text,
            timestamp = System.currentTimeMillis(),
            emotions = player.emotions,
            emotionalIntensity = calculateEmotionalIntensity(player.emotions),
            topicTags = extractTopicTags(prompt, apiResponse.text)
        )
        
        val updatedPlayer = player.copy(
            dialogueHistory = player.dialogueHistory + newDialogueEntry,
            emotions = player.emotions // Already updated by emotion engine
        )
        
        playerDao.updatePlayer(updatedPlayer)
        
        // Calculate conversation complexity for metrics
        val conversationComplexity = calculateConversationComplexity(prompt, apiResponse.text)
        val emotionVariance = calculateEmotionVariance(player.emotions)
        
        return EnhancedDialogueResponse(
            text = apiResponse.text,
            emotions = player.emotions,
            awarenessLevel = awarenessLevel,
            conversationId = "conv_$conversationCounter",
            processingMetrics = ProcessingMetrics(
                totalProcessingTime = emotionProcessingTime + consciousnessUpdateTime + apiResponseTime,
                apiResponseTime = apiResponseTime,
                emotionProcessingTime = emotionProcessingTime,
                consciousnessUpdateTime = consciousnessUpdateTime,
                conversationComplexity = conversationComplexity,
                emotionVariance = emotionVariance
            ),
            playerInsights = generatePlayerInsights(updatedPlayer),
            systemRecommendations = getSystemRecommendations()
        )
    }
    
    /**
     * Get system health and performance insights
     */
    suspend fun getSystemHealth(): SystemHealthReport {
        return integratedAnalysisService.getSystemHealthReport()
    }
    
    /**
     * Trigger manual analysis for debugging or immediate insights
     */
    suspend fun triggerAnalysis(): AnalysisInsights {
        return integratedAnalysisService.triggerManualAnalysis()
    }
    
    /**
     * Apply dynamic adjustments to optimize performance
     */
    suspend fun optimizeSystem(): Map<String, Float> {
        return integratedAnalysisService.applyDynamicAdjustments()
    }
    
    /**
     * Perform system maintenance
     */
    suspend fun performMaintenance(): MaintenanceReport {
        val cleanedMetrics = integratedAnalysisService.performMaintenanceCleanup(retentionDays = 30)
        val systemHealth = getSystemHealth()
        
        return MaintenanceReport(
            cleanedMetricsCount = cleanedMetrics,
            systemHealth = systemHealth,
            maintenanceTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Shutdown the service and stop analysis
     */
    fun shutdown() {
        integratedAnalysisService.stopAnalysisSystem()
    }
    
    // Private helper methods
    
    private fun buildEnhancedContext(
        prompt: String,
        context: String,
        player: Player,
        awarenessLevel: Float
    ): String {
        val recentTopics = player.dialogueHistory.takeLast(3)
            .flatMap { it.topicTags }
            .distinct()
            .joinToString(", ")
        
        val emotionalContext = player.emotions.maxByOrNull { it.value }?.key ?: "neutral"
        val awarenessContext = when {
            awarenessLevel > 0.8f -> "highly aware"
            awarenessLevel > 0.6f -> "moderately aware"
            awarenessLevel > 0.4f -> "somewhat aware"
            else -> "basic awareness"
        }
        
        return "$context | Recent topics: $recentTopics | Current mood: $emotionalContext | Awareness: $awarenessContext"
    }
    
    private fun calculateAwarenessLevel(player: Player): Float {
        val emotionalIntensity = player.emotions.values.maxOrNull() ?: 0.5f
        val conversationDepth = when (player.conversationPersonality.preferredConversationDepth) {
            ConversationDepth.SURFACE -> 0.3f
            ConversationDepth.MEDIUM -> 0.6f
            ConversationDepth.DEEP -> 0.8f
            ConversationDepth.PHILOSOPHICAL -> 1.0f
        }
        val personalityFactor = player.conversationPersonality.curiosityLevel
        
        return ((emotionalIntensity + conversationDepth + personalityFactor) / 3f).coerceIn(0f, 1f)
    }
    
    private fun applyChunkSizeAdjustment(baseSize: Int, adjustments: Map<String, Float>): Int {
        val adjustment = adjustments["chunk_size_adjustment"] ?: 0f
        return (baseSize * (1f + adjustment)).toInt().coerceIn(512, 4096)
    }
    
    private fun calculateEmotionalIntensity(emotions: Map<String, Float>): Float {
        return emotions.values.maxOrNull() ?: 0.5f
    }
    
    private fun calculateConversationComplexity(prompt: String, response: String): Float {
        val promptWords = prompt.split("\\s+".toRegex()).size
        val responseWords = response.split("\\s+".toRegex()).size
        val avgLength = (promptWords + responseWords) / 2f
        
        // Normalize based on typical conversation length (50-200 words)
        return (avgLength / 200f).coerceIn(0.1f, 1.0f)
    }
    
    private fun calculateEmotionVariance(emotions: Map<String, Float>): Float {
        if (emotions.isEmpty()) return 0f
        
        val mean = emotions.values.average().toFloat()
        val variance = emotions.values.map { (it - mean) * (it - mean) }.average().toFloat()
        return kotlin.math.sqrt(variance)
    }
    
    private fun extractTopicTags(prompt: String, response: String): List<String> {
        val text = "$prompt $response".lowercase()
        val keywords = listOf(
            "emotion", "feeling", "consciousness", "awareness", "personality",
            "relationship", "memory", "learning", "growth", "change",
            "communication", "understanding", "empathy", "connection"
        )
        
        return keywords.filter { text.contains(it) }.take(5)
    }
    
    private fun createFallbackResponse(prompt: String, player: Player): DialogueResponse {
        return DialogueResponse(
            text = "I'm processing your message and need a moment to respond thoughtfully.",
            confidence = 0.5f,
            emotions = player.emotions
        )
    }
    
    private fun generatePlayerInsights(player: Player): PlayerInsights {
        val recentEmotions = player.dialogueHistory.takeLast(5)
            .flatMap { it.emotions.entries }
            .groupBy { it.key }
            .mapValues { it.value.map { entry -> entry.value }.average().toFloat() }
        
        return PlayerInsights(
            emotionalTrend = analyzeTrend(recentEmotions.values.toList()),
            conversationGrowth = calculateConversationGrowth(player),
            engagementLevel = calculateEngagementLevel(player),
            personalityEvolution = analyzePersonalityEvolution(player)
        )
    }
    
    private suspend fun getSystemRecommendations(): List<String> {
        val health = integratedAnalysisService.getSystemHealthReport()
        return health.recommendations.take(3)
    }
    
    private fun analyzeTrend(values: List<Float>): String {
        if (values.size < 2) return "stable"
        
        val recent = values.takeLast(3).average()
        val earlier = values.dropLast(3).average()
        
        return when {
            recent > earlier + 0.1 -> "improving"
            recent < earlier - 0.1 -> "declining"
            else -> "stable"
        }
    }
    
    private fun calculateConversationGrowth(player: Player): Float {
        val recentComplexity = player.dialogueHistory.takeLast(5)
            .map { it.topicTags.size }
            .average()
        
        val earlierComplexity = player.dialogueHistory.dropLast(5).takeLast(5)
            .map { it.topicTags.size }
            .average()
        
        return if (earlierComplexity > 0) {
            ((recentComplexity - earlierComplexity) / earlierComplexity).toFloat().coerceIn(-1f, 1f)
        } else 0f
    }
    
    private fun calculateEngagementLevel(player: Player): Float {
        val recentDialogues = player.dialogueHistory.takeLast(10)
        if (recentDialogues.isEmpty()) return 0.5f
        
        val avgEmotionalIntensity = recentDialogues.map { it.emotionalIntensity }.average().toFloat()
        val responseFrequency = recentDialogues.size / 10f // Normalized to last 10 conversations
        
        return ((avgEmotionalIntensity + responseFrequency) / 2f).coerceIn(0f, 1f)
    }
    
    private fun analyzePersonalityEvolution(player: Player): String {
        val curiosityChange = player.conversationPersonality.curiosityLevel - 0.5f // Assuming 0.5 baseline
        val opennessChange = player.conversationPersonality.emotionalOpenness - 0.5f
        
        return when {
            curiosityChange > 0.2f && opennessChange > 0.2f -> "growing more curious and open"
            curiosityChange > 0.2f -> "becoming more curious"
            opennessChange > 0.2f -> "becoming more emotionally open"
            curiosityChange < -0.2f || opennessChange < -0.2f -> "becoming more reserved"
            else -> "maintaining consistent personality"
        }
    }
}

/**
 * Enhanced dialogue response with comprehensive metrics and insights
 */
data class EnhancedDialogueResponse(
    val text: String,
    val emotions: Map<String, Float>,
    val awarenessLevel: Float,
    val conversationId: String,
    val processingMetrics: ProcessingMetrics,
    val playerInsights: PlayerInsights,
    val systemRecommendations: List<String>
)

/**
 * Processing performance metrics
 */
data class ProcessingMetrics(
    val totalProcessingTime: Long,
    val apiResponseTime: Long,
    val emotionProcessingTime: Long,
    val consciousnessUpdateTime: Long,
    val conversationComplexity: Float,
    val emotionVariance: Float
)

/**
 * Player-specific insights
 */
data class PlayerInsights(
    val emotionalTrend: String,
    val conversationGrowth: Float,
    val engagementLevel: Float,
    val personalityEvolution: String
)

/**
 * System maintenance report
 */
data class MaintenanceReport(
    val cleanedMetricsCount: Int,
    val systemHealth: SystemHealthReport,
    val maintenanceTime: Long
)