package com.chimera.domain.usecase

import com.chimera.data.repository.JournalRepository
import com.chimera.model.JournalEntry
import javax.inject.Inject

class SaveJournalEntryUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(entry: JournalEntry): Long =
        journalRepository.insertEntry(entry)
}
