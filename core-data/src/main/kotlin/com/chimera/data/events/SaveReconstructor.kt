package com.chimera.data.events

import com.chimera.core.simulation.SaveStateReducer
import com.chimera.model.GameEventRecord
import com.chimera.model.SaveState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reconstructs a save slot's state by replaying its event log (ADR-002).
 *
 * This is the **read path of the authority model** and the concrete exit criterion of
 * Milestone E: *a save can be reconstructed by replaying its event log*.
 *
 * Because [SaveStateReducer] is pure, reconstruction is deterministic - the same log
 * always yields the same [SaveState], on any device, in any process.
 */
@Singleton
class SaveReconstructor @Inject constructor(
    private val eventLogRepository: EventLogRepository
) {

    /** Replays the full log for [slotId] from sequence 0. */
    suspend fun reconstruct(slotId: Long): SaveState =
        SaveStateReducer.reduceAll(eventLogRepository.load(slotId), SaveState(slotId = slotId))

    /**
     * Replays the log up to and including [upToSequence] - the primitive behind undo /
     * rewind / "load an earlier point".
     */
    suspend fun reconstructAt(slotId: Long, upToSequence: Long): SaveState {
        val records = eventLogRepository.load(slotId).takeWhile { it.sequence <= upToSequence }
        return SaveStateReducer.reduceAll(records, SaveState(slotId = slotId))
    }

    /** Replays an already-loaded log (used by tests and by cloud-merge verification). */
    fun reconstructFrom(records: List<GameEventRecord>, slotId: Long = 0L): SaveState =
        SaveStateReducer.reduceAll(records, SaveState(slotId = slotId))
}
