package com.chimera.data.repository

import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.database.entity.VowEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val vowDao: VowDao
) {
    fun observeEntries(slotId: Long): Flow<List<JournalEntryEntity>> =
        journalEntryDao.observeAll(slotId)

    fun observeByCategory(slotId: Long, category: String): Flow<List<JournalEntryEntity>> =
        journalEntryDao.observeByCategory(slotId, category)

    fun observeUnreadCount(slotId: Long): Flow<Int> =
        journalEntryDao.observeUnreadCount(slotId)

    suspend fun insertEntry(entry: JournalEntryEntity): Long =
        journalEntryDao.insert(entry)

    suspend fun markRead(entryId: Long) =
        journalEntryDao.markRead(entryId)

    fun observeVows(slotId: Long): Flow<List<VowEntity>> =
        vowDao.observeAll(slotId)

    fun observeActiveVows(slotId: Long): Flow<List<VowEntity>> =
        vowDao.observeActive(slotId)

    suspend fun insertVow(vow: VowEntity): Long =
        vowDao.insert(vow)

    suspend fun resolveVow(id: Long, status: String) =
        vowDao.resolve(id, status)
}
