package com.chimera.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chimera.core.model.sprites.PortraitExpression
import com.chimera.core.ui.sprites.LocalReducedMotion
import com.chimera.core.ui.theme.ChimeraSpritePalette

/**
 * Dialogue tone ring — a disposition-driven ring drawn around an NPC portrait.
 *
 * Maps to ROADMAP Workstream C: mood/disposition state is visible in dialogue
 * without exposing raw numeric scores. The ring color is a pure projection of
 * [PortraitExpression] (itself a pure projection of simulation disposition);
 * nothing here reads or writes simulation state.
 *
 * HOSTILE expressions pulse gently unless reduced motion is requested via
 * [LocalReducedMotion] (ROADMAP F: motion can be disabled or reduced).
 *
 * Usage:
 *   DialogueToneRing(expression = PortraitExpression.fromDisposition(d)) {
 *       NpcPortrait(...)
 *   }
 */

/** Ring color per expression. Bright enough to read on dark parchment;
 *  hues stay inside the ChimeraSpritePalette families. */
fun toneRingColor(expression: PortraitExpression): Color = when (expression) {
    PortraitExpression.NEUTRAL   -> ChimeraSpritePalette.INK_GREY
    PortraitExpression.TENSE     -> ChimeraSpritePalette.ACCENT_GOLD
    PortraitExpression.WOUNDED   -> Color(0xFF9C3A2E)   // ember red (failed-node ramp)
    PortraitExpression.GRATEFUL  -> Color(0xFF4F7D5C)   // verdigris (completed-node ramp)
    PortraitExpression.HOSTILE   -> ChimeraSpritePalette.ACCENT_CRIMSON
    PortraitExpression.OATHBOUND -> Color(0xFF8B7FBF)   // cool binding blue
}

/** Human-readable tone label for accessibility announcements. */
fun toneRingLabel(expression: PortraitExpression): String = when (expression) {
    PortraitExpression.NEUTRAL   -> "calm"
    PortraitExpression.TENSE     -> "tense"
    PortraitExpression.WOUNDED   -> "wounded"
    PortraitExpression.GRATEFUL  -> "grateful"
    PortraitExpression.HOSTILE   -> "hostile"
    PortraitExpression.OATHBOUND -> "oath-bound"
}

/** Whether the ring pulses for this expression (attention cue for hostility). */
fun toneRingPulses(expression: PortraitExpression): Boolean =
    expression == PortraitExpression.HOSTILE

@Composable
fun DialogueToneRing(
    expression: PortraitExpression,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val ringColor = toneRingColor(expression)
    val reducedMotion = LocalReducedMotion.current
    val shouldPulse = toneRingPulses(expression) && !reducedMotion

    val infiniteTransition = rememberInfiniteTransition(label = "tone_ring")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (shouldPulse) 0.45f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tone_ring_alpha"
    )
    val alpha = if (shouldPulse) pulseAlpha else 1f

    Box(
        modifier = modifier.semantics(mergeDescendants = true) {
            this.contentDescription =
                contentDescription ?: "NPC tone: ${toneRingLabel(expression)}"
        }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 2.dp.toPx()
            drawCircle(
                color = ringColor.copy(alpha = alpha),
                radius = (size.minDimension / 2f) - stroke,
                style = Stroke(width = stroke)
            )
        }
        Box(modifier = Modifier.padding(3.dp)) {
            content()
        }
    }
}
