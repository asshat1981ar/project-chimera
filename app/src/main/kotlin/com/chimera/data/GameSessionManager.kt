package com.chimera.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the currently active save slot for the play session.
 * Injected into ViewModels that need to scope queries to a specific save.
 */
@Singleton
class GameSessionManager @Inject constructor() {

    private val _activeSlotId = MutableStateFlow<Long?>(null)
    val activeSlotId: StateFlow<Long?> = _activeSlotId.asStateFlow()

    fun setActiveSlot(slotId: Long) {
        _activeSlotId.value = slotId
    }

    fun clearActiveSlot() {
        _activeSlotId.value = null
    }

    fun requireActiveSlotId(): Long =
        _activeSlotId.value ?: throw IllegalStateException("No active save slot")
}
