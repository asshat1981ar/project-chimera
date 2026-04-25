package com.chimera.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp

@Composable
fun DispositionHeart(
    filledFraction: Float,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val clampedFraction = filledFraction.coerceIn(0f, 1f)
    
    Canvas(modifier = modifier.size(24.dp)) {
        val heartPath = Path().apply {
            val w = size.width
            val h = size.height
            moveTo(w / 2, h * 0.85f)
            cubicTo(w * 0.15f, h * 0.65f, 0f, h * 0.4f, w * 0.2f, h * 0.25f)
            cubicTo(w * 0.35f, h * 0.1f, w * 0.5f, h * 0.2f, w / 2, h * 0.35f)
            cubicTo(w * 0.5f, h * 0.2f, w * 0.65f, h * 0.1f, w * 0.8f, h * 0.25f)
            cubicTo(w * 1f, h * 0.4f, w * 0.85f, h * 0.65f, w / 2, h * 0.85f)
        }
        
        drawPath(path = heartPath, color = color.copy(alpha = 0.5f), style = Stroke(width = 1.5f, cap = StrokeCap.Round))
        
        if (clampedFraction > 0f) {
            val fillHeight = size.height * (1 - clampedFraction)
            clipRect(left = 0f, top = fillHeight, right = size.width, bottom = size.height) {
                drawPath(path = heartPath, color = color)
            }
        }
    }
}

@Composable
fun FactionStandingIndicator(
    standing: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        standing > 0.3f -> MaterialTheme.colorScheme.primary
        standing < -0.3f -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    
    Canvas(modifier = modifier.size(16.dp, 8.dp)) {
        drawLine(
            color = color.copy(alpha = 0.3f),
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        val indicatorX = ((standing + 1f) / 2f) * size.width
        drawCircle(color = color, radius = 4f, center = Offset(indicatorX, size.height / 2))
    }
}

@Composable
fun SwordsIcon(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = Stroke(width = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(6f, 18f), Offset(14f, 10f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(18f, 18f), Offset(10f, 10f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(6f, 6f), Offset(14f, 14f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(18f, 6f), Offset(10f, 14f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    }
}

@Composable
fun CampfireIcon(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        drawLine(color, Offset(12f, 8f), Offset(8f, 16f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(12f, 8f), Offset(16f, 14f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(12f, 8f), Offset(12f, 18f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(4f, 20f), Offset(20f, 20f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(6f, 18f), Offset(10f, 20f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(14f, 20f), Offset(18f, 18f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    }
}

@Composable
fun ScrollIcon(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        drawLine(color, Offset(8f, 4f), Offset(8f, 20f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(8f, 4f), Offset(16f, 4f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(8f, 20f), Offset(16f, 20f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(16f, 4f), Offset(16f, 20f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(8f, 9f), Offset(13f, 9f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(8f, 14f), Offset(13f, 14f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    }
}

@Composable
fun ShieldIcon(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(12f, 4f)
            lineTo(5f, 7f)
            lineTo(5f, 14f)
            lineTo(12f, 20f)
            lineTo(19f, 14f)
            lineTo(19f, 7f)
            close()
        }
        drawPath(path, color, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
    }
}

@Composable
fun CompassIcon(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        drawCircle(color, radius = 9f, center = center, style = Stroke(width = 1.5f))
        drawLine(color, Offset(12f, 5f), Offset(12f, 19f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(5f, 12f), Offset(19f, 12f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(12f, 5f), Offset(14f, 8f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    }
}
