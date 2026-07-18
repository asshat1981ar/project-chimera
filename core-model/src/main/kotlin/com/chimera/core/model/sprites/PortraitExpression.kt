package com.chimera.core.model.sprites

/**
 * Expression states for NPC portraits.
 * Maps to ROADMAP Workstream C: NPC portrait and emotional telemetry.
 *
 * Pure Kotlin (no Android deps) so it can live in core-model and be
 * referenced by chimera-core-adjacent tests. Colors are stored as ARGB
 * hex Longs; UI code converts via Color(tintColorHex.toInt()).
 *
 * Sprite ID convention: "npc_{npcId}_{expressionName}" (e.g. "npc_elara_hostile").
 */
enum class PortraitExpression(
    /** Lowercase name used in sprite asset IDs and drawable base names. */
    val expressionName: String,
    /** ARGB tint applied over the portrait (ChimeraSpritePalette TINT_* values). */
    val tintColorHex: Long,
    /** 0.0–1.0; values > 0.3 enable the InkWashOverlay effect. */
    val inkWashIntensity: Float
) {
    NEUTRAL(
        expressionName = "neutral",
        tintColorHex = 0xFF000000,   // TINT_NEUTRAL (no tint applied)
        inkWashIntensity = 0.0f
    ),
    TENSE(
        expressionName = "tense",
        tintColorHex = 0xFF3A2010,   // TINT_TENSE (warm tension)
        inkWashIntensity = 0.2f
    ),
    WOUNDED(
        expressionName = "wounded",
        tintColorHex = 0xFF4A1010,   // TINT_WOUNDED (blood wash)
        inkWashIntensity = 0.6f
    ),
    GRATEFUL(
        expressionName = "grateful",
        tintColorHex = 0xFF1A3020,   // TINT_GRATEFUL (cool relief)
        inkWashIntensity = 0.1f
    ),
    HOSTILE(
        expressionName = "hostile",
        tintColorHex = 0xFF4A0A0A,   // TINT_HOSTILE (deep crimson)
        inkWashIntensity = 0.5f
    ),
    OATHBOUND(
        expressionName = "oathbound",
        tintColorHex = 0xFF1A1A3A,   // TINT_OATHBOUND (cool blue binding)
        inkWashIntensity = 0.25f
    );

    companion object {
        /**
         * Maps a deterministic-simulation disposition score (-1.0..1.0) to an
         * expression. Thresholds per SPRITE-DEVELOPMENT-PLAN §5.1. The sim
         * remains the source of truth; this is a pure display projection.
         */
        fun fromDisposition(disposition: Float): PortraitExpression = when {
            disposition > 0.7f  -> OATHBOUND
            disposition > 0.3f  -> GRATEFUL
            disposition > -0.2f -> NEUTRAL
            disposition > -0.5f -> TENSE
            disposition > -0.8f -> WOUNDED
            else                -> HOSTILE
        }

        /** Case-insensitive lookup by expressionName; null if unknown. */
        fun fromName(name: String): PortraitExpression? =
            entries.find { it.expressionName.equals(name, ignoreCase = true) }
    }
}
