package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.chimera.database.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Query("SELECT * FROM journal_entries WHERE save_slot_id = :slotId ORDER BY created_at DESC")
    fun observeAll(slotId: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE save_slot_id = :slotId AND category = :category ORDER BY created_at DESC")
    fun observeByCategory(slotId: Long, category: String): Flow<List<JournalEntryEntity>>

    @Insert
    suspend fun insert(entry: JournalEntryEntity): Long

    @Query("UPDATE journal_entries SET is_read = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("SELECT COUNT(*) FROM journal_entries WHERE save_slot_id = :slotId AND is_read = 0")
    fun observeUnreadCount(slotId: Long): Flow<Int>
}
