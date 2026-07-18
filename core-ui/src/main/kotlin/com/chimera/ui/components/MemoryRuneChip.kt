package com.chimera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chimera.core.ui.theme.ChimeraSpritePalette

/**
 * Memory rune chip — a small rune + label showing what an NPC carries about
 * the player, without exposing raw numeric scores.
 *
 * Maps to ROADMAP Workstream C: "Dialogue screen can show memory chips
 * without exposing raw numeric scores."
 *
 * Chips are derived by [memoryRunesForDisposition], a pure projection of the
 * deterministic disposition score with thresholds aligned to
 * PortraitExpression.fromDisposition (SPRITE-DEVELOPMENT-PLAN §5.1). The sim
 * remains the source of truth; this is display-only.
 */
enum class MemoryRune(
    /** Player-facing chip label. */
    val label: String,
    /** ARGB rune color. */
    val color: Color
) {
    /** The NPC carries any significant memory of the player. */
    REMEMBERED("Remembered", ChimeraSpritePalette.INK_GREY),

    /** The relationship has been harmed. */
    WOUNDED("Wounded", Color(0xFF9C3A2E)),

    /** The NPC is wary but not yet wounded by the player. */
    SUSPICIOUS("Suspicious", ChimeraSpritePalette.ACCENT_GOLD),

    /** The NPC owes the player goodwill. */
    GRATEFUL("Grateful", Color(0xFF4F7D5C)),

    /** A vow binds the NPC to the player (authored vows, disposition > 0.7). */
    OATH_BOUND("Oath-bound", Color(0xFF8B7FBF))
}

/**
 * Pure projection from simulation disposition (-1.0..1.0) to the memory runes
 * shown in dialogue. Thresholds mirror PortraitExpression.fromDisposition so
 * portrait expression, tone ring, and memory chips never contradict.
 *
 * Returns at most two chips, most specific first. Neutral dispositions near
 * zero return an empty list — a stranger shows no runes.
 */
fun memoryRunesForDisposition(disposition: Float): List<MemoryRune> {
    val runes = mutableListOf<MemoryRune>()
    when {
        disposition > 0.7f -> runes += MemoryRune.OATH_BOUND
        disposition > 0.3f -> runes += MemoryRune.GRATEFUL
        disposition > -0.2f -> Unit
        disposition > -0.5f -> runes += MemoryRune.SUSPICIOUS
        disposition > -0.8f -> runes += MemoryRune.WOUNDED
        else -> runes += MemoryRune.WOUNDED
    }
    if (disposition >= 0.15f || disposition <= -0.15f) runes += MemoryRune.REMEMBERED
    return runes.take(2)
}

/**
 * A single memory rune chip: drawn rune glyph + label. The glyph differs per
 * rune so state never relies on color alone (ROADMAP D/G accessibility).
 */
@Composable
fun MemoryRuneChip(
    rune: MemoryRune,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .height(20.dp)
            .border(1.dp, rune.color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .testTag("memory_rune_${rune.name.lowercase()}")
            .semantics { contentDescription = "Memory: ${rune.label}" }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            RuneGlyph(rune = rune, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = rune.label,
                style = MaterialTheme.typography.labelSmall,
                color = rune.color
            )
        }
    }
}

/**
 * Drawn rune glyph; the shape differs per rune so state never relies on
 * color alone (ROADMAP D/G accessibility).
 */
@Composable
private fun RuneGlyph(rune: MemoryRune, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val c = rune.color
        val w = size.width
        val h = size.height
        when (rune) {
            // Eye-slit: the NPC remembers
            MemoryRune.REMEMBERED -> {
                drawLine(c, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), strokeWidth = 2f)
                drawCircle(c, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.5f))
            }
            // Broken slash: the bond was harmed
            MemoryRune.WOUNDED -> {
                drawLine(c, Offset(w * 0.25f, h * 0.15f), Offset(w * 0.55f, h * 0.55f), strokeWidth = 2f)
                drawLine(c, Offset(w * 0.45f, h * 0.5f), Offset(w * 0.75f, h * 0.9f), strokeWidth = 2f)
            }
            // Watchful triangle: wary
            MemoryRune.SUSPICIOUS -> {
                drawLine(c, Offset(w * 0.5f, h * 0.15f), Offset(w * 0.85f, h * 0.85f), strokeWidth = 2f)
                drawLine(c, Offset(w * 0.85f, h * 0.85f), Offset(w * 0.15f, h * 0.85f), strokeWidth = 2f)
                drawLine(c, Offset(w * 0.15f, h * 0.85f), Offset(w * 0.5f, h * 0.15f), strokeWidth = 2f)
            }
            // Open sprout: goodwill
            MemoryRune.GRATEFUL -> {
                drawLine(c, Offset(w * 0.5f, h * 0.85f), Offset(w * 0.5f, h * 0.3f), strokeWidth = 2f)
                drawLine(c, Offset(w * 0.5f, h * 0.55f), Offset(w * 0.2f, h * 0.3f), strokeWidth = 2f)
                drawLine(c, Offset(w * 0.5f, h * 0.55f), Offset(w * 0.8f, h * 0.3f), strokeWidth = 2f)
            }
            // Bound knot: vow
            MemoryRune.OATH_BOUND -> {
                drawCircle(c, radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.5f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                drawLine(c, Offset(w * 0.5f, h * 0.18f), Offset(w * 0.5f, h * 0.82f), strokeWidth = 2f)
            }
        }
    }
}
