package com.chimera.core.simulation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Deterministic game state machine. Owns the canonical progression state.
 * No Android dependencies -- pure Kotlin, fully unit-testable.
 *
 * States: MainMenu → InScene → InCamp → InDuel → SceneComplete → GameOver
 */
class GameStateMachine {

    enum class GamePhase {
        MAIN_MENU,
        SAVE_SELECT,
        IN_SCENE,
        IN_CAMP,
        IN_DUEL,
        SCENE_COMPLETE,
        GAME_OVER
    }

    data class GameState(
        val phase: GamePhase = GamePhase.MAIN_MENU,
        val activeSceneId: String? = null,
        val activeSaveSlotId: Long? = null,
        val currentAct: Int = 1,
        val turnCount: Int = 0
    )

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    fun selectSave(slotId: Long) {
        require(_state.value.phase == GamePhase.MAIN_MENU || _state.value.phase == GamePhase.SAVE_SELECT)
        _state.value = _state.value.copy(
            phase = GamePhase.SAVE_SELECT,
            activeSaveSlotId = slotId
        )
    }

    fun enterScene(sceneId: String) {
        require(_state.value.activeSaveSlotId != null)
        _state.value = _state.value.copy(
            phase = GamePhase.IN_SCENE,
            activeSceneId = sceneId,
            turnCount = 0
        )
    }

    fun advanceTurn() {
        require(_state.value.phase == GamePhase.IN_SCENE)
        _state.value = _state.value.copy(
            turnCount = _state.value.turnCount + 1
        )
    }

    fun completeScene() {
        require(_state.value.phase == GamePhase.IN_SCENE)
        _state.value = _state.value.copy(
            phase = GamePhase.SCENE_COMPLETE,
            activeSceneId = null
        )
    }

    fun enterCamp() {
        _state.value = _state.value.copy(phase = GamePhase.IN_CAMP)
    }

    fun enterDuel(sceneId: String) {
        _state.value = _state.value.copy(
            phase = GamePhase.IN_DUEL,
            activeSceneId = sceneId
        )
    }

    fun returnToMenu() {
        _state.value = GameState()
    }

    fun advanceAct() {
        _state.value = _state.value.copy(
            currentAct = _state.value.currentAct + 1,
            phase = GamePhase.SCENE_COMPLETE
        )
    }
}
