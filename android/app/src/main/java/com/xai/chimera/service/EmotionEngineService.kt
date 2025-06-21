package com.xai.chimera.service

import com.xai.chimera.api.DialogueResponse

/**
 * Enhanced service for consciousness-inspired emotion processing
 */
interface EmotionEngineService {
    /**
     * Analyzes text and extracts emotions as intensity map
     */
    suspend fun analyzeEmotions(text: String): Map<String, Float>
    
    /**
     * Updates player emotional state based on dialogue response
     */
    suspend fun updatePlayerEmotionalState(playerId: String, dialogueResponse: DialogueResponse)
    
    /**
     * Gets recommended emotional responses based on context
     */
    suspend fun getRecommendedEmotions(playerId: String, context: String): Map<String, Float>
}
