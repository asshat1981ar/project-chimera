package com.chimera.domain.usecase

import com.chimera.ai.DialogueOrchestrator
import com.chimera.data.SceneLoader
import com.chimera.data.repository.CharacterRepository
import com.chimera.data.repository.DialogueRepository
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class StartSceneUseCaseTest {

    private val sceneLoader: SceneLoader = mock()
    private val dialogueRepository: DialogueRepository = mock()
    private val characterRepository: CharacterRepository = mock()
    private val orchestrator: DialogueOrchestrator = mock()

    private fun useCase() = StartSceneUseCase(sceneLoader, dialogueRepository, characterRepository, orchestrator)

    private val testContract = SceneContract(
        sceneId = "watchtower_1",
        sceneTitle = "The Watchtower",
        npcId = "warden",
        npcName = "The Warden",
        setting = "the watchtower gate"
    )

    private val testCharacterState = CharacterState(
        characterId = "warden",
        saveSlotId = 1L,
        dispositionToPlayer = 0.5f
    )

    private val testTurnResult = DialogueTurnResult(
        npcLine = "Who goes there?",
        emotion = "alert"
    )

    @Test
    fun `execute creates scene instance in DB`() = runTest {
        // Arrange
        whenever(sceneLoader.getScene("watchtower_1")).thenReturn(testContract)
        whenever(dialogueRepository.createSceneInstance(1L, "watchtower_1", "warden")).thenReturn(100L)
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharacterState)
        whenever(dialogueRepository.getRecentMemories(1L, "warden", 5)).thenReturn(emptyList())
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(testTurnResult)
        whenever(orchestrator.generateIntents(any(), any(), any())).thenReturn(emptyList())
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // Act
        useCase()(slotId = 1L, sceneId = "watchtower_1")

        // Assert
        verify(dialogueRepository, times(1)).createSceneInstance(1L, "watchtower_1", "warden")
    }

    @Test
    fun `execute initializes character state`() = runTest {
        // Arrange
        whenever(sceneLoader.getScene("watchtower_1")).thenReturn(testContract)
        whenever(dialogueRepository.createSceneInstance(1L, "watchtower_1", "warden")).thenReturn(100L)
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharacterState)
        whenever(dialogueRepository.getRecentMemories(1L, "warden", 5)).thenReturn(emptyList())
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(testTurnResult)
        whenever(orchestrator.generateIntents(any(), any(), any())).thenReturn(emptyList())
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // Act
        val result = useCase()(slotId = 1L, sceneId = "watchtower_1")

        // Assert
        assertNotNull(result.characterState)
        assertEquals("warden", result.characterState.characterId)
        assertEquals(1L, result.characterState.saveSlotId)
        verify(characterRepository, times(1)).getCharacterState("warden")
    }

    @Test
    fun `execute returns correct scene summary`() = runTest {
        // Arrange
        whenever(sceneLoader.getScene("watchtower_1")).thenReturn(testContract)
        whenever(dialogueRepository.createSceneInstance(1L, "watchtower_1", "warden")).thenReturn(100L)
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharacterState)
        whenever(dialogueRepository.getRecentMemories(1L, "warden", 5)).thenReturn(emptyList())
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(testTurnResult)
        whenever(orchestrator.generateIntents(any(), any(), any())).thenReturn(listOf("Ask about duty", "Mention the king"))
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // Act
        val result = useCase()(slotId = 1L, sceneId = "watchtower_1")

        // Assert
        assertNotNull(result.contract)
        assertEquals("watchtower_1", result.contract.sceneId)
        assertEquals("The Watchtower", result.contract.sceneTitle)
        assertEquals("warden", result.contract.npcId)
        assertEquals("The Warden", result.contract.npcName)
        assertEquals("the watchtower gate", result.contract.setting)
        assertEquals(testTurnResult, result.openingTurn)
        assertEquals(listOf("Ask about duty", "Mention the king"), result.intents)
        assertEquals(100L, result.sceneInstanceId)
        assertFalse(result.isFallback)
    }

    @Test
    fun `execute uses fallback defaults for invalid scene id`() = runTest {
        // Arrange - scene not found, returns null
        whenever(sceneLoader.getScene("unknown_scene")).thenReturn(null)
        whenever(dialogueRepository.createSceneInstance(1L, "unknown_scene", "unknown")).thenReturn(200L)
        whenever(characterRepository.getCharacterState("unknown")).thenReturn(null)
        whenever(dialogueRepository.getRecentMemories(1L, "unknown", 5)).thenReturn(emptyList())
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(testTurnResult)
        whenever(orchestrator.generateIntents(any(), any(), any())).thenReturn(emptyList())
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // Act
        val result = useCase()(slotId = 1L, sceneId = "unknown_scene")

        // Assert - uses default/fallback values
        assertNotNull(result.contract)
        assertEquals("unknown_scene", result.contract.sceneId)
        assertEquals("Unknown Scene", result.contract.sceneTitle)
        assertEquals("unknown", result.contract.npcId)
        assertEquals("Stranger", result.contract.npcName)
        assertEquals("an unfamiliar place", result.contract.setting)
    }

    @Test
    fun `execute creates default character state for invalid slot`() = runTest {
        // Arrange - character state not found for slot
        whenever(sceneLoader.getScene("watchtower_1")).thenReturn(testContract)
        whenever(dialogueRepository.createSceneInstance(999L, "watchtower_1", "warden")).thenReturn(300L)
        whenever(characterRepository.getCharacterState("warden")).thenReturn(null)
        whenever(dialogueRepository.getRecentMemories(999L, "warden", 5)).thenReturn(emptyList())
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(testTurnResult)
        whenever(orchestrator.generateIntents(any(), any(), any())).thenReturn(emptyList())
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // Act
        val result = useCase()(slotId = 999L, sceneId = "watchtower_1")

        // Assert - creates default CharacterState
        assertNotNull(result.characterState)
        assertEquals("warden", result.characterState.characterId)
        assertEquals(999L, result.characterState.saveSlotId)
        assertEquals(1.0f, result.characterState.healthFraction)
        assertEquals(0.0f, result.characterState.dispositionToPlayer)
        assertTrue(result.characterState.emotionalState.isEmpty())
    }

    @Test
    fun `execute sets initial scene variables`() = runTest {
        // Arrange
        val testMemories = listOf(
            MemoryShard(
                id = 1L,
                saveSlotId = 1L,
                sceneId = "watchtower_1",
                characterId = "warden",
                summary = "Previous encounter at the gate"
            )
        )
        whenever(sceneLoader.getScene("watchtower_1")).thenReturn(testContract)
        whenever(dialogueRepository.createSceneInstance(1L, "watchtower_1", "warden")).thenReturn(400L)
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharacterState)
        whenever(dialogueRepository.getRecentMemories(1L, "warden", 5)).thenReturn(testMemories)
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(testTurnResult)
        whenever(orchestrator.generateIntents(any(), any(), any())).thenReturn(emptyList())
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // Act
        val result = useCase()(slotId = 1L, sceneId = "watchtower_1")

        // Assert
        assertNotNull(result.recentMemories)
        assertEquals(1, result.recentMemories.size)
        assertEquals("Previous encounter at the gate", result.recentMemories[0].summary)

        // Verify persistTurn was called to initialize the scene
        argumentCaptor<String>().apply {
            verify(dialogueRepository, times(1)).persistTurn(
                any(), any(), any(), capture(), any()
            )
            assertTrue(firstValue.isNotEmpty())
        }

        // Verify generateTurn was called with initial PlayerInput
        argumentCaptor<PlayerInput>().apply {
            verify(orchestrator, times(1)).generateTurn(
                any(), capture(), any(), any(), any()
            )
            assertEquals("[Scene begins]", firstValue.text)
            assertTrue(firstValue.isQuickIntent)
        }
    }
}
