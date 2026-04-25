package com.chimera.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.*

/**
 * A parchment-styled stat bar for HP, XP, stamina, etc.
 * Iron track background with AgedGold fill and optional label.
 */
@Composable
fun ManuscriptStatBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    label: String = "",
    fillColor: Color = AgedGold,
    trackColor: Color = Iron,
    height: Dp = ManuscriptStatBarDefaults.height,
    cornerRadius: Dp = ManuscriptStatBarDefaults.cornerRadius,
    labelStyle: TextStyle = ManuscriptStatBarDefaults.labelStyle,
    animated: Boolean = true
) {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = clampedFraction,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "statbar-fill"
    )
    val displayFraction = if (animated) animatedFraction else clampedFraction

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = labelStyle,
                modifier = Modifier.padding(bottom = ChimeraSpacing.micro)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .shadow(ChimeraElevation.subtle, RoundedCornerShape(cornerRadius))
                .clip(RoundedCornerShape(cornerRadius))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = displayFraction)
                    .fillMaxHeight()
                    .background(fillColor)
            )
        }
    }
}

object ManuscriptStatBarDefaults {
    val height = 12.dp
    val cornerRadius = ChimeraCorners.small
    val labelStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = Vellum
    )
    val trackColor = Iron
}