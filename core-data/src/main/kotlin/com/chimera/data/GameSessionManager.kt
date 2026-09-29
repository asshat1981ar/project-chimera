package com.chimera.data

import com.chimera.data.events.GameEventRecorder
import com.chimera.model.GameEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameSessionManager @Inject constructor(
    private val eventRecorder: GameEventRecorder
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _activeSlotId = MutableStateFlow<Long?>(null)
    val activeSlotId: StateFlow<Long?> = _activeSlotId.asStateFlow()

    private var sessionStartTime: Long = 0L

    fun setActiveSlot(slotId: Long) {
        _activeSlotId.value = slotId
        sessionStartTime = System.currentTimeMillis()
        // ADR-002: the active slot's event log is the authority - start recording it.
        eventRecorder.start(slotId)
        scope.launch { eventRecorder.record(slotId, GameEvent.SaveSlotSelected(slotId)) }
    }

    fun clearActiveSlot() {
        _activeSlotId.value = null
        sessionStartTime = 0L
        eventRecorder.stop()
    }

    fun requireActiveSlotId(): Long =
        _activeSlotId.value ?: throw IllegalStateException("No active save slot")

    /** Returns elapsed play time in seconds since slot was activated. */
    fun getSessionPlaytimeSeconds(): Long {
        if (sessionStartTime == 0L) return 0L
        return (System.currentTimeMillis() - sessionStartTime) / 1000
    }
}
