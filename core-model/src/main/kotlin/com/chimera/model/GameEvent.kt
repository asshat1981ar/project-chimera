package com.chimera.model

sealed class GameEvent {
    data class SaveSlotSelected(val slotId: Long) : GameEvent()
    data class SaveSlotCreated(val slotId: Long, val playerName: String) : GameEvent()
    data class SceneEntered(val sceneId: String) : GameEvent()
    data class SceneCompleted(val sceneId: String) : GameEvent()
    data class DialogueTurnCompleted(
        val sceneId: String,
        val speakerId: String,
        val lineText: String
    ) : GameEvent()
    data class RelationshipChanged(
        val characterId: String,
        val delta: Float,
        val newValue: Float
    ) : GameEvent()
    data class CampPhaseStarted(val day: Int) : GameEvent()
    data class VowCreated(val vowId: String, val description: String) : GameEvent()
}
