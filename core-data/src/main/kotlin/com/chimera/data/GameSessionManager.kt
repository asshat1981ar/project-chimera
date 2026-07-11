package com.chimera.data

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
    private val analyticsTracker: AnalyticsTracker
) {

    private val _activeSlotId = MutableStateFlow<Long?>(null)
    val activeSlotId: StateFlow<Long?> = _activeSlotId.asStateFlow()

    private var sessionStartTime: Long = 0L

    // Fire-and-forget scope for analytics side effects -- scoped to this singleton's lifetime
    // (the app's), so it never needs explicit cancellation.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun setActiveSlot(slotId: Long) {
        _activeSlotId.value = slotId
        sessionStartTime = System.currentTimeMillis()
        scope.launch { analyticsTracker.logEvent("session_start") }
    }

    fun clearActiveSlot() {
        _activeSlotId.value = null
        sessionStartTime = 0L
    }

    fun requireActiveSlotId(): Long =
        _activeSlotId.value ?: throw IllegalStateException("No active save slot")

    /** Returns elapsed play time in seconds since slot was activated. */
    fun getSessionPlaytimeSeconds(): Long {
        if (sessionStartTime == 0L) return 0L
        return (System.currentTimeMillis() - sessionStartTime) / 1000
    }
}
