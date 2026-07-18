package com.chimera.core.model.sprites

/**
 * Quest states for map node visualization.
 * Maps to ROADMAP Workstream A: quest visibility on the map.
 *
 * Pure Kotlin (no Android deps). Ring colors are ARGB hex Longs matching
 * ChimeraSpritePalette RING_* values; UI code converts via Color(ringColorHex.toInt()).
 *
 * Sprite ID convention: "map_{nodeType}_{stateName}" (e.g. "map_ruins_active").
 *
 * NEUTRAL is a display-only state for nodes with no quest marker — it renders
 * without a QuestStateRing (see MapNodeSprite in ChimeraSprite.kt).
 */
enum class MapNodeState(
    /** Lowercase name used in sprite asset IDs and drawable base names. */
    val stateName: String,
    /** ARGB ring color (ChimeraSpritePalette RING_* values). */
    val ringColorHex: Long,
    /** Base opacity for the node sprite in this state. */
    val defaultOpacity: Float,
    /** Whether the node should pulse (active quest attention cue). */
    val pulses: Boolean
) {
    NEUTRAL(
        stateName = "neutral",
        ringColorHex = 0xFF4A4540,   // INK_GREY (ring not drawn for NEUTRAL)
        defaultOpacity = 1.0f,
        pulses = false
    ),
    ACTIVE(
        stateName = "active",
        ringColorHex = 0xFFB8941F,   // RING_ACTIVE (faded gold leaf)
        defaultOpacity = 1.0f,
        pulses = true
    ),
    HIDDEN(
        stateName = "hidden",
        ringColorHex = 0xFF4A4540,   // RING_HIDDEN (diluted ink grey)
        defaultOpacity = 0.3f,
        pulses = false
    ),
    COMPLETED(
        stateName = "completed",
        ringColorHex = 0xFF1A3020,   // RING_COMPLETED (cool relief green)
        defaultOpacity = 1.0f,
        pulses = false
    ),
    FAILED(
        stateName = "failed",
        ringColorHex = 0xFF4A0A0A,   // RING_FAILED (deep crimson)
        defaultOpacity = 0.8f,
        pulses = false
    ),
    BLOCKED(
        stateName = "blocked",
        ringColorHex = 0xFF2D1B69,   // RING_BLOCKED (faint Hollow purple)
        defaultOpacity = 0.5f,
        pulses = false
    );

    companion object {
        /** Case-insensitive lookup by stateName; null if unknown. */
        fun fromName(name: String): MapNodeState? =
            entries.find { it.stateName.equals(name, ignoreCase = true) }
    }
}

/**
 * Projection from the existing quest model (core-model QuestObjectiveStatus)
 * onto display states. Keeps priority semantics consistent with MapScreen's
 * mostSignificantStatus(): FAILED > ACTIVE/HIDDEN > COMPLETED.
 *
 * Kept as a free function so core-model's quest types stay decoupled from
 * sprite display types; call sites: `node.questMarkers.mostSignificantStatus()
 * ?.toMapNodeState() ?: MapNodeState.NEUTRAL`.
 */
fun com.chimera.model.QuestObjectiveStatus.toMapNodeState(): MapNodeState = when (this) {
    com.chimera.model.QuestObjectiveStatus.ACTIVE -> MapNodeState.ACTIVE
    com.chimera.model.QuestObjectiveStatus.HIDDEN -> MapNodeState.HIDDEN
    com.chimera.model.QuestObjectiveStatus.COMPLETED,
    com.chimera.model.QuestObjectiveStatus.OPTIONAL_COMPLETED -> MapNodeState.COMPLETED
    com.chimera.model.QuestObjectiveStatus.FAILED -> MapNodeState.FAILED
}
