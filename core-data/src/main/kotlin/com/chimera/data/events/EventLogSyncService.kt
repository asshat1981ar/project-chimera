package com.chimera.data.events

import com.chimera.model.GameEventCodec
import com.chimera.model.GameEventRecord
import com.chimera.network.CloudSaveRepository
import com.chimera.network.CloudSaveResult
import com.chimera.network.EventLogPushRequest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud save-sync for the event log (ADR-002 section 4).
 *
 * Replaces last-write-wins snapshot sync with **log sync**: devices exchange immutable
 * event records and merge by union on `(slotId, sequence)`. Because records are immutable
 * and totally ordered per slot, a merge can never conflict - the worst case is a duplicate
 * that the unique index silently ignores.
 *
 * Local-first: the Room log is always the source of truth. Every method returns a
 * [SyncOutcome] and never throws, so a network failure degrades to "not synced yet"
 * rather than blocking gameplay.
 */
@Singleton
class EventLogSyncService @Inject constructor(
    private val eventLogRepository: EventLogRepository,
    private val cloudSaveRepository: CloudSaveRepository
) {

    sealed interface SyncOutcome {
        /** @property pushed number of records sent; @property pulled number newly merged. */
        data class Success(val pushed: Int, val pulled: Int) : SyncOutcome
        data class Failure(val reason: String) : SyncOutcome
        /** Cloud save is not configured (no URL/token) - sync is a no-op. */
        data object NotConfigured : SyncOutcome
    }

    /**
     * Pushes local records for [slotId] that the server has not seen yet, then pulls any
     * remote records the device is missing.
     *
     * @param remoteMaxSequence highest sequence the server already holds (0 on first sync).
     */
    suspend fun sync(slotId: Long, remoteMaxSequence: Long = 0L): SyncOutcome {
        val push = push(slotId, remoteMaxSequence)
        if (push is SyncOutcome.Failure) return push
        val pull = pull(slotId, eventLogRepository.nextSequence(slotId))
        if (pull is SyncOutcome.Failure) return pull

        val pushed = (push as? SyncOutcome.Success)?.pushed ?: 0
        val pulled = (pull as? SyncOutcome.Success)?.pulled ?: 0
        return SyncOutcome.Success(pushed = pushed, pulled = pulled)
    }

    /** Pushes records with `sequence > remoteMaxSequence`. Idempotent on the server. */
    suspend fun push(slotId: Long, remoteMaxSequence: Long): SyncOutcome {
        val pending = eventLogRepository.loadFrom(slotId, remoteMaxSequence + 1L)
        if (pending.isEmpty()) return SyncOutcome.Success(pushed = 0, pulled = 0)

        val request = EventLogPushRequest(
            slotId = slotId,
            fromSequence = remoteMaxSequence + 1L,
            records = pending.map { GameEventCodec.encode(it) }
        )
        return when (val result = cloudSaveRepository.pushEventLog(request)) {
            is CloudSaveResult.Success -> SyncOutcome.Success(pushed = pending.size, pulled = 0)
            is CloudSaveResult.Failure -> {
                Timber.w("Event log push failed for slot %d: %s", slotId, result.reason)
                SyncOutcome.Failure(result.reason)
            }
        }
    }

    /** Pulls remote records from [fromSequence] and merges them into the local log. */
    suspend fun pull(slotId: Long, fromSequence: Long): SyncOutcome {
        return when (val result = cloudSaveRepository.pullEventLog(slotId, fromSequence)) {
            is CloudSaveResult.Success -> {
                val remote = result.data?.records.orEmpty()
                if (remote.isEmpty()) return SyncOutcome.Success(pushed = 0, pulled = 0)
                val records = remote.mapNotNull { raw ->
                    runCatching { GameEventCodec.decode(raw) }
                        .onFailure { Timber.w(it, "Skipping undecodable remote event record") }
                        .getOrNull()
                }
                val merged = eventLogRepository.mergeRemote(records)
                SyncOutcome.Success(pushed = 0, pulled = merged)
            }
            is CloudSaveResult.Failure -> {
                Timber.w("Event log pull failed for slot %d: %s", slotId, result.reason)
                SyncOutcome.Failure(result.reason)
            }
        }
    }

    /** Convenience: merge an explicit list of remote records (used by tests / import). */
    suspend fun mergeRemote(records: List<GameEventRecord>): Int =
        eventLogRepository.mergeRemote(records)
}
