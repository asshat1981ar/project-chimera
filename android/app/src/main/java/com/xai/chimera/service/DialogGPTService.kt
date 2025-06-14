package com.xai.chimera.service

import com.xai.chimera.api.DialogueApiService
import com.xai.chimera.api.DialogueRequest
import com.xai.chimera.api.DialogueResponse
import com.xai.chimera.dao.PlayerDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main service for DialogGPT functionality
 */
class DialogGPTService(
    private val apiService: DialogueApiService,
    private val playerDao: PlayerDao,
    private val emotionEngine: EmotionEngineService
) {
    /**
     * Generates dialogue response based on user input
     */
    suspend fun generateDialogue(playerId: String, prompt: String, context: String): DialogueResponse {
        return withContext(Dispatchers.IO) {
            val player = playerDao.getPlayer(playerId) ?: throw IllegalArgumentException("Player not found")
            
            val request = DialogueRequest(
                prompt = prompt,
                context = context,
                options = mapOf("emotions" to player.emotions)
            )
            
            val response = apiService.generateDialogue(request)
            if (response.isSuccessful && response.body() != null) {
                val dialogueResponse = response.body()!!
                emotionEngine.updatePlayerEmotionalState(playerId, dialogueResponse)
                return@withContext dialogueResponse
            } else {
                throw RuntimeException("Failed to generate dialogue: ${response.errorBody()?.string()}")
            }
        }
    }
    
    /**
     * Retrieves a specific dialogue by ID
     */
    suspend fun getDialogue(dialogueId: String): DialogueResponse {
        return withContext(Dispatchers.IO) {
            val response = apiService.getDialogue(dialogueId)
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            } else {
                throw RuntimeException("Failed to retrieve dialogue: ${response.errorBody()?.string()}")
            }
        }
    }
}
