package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.EventLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Append-only access to the event log (ADR-002).
 *
 * There is deliberately **no update or delete** here: the log is immutable. The only
 * sanctioned removal is log compaction, which will be added as an explicit, audited
 * operation in a follow-up task.
 */
@Dao
interface EventLogDao {

    /**
     * Appends a record. [OnConflictStrategy.IGNORE] makes re-appending an already-known
     * record a no-op, which is what makes cloud pull idempotent.
     *
     * @return the new row id, or -1 if the record was already present.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun append(record: EventLogEntity): Long

    /** Appends a batch; already-known records are ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun appendAll(records: List<EventLogEntity>): List<Long>

    /** Full log for a slot, in replay order. */
    @Query("SELECT * FROM game_events WHERE slot_id = :slotId ORDER BY sequence ASC")
    suspend fun loadForSlot(slotId: Long): List<EventLogEntity>

    /** Log for a slot from [fromSequence] (inclusive) - used for incremental cloud push. */
    @Query(
        "SELECT * FROM game_events WHERE slot_id = :slotId AND sequence >= :fromSequence " +
            "ORDER BY sequence ASC"
    )
    suspend fun loadForSlotFrom(slotId: Long, fromSequence: Long): List<EventLogEntity>

    /** Reactive log for a slot, in replay order. */
    @Query("SELECT * FROM game_events WHERE slot_id = :slotId ORDER BY sequence ASC")
    fun observeForSlot(slotId: Long): Flow<List<EventLogEntity>>

    /** Highest sequence recorded for a slot, or null when the log is empty. */
    @Query("SELECT MAX(sequence) FROM game_events WHERE slot_id = :slotId")
    suspend fun maxSequence(slotId: Long): Long?

    /** Number of records for a slot. */
    @Query("SELECT COUNT(*) FROM game_events WHERE slot_id = :slotId")
    suspend fun countForSlot(slotId: Long): Int

    /** True when a record with this globally-unique id already exists. */
    @Query("SELECT EXISTS(SELECT 1 FROM game_events WHERE event_id = :eventId)")
    suspend fun containsEventId(eventId: String): Boolean

    /** All records across all slots - used by cloud sync to enumerate local logs. */
    @Query("SELECT * FROM game_events ORDER BY slot_id ASC, sequence ASC")
    suspend fun loadAll(): List<EventLogEntity>
}
