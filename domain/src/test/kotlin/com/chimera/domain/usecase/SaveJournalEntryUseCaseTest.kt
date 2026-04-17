package com.chimera.domain.usecase

import com.chimera.data.repository.JournalRepository
import com.chimera.database.entity.JournalEntryEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SaveJournalEntryUseCaseTest {

    private val journalRepository: JournalRepository = mock()

    private fun useCase() = SaveJournalEntryUseCase(journalRepository)

    private fun fakeEntry(
        saveSlotId: Long = 1L,
        title: String = "Test",
        body: String = "Content",
        category: String = "story"
    ) = JournalEntryEntity(
        saveSlotId = saveSlotId,
        title = title,
        body = body,
        category = category,
        createdAt = 0L
    )

    @Test
    fun `invoke_callsRepositoryInsertEntry`() = runTest {
        val entry = fakeEntry()
        whenever(journalRepository.insertEntry(entry)).thenReturn(1L)

        useCase()(entry)

        verify(journalRepository).insertEntry(entry)
    }

    @Test
    fun `invoke_returnsInsertedId`() = runTest {
        val entry = fakeEntry()
        whenever(journalRepository.insertEntry(entry)).thenReturn(42L)

        val result = useCase()(entry)

        assertEquals(42L, result)
    }

    @Test
    fun `invoke_passesEntryUnmodified`() = runTest {
        val entry = fakeEntry(saveSlotId = 7L, title = "My Title", body = "My Body", category = "rumor")
        whenever(journalRepository.insertEntry(entry)).thenReturn(1L)

        useCase()(entry)

        val captor = argumentCaptor<JournalEntryEntity>()
        verify(journalRepository).insertEntry(captor.capture())
        val captured = captor.firstValue
        assertEquals(7L, captured.saveSlotId)
        assertEquals("My Title", captured.title)
        assertEquals("My Body", captured.body)
        assertEquals("rumor", captured.category)
    }

    @Test
    fun `invoke_withMinimalEntry`() = runTest {
        val entry = JournalEntryEntity(
            saveSlotId = 1L,
            title = "",
            body = "",
            category = "story",
            createdAt = 0L
        )
        whenever(journalRepository.insertEntry(entry)).thenReturn(1L)

        useCase()(entry)

        verify(journalRepository).insertEntry(entry)
    }

    @Test
    fun `invoke_withLongContent`() = runTest {
        val longBody = "A".repeat(1000)
        val entry = fakeEntry(body = longBody)
        whenever(journalRepository.insertEntry(entry)).thenReturn(5L)

        useCase()(entry)

        verify(journalRepository).insertEntry(entry)
    }

    @Test
    fun `invoke_withDifferentCategories`() = runTest {
        val storyEntry = fakeEntry(category = "story")
        val rumorEntry = fakeEntry(category = "rumor")
        whenever(journalRepository.insertEntry(storyEntry)).thenReturn(1L)
        whenever(journalRepository.insertEntry(rumorEntry)).thenReturn(2L)

        val uc = useCase()
        uc(storyEntry)
        uc(rumorEntry)

        verify(journalRepository, times(1)).insertEntry(storyEntry)
        verify(journalRepository, times(1)).insertEntry(rumorEntry)
    }
}
