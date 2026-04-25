package com.chimera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold

/**
 * Filigree corner decoration drawn as a Canvas composable.
 * Used for ornamenting card corners, section dividers, and
 * manuscript-style borders.
 */
@Composable
fun FiligreeDecoration(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = AgedGold,
    strokeWidth: Float = 1.5f
) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.value * density
        val halfS = s / 2f
        val cornerRadius = s * 0.15f

        // Outer swirl
        val outerPath = Path().apply {
            moveTo(0f, cornerRadius * 2)
            cubicTo(0f, cornerRadius, cornerRadius, 0f, cornerRadius * 2, 0f)
            lineTo(s - cornerRadius * 2, 0f)
            cubicTo(s - cornerRadius, 0f, s, cornerRadius, s, cornerRadius * 2)
        }
        drawPath(outerPath, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        // Center diamond
        val diamondPath = Path().apply {
            moveTo(halfS, halfS - s * 0.15f)
            lineTo(halfS + s * 0.1f, halfS)
            lineTo(halfS, halfS + s * 0.15f)
            lineTo(halfS - s * 0.1f, halfS)
            close()
        }
        drawPath(diamondPath, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        // Accent dots at cardinal points
        val dotRadius = strokeWidth * 0.8f
        drawCircle(color, radius = dotRadius, center = Offset(s * 0.25f, s * 0.25f))
        drawCircle(color, radius = dotRadius, center = Offset(s * 0.75f, s * 0.25f))
        drawCircle(color, radius = dotRadius, center = Offset(s * 0.25f, s * 0.75f))
        drawCircle(color, radius = dotRadius, center = Offset(s * 0.75f, s * 0.75f))
    }
}