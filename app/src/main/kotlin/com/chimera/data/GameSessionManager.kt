package com.chimera.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameSessionManager @Inject constructor() {

    private val _activeSlotId = MutableStateFlow<Long?>(null)
    val activeSlotId: StateFlow<Long?> = _activeSlotId.asStateFlow()

    private var sessionStartTime: Long = 0L

    fun setActiveSlot(slotId: Long) {
        _activeSlotId.value = slotId
        sessionStartTime = System.currentTimeMillis()
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
