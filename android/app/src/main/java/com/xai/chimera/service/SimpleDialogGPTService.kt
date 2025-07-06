package com.xai.chimera.service

import com.xai.chimera.api.DialogueApiService
import com.xai.chimera.api.DialogueRequest
import com.xai.chimera.api.DialogueResponse
import com.xai.chimera.dao.PlayerDao
import com.xai.chimera.domain.*
import kotlinx.coroutines.*

/**
 * Simple, focused DialogGPT service
 * Core dialogue functionality without complex consciousness features
 */
class SimpleDialogGPTService(
    private val apiService: DialogueApiService,
    private val playerDao: PlayerDao,
    private val emotionEngine: EmotionEngineService
) {
    
    /**
     * Generate basic dialogue response
     */
    suspend fun generateDialogue(
        playerId: String,
        prompt: String,
        context: String = ""
    ): DialogueResponse = withContext(Dispatchers.IO) {
        
        val player = playerDao.getPlayer(playerId) ?: throw IllegalArgumentException("Player not found")
        
        // Create request with basic player context
        val request = DialogueRequest(
            prompt = prompt,
            context = buildContextString(context, player),
            options = mapOf(
                "emotions" to player.emotions,
                "communication_style" to player.conversationPersonality.communicationStyle.name
            )
        )
        
        // Make API call
        val response = apiService.generateDialogue(request)
        val dialogueResponse = if (response.isSuccessful && response.body() != null) {
            response.body() ?: throw RuntimeException("Empty response body")
        } else {
            throw RuntimeException("Failed to generate dialogue: ${response.errorBody()?.string()}")
        }
        
        // Update player state
        emotionEngine.updatePlayerEmotionalState(playerId, dialogueResponse)
        updatePlayerHistory(player, dialogueResponse)
        
        return@withContext dialogueResponse
    }
    
    /**
     * Get player's conversation history
     */
    suspend fun getConversationHistory(playerId: String): List<DialogueEntry> {
        val player = playerDao.getPlayer(playerId) ?: throw IllegalArgumentException("Player not found")
        return player.dialogueHistory
    }
    
    /**
     * Get player's current emotional state
     */
    suspend fun getPlayerEmotions(playerId: String): Map<String, Float> {
        val player = playerDao.getPlayer(playerId) ?: throw IllegalArgumentException("Player not found")
        return player.emotions
    }
    
    private fun buildContextString(context: String, player: Player): String {
        val recentDialogue = player.dialogueHistory.takeLast(2)
            .joinToString(" ") { it.text }
        
        val emotionalContext = player.emotions.maxByOrNull { it.value }?.key ?: "neutral"
        
        return listOf(context, "Recent: $recentDialogue", "Mood: $emotionalContext")
            .filter { it.isNotBlank() }
            .joinToString(" | ")
    }
    
    private suspend fun updatePlayerHistory(player: Player, response: DialogueResponse) {
        val newEntry = DialogueEntry(
            id = "entry_${System.currentTimeMillis()}",
            text = response.text,
            timestamp = System.currentTimeMillis(),
            emotions = response.emotions,
            emotionalIntensity = response.emotions.values.maxOrNull() ?: 0.5f
        )
        
        val updatedPlayer = player.copy(
            dialogueHistory = (player.dialogueHistory + newEntry).takeLast(50),
            emotions = response.emotions
        )
        
        playerDao.updatePlayer(updatedPlayer)
    }
}