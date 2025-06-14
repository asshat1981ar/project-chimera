package com.xai.chimera.test

import com.xai.chimera.api.DialogueApiService
import com.xai.chimera.api.DialogueRequest
import com.xai.chimera.api.DialogueResponse
import com.xai.chimera.api.Emotion
import com.xai.chimera.dao.PlayerDao
import com.xai.chimera.domain.Player
import com.xai.chimera.service.DialogGPTService
import com.xai.chimera.service.EmotionEngineService
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.runner.RunWith
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for DialogGPTService.
 */
@RunWith(MockitoJUnitRunner::class)
class DialogGPTServiceTest {

    @Mock
    private lateinit var apiService: DialogueApiService
    
    @Mock
    private lateinit var playerDao: PlayerDao
    
    @Mock
    private lateinit var emotionEngine: EmotionEngineService
    
    private lateinit var dialogGPTService: DialogGPTService

    @Before
    fun setup() {
        dialogGPTService = DialogGPTService(apiService, playerDao, emotionEngine)
    }

    @Test
    fun `test generate dialogue success`() = runBlocking {
        // Given
        val playerId = "player123"
        val prompt = "Hello there"
        val context = "Friendly conversation"
        
        val player = Player(
            id = playerId,
            name = "Test Player",
            emotions = mapOf("happy" to 0.8f, "curious" to 0.6f)
        )
        
        val dialogueResponse = DialogueResponse(
            id = "dialogue123",
            text = "Hello! How can I help you today?",
            emotions = listOf(Emotion("friendly", 0.9f), Emotion("helpful", 0.8f))
        )
        
        `when`(playerDao.getPlayer(playerId)).thenReturn(player)
        
        val request = DialogueRequest(
            prompt = prompt,
            context = context,
            options = mapOf("emotions" to player.emotions)
        )
        
        `when`(apiService.generateDialogue(request)).thenReturn(Response.success(dialogueResponse))
        
        // When
        val result = dialogGPTService.generateDialogue(playerId, prompt, context)
        
        // Then
        assertNotNull(result)
        assertEquals("dialogue123", result.id)
        assertEquals("Hello! How can I help you today?", result.text)
        assertEquals(2, result.emotions.size)
        
        verify(playerDao).getPlayer(playerId)
        verify(apiService).generateDialogue(request)
        verify(emotionEngine).updatePlayerEmotionalState(playerId, dialogueResponse)
    }

    @Test
    fun `test get dialogue success`() = runBlocking {
        // Given
        val dialogueId = "dialogue123"
        
        val dialogueResponse = DialogueResponse(
            id = dialogueId,
            text = "Hello! How can I help you today?",
            emotions = listOf(Emotion("friendly", 0.9f), Emotion("helpful", 0.8f))
        )
        
        `when`(apiService.getDialogue(dialogueId)).thenReturn(Response.success(dialogueResponse))
        
        // When
        val result = dialogGPTService.getDialogue(dialogueId)
        
        // Then
        assertNotNull(result)
        assertEquals(dialogueId, result.id)
        assertEquals("Hello! How can I help you today?", result.text)
        assertEquals(2, result.emotions.size)
        
        verify(apiService).getDialogue(dialogueId)
    }
}
