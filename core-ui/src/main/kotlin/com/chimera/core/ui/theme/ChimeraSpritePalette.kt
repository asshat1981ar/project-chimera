package com.chimera.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Sprite and atmosphere color palette for Chimera's gothic manuscript theme.
 *
 * This palette extends the base ChimeraTheme with colors specific to
 * the ink-wash sprite system. All sprite composables reference these
 * values for consistent tinting, overlays, and fallback rendering.
 *
 * Maps to ROADMAP Workstream F: Atmosphere and visual system.
 */
object ChimeraSpritePalette {
    // Primary parchment backgrounds (dark, aged)
    val PARCHMENT_DARK   = Color(0xFF1A1410)   // Deep aged parchment
    val PARCHMENT_MID    = Color(0xFF2A2018)   // Medium parchment
    val PARCHMENT_LIGHT  = Color(0xFF3A3028)   // Light parchment

    // Ink tones (sumi-e inspired)
    val INK_BLACK        = Color(0xFF0A0806)   // Pure sumi ink
    val INK_CHARCOAL     = Color(0xFF1E1A16)   // Charcoal wash
    val INK_GREY         = Color(0xFF4A4540)   // Diluted ink

    // Accent colors (restrained use - single accent per sprite max)
    val ACCENT_CRIMSON   = Color(0xFF8B2500)   // Dried blood / seal wax
    val ACCENT_GOLD      = Color(0xFFB8941F)   // Faded gold leaf
    val ACCENT_ASH       = Color(0xFF6B6B6B)   // Ash grey
    val ACCENT_HOLLOW    = Color(0xFF2D1B69)   // Faint purple (Hollow magic)

    // Expression-specific tints for NPC portraits
    val TINT_NEUTRAL     = Color(0xFF000000)   // No tint
    val TINT_TENSE       = Color(0xFF3A2010)   // Warm tension
    val TINT_WOUNDED     = Color(0xFF4A1010)   // Blood wash
    val TINT_GRATEFUL    = Color(0xFF1A3020)   // Cool relief
    val TINT_HOSTILE     = Color(0xFF4A0A0A)   // Deep crimson
    val TINT_OATHBOUND   = Color(0xFF1A1A3A)   // Cool blue binding

    // Map node state ring colors
    val RING_ACTIVE      = ACCENT_GOLD
    val RING_HIDDEN      = INK_GREY
    val RING_COMPLETED   = TINT_GRATEFUL
    val RING_FAILED      = TINT_HOSTILE
    val RING_BLOCKED     = ACCENT_HOLLOW

    // Rarity seal colors
    val SEAL_COMMON      = Color(0xFF9E9E9E)
    val SEAL_UNCOMMON    = Color(0xFF4CAF50)
    val SEAL_RARE        = Color(0xFF2196F3)
    val SEAL_EPIC        = Color(0xFF9C27B0)
    val SEAL_LEGENDARY   = Color(0xFFFFD700)

    // UI chrome
    val FRAME_BORDER     = INK_GREY
    val FRAME_GOLD       = ACCENT_GOLD.copy(alpha = 0.7f)
    val SELECTION_GLOW   = ACCENT_GOLD.copy(alpha = 0.3f)

    // Combat health gradients
    val HEALTH_HIGH      = TINT_GRATEFUL
    val HEALTH_MID       = TINT_TENSE
    val HEALTH_LOW       = TINT_WOUNDED
    val HEALTH_CRITICAL  = ACCENT_CRIMSON
}
