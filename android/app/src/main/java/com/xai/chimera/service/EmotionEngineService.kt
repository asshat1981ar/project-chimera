package com.xai.chimera.service

import com.xai.chimera.api.DialogueResponse
import com.xai.chimera.api.Emotion

/**
 * Service for handling emotion processing in the DialogGPT system
 */
interface EmotionEngineService {
    /**
     * Analyzes text and extracts emotions
     */
    suspend fun analyzeEmotions(text: String): List<Emotion>
    
    /**
     * Updates player emotional state based on dialogue response
     */
    suspend fun updatePlayerEmotionalState(playerId: String, response: DialogueResponse)
    
    /**
     * Gets recommended emotional responses based on context
     */
    suspend fun getRecommendedEmotions(context: String): List<Emotion>
}
