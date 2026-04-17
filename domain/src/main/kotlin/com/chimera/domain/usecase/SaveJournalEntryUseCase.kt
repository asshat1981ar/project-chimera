package com.chimera.domain.usecase

import com.chimera.data.repository.JournalRepository
import com.chimera.database.entity.JournalEntryEntity
import javax.inject.Inject

class SaveJournalEntryUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(entry: JournalEntryEntity): Long =
        journalRepository.insertEntry(entry)
}
