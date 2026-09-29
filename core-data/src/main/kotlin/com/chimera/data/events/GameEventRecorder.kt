package com.chimera.data.events

import com.chimera.core.events.GameEventBus
import com.chimera.model.GameEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges the in-memory [GameEventBus] to the durable event log (ADR-002).
 *
 * Every event emitted on the bus is appended to the active slot's log. This is the seam
 * that turns the existing fire-and-forget event bus into the authority: the bus stays the
 * in-process notification channel, the log becomes the record of truth.
 *
 * The recorder is deliberately **fail-soft**: a logging failure must never crash gameplay.
 * Failures are reported through [onRecordError] so callers can surface a sync warning.
 */
@Singleton
class GameEventRecorder @Inject constructor(
    private val eventLogRepository: EventLogRepository,
    private val gameEventBus: GameEventBus
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** The active collection job; cancelled and replaced when the slot changes. */
    private var collectJob: Job? = null

    /** Set by the app layer to observe recording failures (e.g. to show a sync warning). */
    @Volatile
    var onRecordError: ((Throwable) -> Unit)? = null

    /**
     * Starts recording bus events for [slotId]. Call once the active slot is known
     * (i.e. after `GameSessionManager.setActiveSlot`).
     *
     * Idempotent: calling it again (e.g. on a slot switch) cancels the previous collector
     * first, so events are never appended twice.
     */
    @Synchronized
    fun start(slotId: Long) {
        collectJob?.cancel()
        collectJob = scope.launch {
            gameEventBus.eventFlow.collect { event ->
                record(slotId, event)
            }
        }
    }

    /** Stops recording (e.g. when the player returns to the main menu). */
    @Synchronized
    fun stop() {
        collectJob?.cancel()
        collectJob = null
    }

    /** Appends a single event to [slotId]'s log. Never throws. */
    suspend fun record(slotId: Long, event: GameEvent) {
        runCatching { eventLogRepository.append(slotId, event) }
            .onFailure { error ->
                Timber.w(error, "Failed to append %s to event log for slot %d", event::class.simpleName, slotId)
                onRecordError?.invoke(error)
            }
    }
}
