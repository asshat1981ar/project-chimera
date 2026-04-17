package com.chimera.domain.usecase

import com.chimera.data.GameSessionManager
import com.chimera.data.repository.JournalRepository
import com.chimera.data.repository.SaveRepository
import com.chimera.database.entity.VowEntity
import com.chimera.model.SaveSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoadHomeStateUseCaseTest {

    private val saveRepository: SaveRepository = mock()
    private val journalRepository: JournalRepository = mock()
    private val gameSessionManager: GameSessionManager = mock()

    private fun useCase() = LoadHomeStateUseCase(saveRepository, journalRepository, gameSessionManager)

    private val testSlot = SaveSlot(id = 5L, slotIndex = 0, playerName = "Aria", chapterTag = "act1")
    private val testVow = VowEntity(id = 1, saveSlotId = 5L, description = "Protect the village")

    @Test
    fun `emits loading false state when no active slot`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val state = useCase()().first()

        assertFalse(state.isLoading)
        assertEquals("", state.playerName)
    }

    @Test
    fun `emits correct player name from active slot`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(5L))
        whenever(saveRepository.observeAllSlots()).thenReturn(flowOf(listOf(testSlot)))
        whenever(journalRepository.observeActiveVows(5L)).thenReturn(flowOf(emptyList()))

        val state = useCase()().first()

        assertEquals("Aria", state.playerName)
    }

    @Test
    fun `emits chapter tag from active slot`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(5L))
        whenever(saveRepository.observeAllSlots()).thenReturn(flowOf(listOf(testSlot)))
        whenever(journalRepository.observeActiveVows(5L)).thenReturn(flowOf(emptyList()))

        val state = useCase()().first()

        assertEquals("act1", state.chapterTag)
    }

    @Test
    fun `counts active vows correctly`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(5L))
        whenever(saveRepository.observeAllSlots()).thenReturn(flowOf(listOf(testSlot)))
        whenever(journalRepository.observeActiveVows(5L)).thenReturn(
            flowOf(listOf(testVow, testVow.copy(id = 2)))
        )

        val state = useCase()().first()

        assertEquals(2, state.activeVowCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun `emits loading false when slot not in list`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(99L))
        whenever(saveRepository.observeAllSlots()).thenReturn(flowOf(emptyList()))
        whenever(journalRepository.observeActiveVows(99L)).thenReturn(flowOf(emptyList()))

        val state = useCase()().first()

        assertEquals("", state.playerName)
        assertEquals("prologue", state.chapterTag)
        assertFalse(state.isLoading)
    }
}
