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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * NPC portrait composable — unified placeholder system.
 *
 * Renders a circular avatar with:
 *  - Letter initial (from [npcName]) on a gradient background seeded by [npcId]
 *  - An animated disposition ring whose arc length encodes [disposition] (-1..1)
 *  - An archetype badge dot colour-coded to [archetype]
 *
 * When a real portrait resource exists ([portraitResName] non-null), this
 * composable can be upgraded to load it via Coil without changing call sites.
 *
 * Usage:
 * ```kotlin
 * NpcPortrait(
 *     npcId = "warden",
 *     npcName = "The Warden",
 *     disposition = 0.4f,
 *     archetype = "SHIFTING_THE_BURDEN",
 *     size = 56.dp
 * )
 * ```
 */
@Composable
fun NpcPortrait(
    npcId: String,
    npcName: String,
    disposition: Float = 0f,
    archetype: String? = null,
    portraitResName: String? = null, // reserved for future Coil integration
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val clampedDisposition = disposition.coerceIn(-1f, 1f)

    // Animate disposition ring changes smoothly
    val ringProgress by animateFloatAsState(
        targetValue = (clampedDisposition + 1f) / 2f, // 0..1
        animationSpec = tween(600),
        label = "disposition_ring"
    )

    val ringColor = when {
        clampedDisposition > 0.2f  -> VoidGreen
        clampedDisposition > -0.2f -> EmberGold
        else                       -> HollowCrimson
    }

    val arcLength = ringProgress * 300f // max 300° to leave a gap at the bottom
    val arcStart  = 120f                // start angle (clockwise from right)

    val avatarGradient = npcGradient(npcId)
    val fontSize: TextUnit = (size.value * 0.38f).sp
    val strokeWidth = (size.value * 0.06f).dp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Disposition arc ring drawn behind the avatar circle
        Box(
            modifier = Modifier
                .size(size)
                .drawBehind {
                    val stroke = Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                    val inset = stroke.width / 2f
                    drawArc(
                        color = ringColor.copy(alpha = 0.25f),
                        startAngle = arcStart,
                        sweepAngle = 300f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(this.size.width - stroke.width, this.size.height - stroke.width),
                        style = stroke
                    )
                    if (arcLength > 2f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    ringColor.copy(alpha = 0f),
                                    ringColor.copy(alpha = 0.85f),
                                    ringColor
                                )
                            ),
                            startAngle = arcStart,
                            sweepAngle = arcLength,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(this.size.width - stroke.width, this.size.height - stroke.width),
                            style = stroke
                        )
                    }
                }
        )

        // Avatar circle — Coil AsyncImage when portraitResName is set, letter-gradient fallback otherwise
        if (portraitResName != null) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(portraitResName)
                    .crossfade(300)
                    .build(),
                contentDescription = npcName,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Medium,
                placeholder = null, // letter-avatar rendered below as fallback slot
                error = null,
                modifier = Modifier
                    .size(size * 0.82f)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size * 0.82f)
                    .clip(CircleShape)
                    .background(avatarGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = npcName.firstOrNull { it.isLetter() }?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Archetype badge dot (bottom-right)
        if (archetype != null) {
            Box(
                modifier = Modifier
                    .size(size * 0.22f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(archetypeColor(archetype))
                    .border(1.dp, MaterialTheme.colorScheme.background, CircleShape)
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Deterministic gradient per NPC id — same NPC always gets same colours. */
@Composable
private fun npcGradient(npcId: String): Brush {
    val hash = npcId.hashCode()
    val palettes = listOf(
        listOf(Color(0xFF6B3FA0), Color(0xFF2D1B69)),  // deep violet
        listOf(Color(0xFF8B1A1A), Color(0xFF4A0E0E)),  // chimera crimson
        listOf(Color(0xFF1A5C3A), Color(0xFF0D3320)),  // hollow green
        listOf(Color(0xFF5C4A1A), Color(0xFF2E2510)),  // ember amber
        listOf(Color(0xFF1A3A5C), Color(0xFF0D1F30)),  // deep ocean
        listOf(Color(0xFF5C1A3A), Color(0xFF30101F)),  // blood rose
        listOf(Color(0xFF3A3A5C), Color(0xFF1F1F30)),  // slate
        listOf(Color(0xFF5C3A1A), Color(0xFF301F0D)),  // rust
    )
    val palette = palettes[Math.abs(hash) % palettes.size]
    return Brush.linearGradient(colors = palette)
}

/** Maps archetype strings to badge indicator colours. */
private fun archetypeColor(archetype: String): Color = when (archetype) {
    "SHIFTING_THE_BURDEN"      -> Color(0xFF9B59B6)  // purple — dependency
    "ESCALATION"               -> Color(0xFFE74C3C)  // red — conflict
    "GROWTH_AND_UNDERINVESTMENT" -> Color(0xFF27AE60) // green — growth
    "FIXES_THAT_FAIL"          -> Color(0xFFF39C12)  // amber — warning
    else                       -> Color(0xFF95A5A6)  // gray — unknown
}
