package com.chimera.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@Composable
fun NpcPortrait(
    npcId: String,
    npcName: String,
    disposition: Float = 0f,
    archetype: String? = null,
    portraitResName: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val clampedDisposition = disposition.coerceIn(-1f, 1f)

    val ringProgress by animateFloatAsState(
        targetValue = (clampedDisposition + 1f) / 2f,
        animationSpec = tween(600),
        label = "disposition_ring"
    )

    val backgroundGradient = Brush.radialGradient(
        colors = listOf(
            gradientColorFor(npcId, 0.7f),
            gradientColorFor(npcId, 0.3f)
        )
    )

    val initial = npcInitial(npcName)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundGradient)
            .drawDispositionRing(ringProgress, clampedDisposition),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        archetype?.let {
            Box(
                modifier = Modifier
                    .offset(x = size * 0.35f, y = size * 0.35f)
                    .size(size * 0.2f)
                    .clip(CircleShape)
                    .background(archetypeColor(it))
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            )
        }
    }
}

private fun Modifier.drawDispositionRing(
    progress: Float,
    disposition: Float
): Modifier = this.drawBehind {
    val ringColor = when {
        disposition > 0.3f -> VoidGreen
        disposition < -0.3f -> HollowCrimson
        else -> EmberGold
    }
    val strokeWidth = size.minDimension * 0.06f
    val radius = (size.minDimension - strokeWidth) / 2f

    drawArc(
        color = ringColor,
        startAngle = -90f,
        sweepAngle = 360f * progress,
        useCenter = false,
        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun gradientColorFor(seed: String, factor: Float): Color {
    val hash = seed.hashCode()
    val r = ((hash shr 16) and 0xFF) / 255f * factor
    val g = ((hash shr 8) and 0xFF) / 255f * factor
    val b = (hash and 0xFF) / 255f * factor
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
}

private fun archetypeColor(archetype: String): Color = when {
    archetype.contains("SHIFTING") -> VoidGreen
    archetype.contains("PRESERVING") -> EmberGold
    archetype.contains("RESTORING") -> Color(0xFF6B9BD1)
    else -> Color(0xFF888888)
}

internal fun npcInitial(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "?"
    if (trimmed.all { it.isDigit() }) return "?"

    val words = trimmed.split("\\s+".toRegex())
    val letters = mutableListOf<String>()

    for (word in words) {
        val firstLetter = word.firstOrNull { it.isLetter() }
        if (firstLetter != null) {
            letters.add(firstLetter.uppercase())
        }
        if (letters.size >= 2) break
    }

    if (letters.size == 1 && words.size == 1) {
        val word = words[0]
        val firstLetterIdx = word.indexOfFirst { it.isLetter() }
        if (firstLetterIdx >= 0 && firstLetterIdx + 1 < word.length) {
            val nextChar = word[firstLetterIdx + 1]
            if (nextChar.isDigit()) {
                letters.add(nextChar.toString())
            }
        }
    }

    return if (letters.isEmpty()) "?" else letters.joinToString("").take(2)
}
