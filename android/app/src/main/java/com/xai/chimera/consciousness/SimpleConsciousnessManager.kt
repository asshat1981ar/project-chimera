package com.xai.chimera.consciousness

import com.xai.chimera.domain.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Simplified consciousness state manager
 * Provides basic awareness tracking without complex consciousness simulation
 */
class SimpleConsciousnessManager {
    
    private val currentState = MutableStateFlow(BasicConsciousnessState())
    
    /**
     * Get current consciousness state
     */
    fun getCurrentState(): StateFlow<BasicConsciousnessState> = currentState.asStateFlow()
    
    /**
     * Update consciousness based on conversation context
     */
    suspend fun updateFromConversation(
        emotionalState: Map<String, Float>,
        conversationComplexity: Float,
        playerPersonality: ConversationPersonality
    ): BasicConsciousnessState {
        
        val emotionalIntensity = emotionalState.values.maxOrNull() ?: 0.5f
        val awarenessLevel = calculateAwareness(emotionalIntensity, conversationComplexity, playerPersonality)
        val attentionLevel = calculateAttention(conversationComplexity, emotionalIntensity)
        
        val newState = BasicConsciousnessState(
            awarenessLevel = awarenessLevel,
            attentionLevel = attentionLevel,
            emotionalClarity = emotionalIntensity,
            conversationFocus = determineFocus(conversationComplexity),
            timestamp = System.currentTimeMillis()
        )
        
        currentState.value = newState
        return newState
    }
    
    /**
     * Generate simple consciousness insight
     */
    fun generateInsight(state: BasicConsciousnessState): String {
        return when {
            state.awarenessLevel > 0.8f -> "High awareness - deeply engaged in conversation"
            state.awarenessLevel > 0.6f -> "Good awareness - actively participating"
            state.awarenessLevel > 0.4f -> "Moderate awareness - following the conversation"
            else -> "Basic awareness - present but not deeply engaged"
        }
    }
    
    private fun calculateAwareness(
        emotionalIntensity: Float,
        complexity: Float,
        personality: ConversationPersonality
    ): Float {
        val emotionFactor = emotionalIntensity * 0.3f
        val complexityFactor = complexity * 0.3f
        val personalityFactor = personality.curiosityLevel * 0.4f
        
        return (emotionFactor + complexityFactor + personalityFactor).coerceIn(0f, 1f)
    }
    
    private fun calculateAttention(complexity: Float, emotionalIntensity: Float): Float {
        return ((complexity + emotionalIntensity) / 2f).coerceIn(0.2f, 1f)
    }
    
    private fun determineFocus(complexity: Float): String {
        return when {
            complexity > 0.7f -> "deep_analysis"
            complexity > 0.5f -> "active_engagement"
            complexity > 0.3f -> "casual_attention"
            else -> "minimal_focus"
        }
    }
}

/**
 * Simplified consciousness state data
 */
data class BasicConsciousnessState(
    val awarenessLevel: Float = 0.5f,
    val attentionLevel: Float = 0.5f,
    val emotionalClarity: Float = 0.5f,
    val conversationFocus: String = "casual_attention",
    val timestamp: Long = System.currentTimeMillis()
)