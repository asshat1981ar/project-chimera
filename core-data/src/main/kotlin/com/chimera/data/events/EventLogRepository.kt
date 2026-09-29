package com.chimera.data.events

import com.chimera.database.dao.EventLogDao
import com.chimera.database.entity.EventLogEntity
import com.chimera.model.GameEvent
import com.chimera.model.GameEventCodec
import com.chimera.model.GameEventRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Append-only event log repository (ADR-002).
 *
 * This is the **write path of the authority model**: every state-changing action is
 * appended here as an immutable [GameEventRecord]. Nothing in this class mutates or
 * deletes a record.
 *
 * Sequence allocation is per slot and monotonic. It is derived from the current max
 * sequence in the log, so it is correct after a process restart and after a cloud pull
 * that brought in records from another device.
 */
@Singleton
class EventLogRepository @Inject constructor(
    private val eventLogDao: EventLogDao
) {

    /**
     * Appends [event] to [slotId]'s log, allocating the next sequence.
     *
     * @return the appended record, or `null` if an identical record was already present
     *         (idempotent re-append - safe to retry).
     */
    suspend fun append(slotId: Long, event: GameEvent, occurredAt: Long = System.currentTimeMillis()): GameEventRecord? {
        val sequence = nextSequence(slotId)
        val record = GameEventRecord(
            eventId = UUID.randomUUID().toString(),
            slotId = slotId,
            sequence = sequence,
            occurredAt = occurredAt,
            event = event
        )
        val rowId = eventLogDao.append(record.toEntity())
        return if (rowId == -1L) null else record
    }

    /**
     * Appends a record that already carries its own identity/sequence - used when merging
     * a remote log. Idempotent: a record whose `(slotId, sequence)` or `eventId` already
     * exists is ignored.
     *
     * @return true if the record was newly inserted.
     */
    suspend fun appendRemote(record: GameEventRecord): Boolean =
        eventLogDao.append(record.toEntity()) != -1L

    /** Merges a batch of remote records; returns how many were newly inserted. */
    suspend fun mergeRemote(records: List<GameEventRecord>): Int {
        if (records.isEmpty()) return 0
        val inserted = eventLogDao.appendAll(records.map { it.toEntity() })
        return inserted.count { it != -1L }
    }

    /** Full log for a slot, in replay order. */
    suspend fun load(slotId: Long): List<GameEventRecord> =
        eventLogDao.loadForSlot(slotId).map { it.toRecord() }

    /** Incremental log for a slot from [fromSequence] (inclusive). */
    suspend fun loadFrom(slotId: Long, fromSequence: Long): List<GameEventRecord> =
        eventLogDao.loadForSlotFrom(slotId, fromSequence).map { it.toRecord() }

    /** Reactive log for a slot, in replay order. */
    fun observe(slotId: Long): Flow<List<GameEventRecord>> =
        eventLogDao.observeForSlot(slotId).map { rows -> rows.map { it.toRecord() } }

    /** Next sequence to allocate for a slot (0 when the log is empty). */
    suspend fun nextSequence(slotId: Long): Long =
        (eventLogDao.maxSequence(slotId) ?: -1L) + 1L

    /** Number of records in a slot's log. */
    suspend fun count(slotId: Long): Int = eventLogDao.countForSlot(slotId)

    /** All records across all slots - used by cloud sync. */
    suspend fun loadAll(): List<GameEventRecord> = eventLogDao.loadAll().map { it.toRecord() }

    private fun GameEventRecord.toEntity() = EventLogEntity(
        eventId = eventId,
        slotId = slotId,
        sequence = sequence,
        occurredAt = occurredAt,
        schemaVersion = schemaVersion,
        eventType = event::class.simpleName ?: "Unknown",
        payloadJson = GameEventCodec.encode(this)
    )

    private fun EventLogEntity.toRecord(): GameEventRecord = GameEventCodec.decode(payloadJson)
}
