package com.chimera.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Canonical, **serializable** game event hierarchy.
 *
 * Every state-changing action in Chimera is expressed as one of these events and appended
 * to the per-slot event log (see ADR-002). The log is the authority; game state is a fold
 * over it.
 *
 * ## Compatibility rules (do not break these)
 * - The sealed hierarchy is polymorphic: each subclass carries a stable [SerialName].
 *   **Never rename or reuse a `@SerialName`** - old logs must keep deserialising.
 * - Adding a new event type is safe (additive). Removing/renaming a field is not: add a
 *   new field with a default instead, and bump [GameEventRecord.schemaVersion] if the
 *   *meaning* of an existing field changes.
 * - All fields must have serializable types and, where a field is added later, a default
 *   value so older records still decode.
 */
@Serializable
sealed class GameEvent {

    @Serializable
    @SerialName("save_slot_selected")
    data class SaveSlotSelected(val slotId: Long) : GameEvent()

    @Serializable
    @SerialName("save_slot_created")
    data class SaveSlotCreated(val slotId: Long, val playerName: String) : GameEvent()

    @Serializable
    @SerialName("scene_entered")
    data class SceneEntered(val sceneId: String) : GameEvent()

    @Serializable
    @SerialName("scene_completed")
    data class SceneCompleted(val sceneId: String) : GameEvent()

    @Serializable
    @SerialName("dialogue_turn_completed")
    data class DialogueTurnCompleted(
        val sceneId: String,
        val speakerId: String,
        val lineText: String
    ) : GameEvent()

    @Serializable
    @SerialName("relationship_changed")
    data class RelationshipChanged(
        val characterId: String,
        val delta: Float,
        val newValue: Float
    ) : GameEvent()

    @Serializable
    @SerialName("camp_phase_started")
    data class CampPhaseStarted(val day: Int) : GameEvent()

    @Serializable
    @SerialName("vow_created")
    data class VowCreated(val vowId: String, val description: String) : GameEvent()

    /**
     * A deterministic combat/duel round outcome. The RNG seed is recorded so the round can
     * be replayed bit-identically (ADR-002 section 3).
     */
    @Serializable
    @SerialName("combat_round_resolved")
    data class CombatRoundResolved(
        val encounterId: String,
        val round: Int,
        val playerAction: String,
        val opponentAction: String,
        val outcome: String,
        val rngSeed: Long
    ) : GameEvent()

    /** Chapter/act progression. */
    @Serializable
    @SerialName("chapter_advanced")
    data class ChapterAdvanced(val fromTag: String, val toTag: String) : GameEvent()

    /** Faction standing mutation. */
    @Serializable
    @SerialName("faction_standing_changed")
    data class FactionStandingChanged(
        val factionId: String,
        val delta: Float,
        val newStanding: Float
    ) : GameEvent()

    /** Inventory mutation (item id + signed quantity). */
    @Serializable
    @SerialName("inventory_changed")
    data class InventoryChanged(
        val itemId: String,
        val quantityDelta: Int
    ) : GameEvent()

    /** Quest objective completion. */
    @Serializable
    @SerialName("objective_completed")
    data class ObjectiveCompleted(
        val questId: Long,
        val stepIndex: Int
    ) : GameEvent()
}
