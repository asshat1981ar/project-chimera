package com.chimera.core.simulation

import com.chimera.model.GameEvent
import com.chimera.model.GameEventRecord
import com.chimera.model.SaveState

/**
 * The **single mutation path** for game state (ADR-002).
 *
 * `SaveStateReducer` is a pure, deterministic fold:
 *
 * ```
 * reduce(state, record) -> state'
 * ```
 *
 * It has no Android, Hilt, I/O or clock dependencies, so the same reducer runs on the
 * device, in unit tests, and (in a future `core-session` module) on a multiplayer host.
 * Because it is pure, replaying the same log always yields the same state - that is the
 * determinism guarantee the whole authority model rests on.
 *
 * ## Ordering contract
 * Records MUST be applied in ascending [GameEventRecord.sequence] order, gap-free from 0.
 * [reduceAll] enforces this and throws on a gap or an out-of-order record, so a corrupt
 * log fails loudly instead of silently producing a wrong save.
 */
object SaveStateReducer {

    /** Applies a single record. Pure - returns a new [SaveState]. */
    fun reduce(state: SaveState, record: GameEventRecord): SaveState {
        val next = applyEvent(state, record.event)
        return next.copy(
            slotId = if (record.slotId != 0L) record.slotId else next.slotId,
            lastSequence = record.sequence
        )
    }

    /**
     * Folds a whole log. Enforces the ordering contract: sequences must start at 0 and
     * increase by exactly 1.
     *
     * @throws IllegalStateException if the log has a gap or is out of order.
     */
    fun reduceAll(records: List<GameEventRecord>, initial: SaveState = SaveState()): SaveState {
        var state = initial
        var expected = 0L
        for (record in records) {
            check(record.sequence == expected) {
                "Event log out of order: expected sequence $expected but found ${record.sequence} " +
                    "(slot ${record.slotId})"
            }
            state = reduce(state, record)
            expected++
        }
        return state
    }

    private fun applyEvent(state: SaveState, event: GameEvent): SaveState = when (event) {
        is GameEvent.SaveSlotSelected ->
            state.copy(slotId = event.slotId)

        is GameEvent.SaveSlotCreated ->
            state.copy(slotId = event.slotId, playerName = event.playerName)

        is GameEvent.SceneEntered ->
            state.copy(activeSceneId = event.sceneId)

        is GameEvent.SceneCompleted ->
            state.copy(
                activeSceneId = null,
                completedScenes = (state.completedScenes + event.sceneId).distinct()
            )

        is GameEvent.DialogueTurnCompleted ->
            state.copy(dialogueTurnCount = state.dialogueTurnCount + 1)

        is GameEvent.RelationshipChanged ->
            state.copy(
                relationships = state.relationships + (event.characterId to event.newValue)
            )

        is GameEvent.CampPhaseStarted ->
            state.copy(campDay = event.day)

        is GameEvent.VowCreated ->
            state.copy(vows = (state.vows + event.vowId).distinct())

        is GameEvent.CombatRoundResolved ->
            // Combat outcomes are recorded, not re-simulated here: the engine already
            // resolved them deterministically from the recorded seed. The reducer only
            // needs to know the encounter happened.
            state

        is GameEvent.ChapterAdvanced ->
            state.copy(
                chapterTag = event.toTag,
                currentAct = actForTag(event.toTag, state.currentAct)
            )

        is GameEvent.FactionStandingChanged ->
            state.copy(
                factionStandings = state.factionStandings + (event.factionId to event.newStanding)
            )

        is GameEvent.InventoryChanged -> {
            val current = state.inventory[event.itemId] ?: 0
            val updated = (current + event.quantityDelta).coerceAtLeast(0)
            state.copy(
                inventory = if (updated == 0) {
                    state.inventory - event.itemId
                } else {
                    state.inventory + (event.itemId to updated)
                }
            )
        }

        is GameEvent.ObjectiveCompleted ->
            state.copy(
                completedObjectives =
                    (state.completedObjectives + "${event.questId}:${event.stepIndex}").distinct()
            )
    }

    /** Maps a chapter tag to its act number, preserving the current act if unknown. */
    private fun actForTag(tag: String, fallback: Int): Int = when {
        tag.startsWith("act1") || tag == "prologue" -> 1
        tag.startsWith("act2") -> 2
        tag.startsWith("act3") -> 3
        else -> fallback
    }
}
