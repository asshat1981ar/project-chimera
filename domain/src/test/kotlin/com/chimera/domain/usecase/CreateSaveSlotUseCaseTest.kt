package com.chimera.domain.usecase

import com.chimera.data.GameSessionManager
import com.chimera.data.NpcSeeder
import com.chimera.data.repository.SaveRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CreateSaveSlotUseCaseTest {

    private val saveRepository: SaveRepository = mock()
    private val npcSeeder: NpcSeeder = mock()
    private val gameSessionManager: GameSessionManager = mock()

    private fun useCase() = CreateSaveSlotUseCase(saveRepository, npcSeeder, gameSessionManager)

    @Test
    fun `returns slotId from repository`() = runTest {
        whenever(saveRepository.createSlot(0, "Aria")).thenReturn(10L)

        val result = useCase()(slotIndex = 0, playerName = "Aria")

        assertEquals(10L, result)
    }

    @Test
    fun `seeds NPCs for the new slot`() = runTest {
        whenever(saveRepository.createSlot(1, "Kael")).thenReturn(7L)

        useCase()(slotIndex = 1, playerName = "Kael")

        verify(npcSeeder).seedNpcsForSlot(7L)
    }

    @Test
    fun `sets active slot after creation`() = runTest {
        whenever(saveRepository.createSlot(0, "Player")).thenReturn(42L)

        useCase()(slotIndex = 0, playerName = "Player")

        verify(gameSessionManager).setActiveSlot(42L)
    }

    @Test
    fun `calls repository and session manager in correct order`() = runTest {
        val order = mutableListOf<String>()
        whenever(saveRepository.createSlot(0, "Test")).thenAnswer {
            order += "createSlot"
            3L
        }
        whenever(npcSeeder.seedNpcsForSlot(3L)).thenAnswer { order += "seedNpcs"; Unit }
        whenever(gameSessionManager.setActiveSlot(3L)).thenAnswer { order += "setActive"; Unit }

        useCase()(slotIndex = 0, playerName = "Test")

        assertEquals(listOf("createSlot", "seedNpcs", "setActive"), order)
    }
}
