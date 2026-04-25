package com.chimera.domain.usecase

import com.chimera.data.repository.CharacterRepository
import com.chimera.data.repository.JournalRepository
import com.chimera.model.JournalEntry
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class ApplyRelationshipDeltaUseCaseTest {

    private val characterRepository: CharacterRepository = mock()
    private val journalRepository: JournalRepository = mock()

    private fun useCase() = ApplyRelationshipDeltaUseCase(characterRepository, journalRepository)

    @Test
    fun `always adjusts disposition regardless of delta size`() = runTest {
        useCase()(slotId = 1L, characterId = "elena", characterName = "Elena", delta = 0.05f)

        verify(characterRepository).adjustDisposition("elena", 0.05f)
    }

    @Test
    fun `does not insert journal entry when delta is below threshold`() = runTest {
        useCase()(slotId = 1L, characterId = "elena", characterName = "Elena", delta = 0.05f)

        verify(journalRepository, never()).insertEntry(any())
    }

    @Test
    fun `inserts journal entry when positive delta meets threshold`() = runTest {
        useCase()(slotId = 1L, characterId = "elena", characterName = "Elena", delta = 0.15f)

        val captor = argumentCaptor<JournalEntry>()
        verify(journalRepository).insertEntry(captor.capture())
        val entry = captor.firstValue
        assertEquals(1L, entry.saveSlotId)
        assertEquals("companion", entry.category)
        assertEquals("elena", entry.characterId)
        assertTrue(entry.body.contains("warmed to"))
    }

    @Test
    fun `inserts journal entry with cold direction for negative delta`() = runTest {
        useCase()(slotId = 2L, characterId = "thorne", characterName = "Thorne", delta = -0.2f)

        val captor = argumentCaptor<JournalEntry>()
        verify(journalRepository).insertEntry(captor.capture())
        assertTrue(captor.firstValue.body.contains("grown colder toward"))
    }

    @Test
    fun `includes context in journal body when provided`() = runTest {
        useCase()(
            slotId = 1L,
            characterId = "vessa",
            characterName = "Vessa",
            delta = 0.2f,
            context = "the shrine encounter"
        )

        val captor = argumentCaptor<JournalEntry>()
        verify(journalRepository).insertEntry(captor.capture())
        assertTrue(captor.firstValue.body.contains("the shrine encounter"))
    }

    @Test
    fun `exactly at threshold inserts entry`() = runTest {
        useCase()(slotId = 1L, characterId = "warden", characterName = "Warden",
            delta = ApplyRelationshipDeltaUseCase.JOURNAL_THRESHOLD)

        verify(journalRepository).insertEntry(any())
    }
}
