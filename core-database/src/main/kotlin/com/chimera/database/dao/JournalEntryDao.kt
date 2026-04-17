package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.chimera.database.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class JournalEntryDao {

    @Query("SELECT * FROM journal_entries WHERE save_slot_id = :slotId ORDER BY created_at DESC")
    abstract fun observeAll(slotId: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE save_slot_id = :slotId AND category = :category ORDER BY created_at DESC")
    abstract fun observeByCategory(slotId: Long, category: String): Flow<List<JournalEntryEntity>>

    @Insert
    abstract suspend fun insert(entry: JournalEntryEntity): Long

    @Query("UPDATE journal_entries SET is_read = 1 WHERE id = :id")
    abstract suspend fun markRead(id: Long)

    @Query("SELECT COUNT(*) FROM journal_entries WHERE save_slot_id = :slotId AND is_read = 0")
    abstract fun observeUnreadCount(slotId: Long): Flow<Int>

    // FTS5 queries use @RawQuery to bypass static SQL validation — MATCH is not understood
    // by Room's annotation processor but is valid SQLite FTS5 syntax at runtime.

    @RawQuery(observedEntities = [JournalEntryEntity::class])
    protected abstract fun searchEntriesRaw(query: SupportSQLiteQuery): Flow<List<JournalEntryEntity>>

    fun searchEntries(slotId: Long, escapedQuery: String): Flow<List<JournalEntryEntity>> =
        searchEntriesRaw(SimpleSQLiteQuery(
            """
            SELECT je.* FROM journal_entries je
            WHERE je.save_slot_id = ?
              AND je.id IN (
                  SELECT rowid FROM journal_entries_fts
                  WHERE journal_entries_fts MATCH ?
              )
            ORDER BY je.created_at DESC
            """.trimIndent(),
            arrayOf<Any>(slotId, escapedQuery)
        ))

    fun searchEntriesByCategory(slotId: Long, category: String, escapedQuery: String): Flow<List<JournalEntryEntity>> =
        searchEntriesRaw(SimpleSQLiteQuery(
            """
            SELECT je.* FROM journal_entries je
            WHERE je.save_slot_id = ?
              AND je.category = ?
              AND je.id IN (
                  SELECT rowid FROM journal_entries_fts
                  WHERE journal_entries_fts MATCH ?
              )
            ORDER BY je.created_at DESC
            """.trimIndent(),
            arrayOf<Any>(slotId, category, escapedQuery)
        ))
}
