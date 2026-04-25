package com.chimera.domain.usecase

import com.chimera.data.repository.JournalRepository
import com.chimera.model.DialogueTurnResult
import com.chimera.model.JournalEntry
import com.chimera.model.SceneContract
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GenerateSceneSummaryUseCaseTest {

    private val journalRepository: JournalRepository = mock()

    private val testContract = SceneContract(
        sceneId = "watchtower_1",
        sceneTitle = "The Watchtower",
        npcId = "warden",
        npcName = "The Warden",
        setting = "the watchtower gate",
        maxTurns = 5
    )

    private fun useCase() = GenerateSceneSummaryUseCase(journalRepository)

    @Test
    fun `inserts one story entry for a single-turn scene`() = runTest {
        whenever(journalRepository.insertEntry(any())).thenReturn(1L)

        useCase()(
            slotId = 1L,
            contract = testContract,
            turnResults = listOf(DialogueTurnResult("Opening line"))
        )

        val captor = argumentCaptor<JournalEntry>()
        verify(journalRepository, times(1)).insertEntry(captor.capture())
        assertEquals("story", captor.firstValue.category)
        assertEquals(testContract.sceneTitle, captor.firstValue.title)
    }

    @Test
    fun `brief encounter summary for single turn`() = runTest {
        whenever(journalRepository.insertEntry(any())).thenReturn(1L)

        useCase()(
            slotId = 1L,
            contract = testContract,
            turnResults = listOf(DialogueTurnResult("Line"))
        )

        val captor = argumentCaptor<JournalEntry>()
        verify(journalRepository).insertEntry(captor.capture())
        assertTrue(captor.firstValue.body.contains("brief encounter"))
    }

    @Test
    fun `multi-turn summary includes turn count`() = runTest {
        whenever(journalRepository.insertEntry(any())).thenReturn(1L)

        val turns = listOf(
            DialogueTurnResult("Line 1"),
            DialogueTurnResult("Line 2"),
            DialogueTurnResult("Line 3")
        )

        useCase()(slotId = 1L, contract = testContract, turnResults = turns)

        val captor = argumentCaptor<JournalEntry>()
        verify(journalRepository, times(1)).insertEntry(captor.capture())
        assertTrue(captor.firstValue.body.contains("3"))
    }

    @Test
    fun `inserts companion entry on recruit_companion flag`() = runTest {
        whenever(journalRepository.insertEntry(any())).thenReturn(1L)

        useCase()(
            slotId = 1L,
            contract = testContract,
            turnResults = listOf(
                DialogueTurnResult("Opening"),
                DialogueTurnResult("Recruited!", flags = listOf("recruit_companion"))
            )
        )

        val captor = argumentCaptor<JournalEntry>()
        verify(journalRepository, times(2)).insertEntry(captor.capture())
        val companion = captor.allValues.first { it.category == "companion" }
        assertEquals("warden", companion.characterId)
        assertTrue(companion.body.contains("The Warden"))
    }

    @Test
    fun `no companion entry when recruit flag is absent`() = runTest {
        whenever(journalRepository.insertEntry(any())).thenReturn(1L)

        useCase()(
            slotId = 1L,
            contract = testContract,
            turnResults = listOf(DialogueTurnResult("Nope"), DialogueTurnResult("Also nope"))
        )

        val captor = argumentCaptor<JournalEntry>()
        verify(journalRepository, times(1)).insertEntry(captor.capture())
        assertEquals("story", captor.firstValue.category)
    }

    private fun any() = org.mockito.kotlin.any<JournalEntry>()
}
