package com.chimera.core.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class GameEventBus {
    private val _eventFlow = MutableSharedFlow<GameEvent>()
    val eventFlow: SharedFlow<GameEvent> = _eventFlow.asSharedFlow()
    
    suspend fun emit(event: GameEvent) {
        _eventFlow.emit(event)
    }
}

sealed class GameEvent
sealed class EmotionalEvent : GameEvent()