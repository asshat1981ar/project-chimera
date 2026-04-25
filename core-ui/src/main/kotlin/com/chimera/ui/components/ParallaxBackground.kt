package com.chimera.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.chimera.ui.theme.AtmospherePalette
import com.chimera.ui.theme.SceneAtmosphere
import com.chimera.ui.theme.AtmosphereTokens
import kotlin.math.PI
import kotlin.math.sin

/**
 * Parallax background renderer with placeholder layers for future art assets.
 * 
 * Supports three layers:
 * - Far background: Large shapes, slow movement
 * - Mid layer: Medium shapes, moderate movement  
 * - Near layer: Small details, fast movement
 */
@Composable
fun ParallaxBackground(
    sceneAtmosphere: SceneAtmosphere,
    parallaxOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier
) {
    val palette = AtmosphereTokens.paletteFor(sceneAtmosphere)
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = parallaxOffset.x,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "parallaxX"
    )
    
    val animatedOffsetY by animateFloatAsState(
        targetValue = parallaxOffset.y,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "parallaxY"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            drawFarLayer(width, height, animatedOffsetX * 0.1f, animatedOffsetY * 0.1f, palette)
            drawMidLayer(width, height, animatedOffsetX * 0.3f, animatedOffsetY * 0.3f, palette)
            drawNearLayer(width, height, animatedOffsetX * 0.6f, animatedOffsetY * 0.6f, palette)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFarLayer(
    width: Float,
    height: Float,
    offsetX: Float,
    offsetY: Float,
    palette: AtmospherePalette
) {
    drawCircle(
        color = palette.surface.copy(alpha = 0.3f),
        radius = width * 0.8f,
        center = Offset(width * 0.3f + offsetX, height * 1.2f + offsetY)
    )
    
    drawCircle(
        color = palette.surface.copy(alpha = 0.25f),
        radius = width * 0.6f,
        center = Offset(width * 0.7f + offsetX, height * 1.3f + offsetY)
    )
    
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            palette.accent.copy(alpha = 0.1f),
            Color.Transparent
        ),
        startY = 0f,
        endY = height * 0.4f
    )
    drawRect(brush = gradientBrush)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMidLayer(
    width: Float,
    height: Float,
    offsetX: Float,
    offsetY: Float,
    palette: AtmospherePalette
) {
    val midColor = palette.elevated.copy(alpha = 0.4f)
    val pillarPositions = listOf(0.15f, 0.35f, 0.55f, 0.75f, 0.9f)
    
    pillarPositions.forEachIndexed { index, pos ->
        val x = width * pos + offsetX
        val pillarHeight = height * (0.2f + (index % 3) * 0.1f)
        
        drawLine(
            color = midColor,
            start = Offset(x, height * 0.7f + offsetY),
            end = Offset(x, height * 0.7f - pillarHeight + offsetY),
            strokeWidth = 8f + (index % 2) * 4f
        )
    }
    
    drawCircle(
        color = palette.accent.copy(alpha = 0.08f),
        radius = width * 0.4f,
        center = Offset(width * 0.5f + offsetX * 0.5f, height * 0.3f + offsetY * 0.5f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNearLayer(
    width: Float,
    height: Float,
    offsetX: Float,
    offsetY: Float,
    palette: AtmospherePalette
) {
    val nearColor = palette.outline.copy(alpha = 0.3f)
    
    val bottomVignette = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            palette.background.copy(alpha = 0.7f)
        ),
        startY = height * 0.6f,
        endY = height
    )
    drawRect(brush = bottomVignette)
    
    val particlePositions = listOf(
        Offset(0.2f, 0.3f),
        Offset(0.5f, 0.5f),
        Offset(0.7f, 0.4f),
        Offset(0.3f, 0.6f),
        Offset(0.8f, 0.7f)
    )
    
    particlePositions.forEachIndexed { index, pos ->
        val x = width * pos.x + offsetX * 1.5f
        val y = height * pos.y + offsetY * 1.5f + sin((index + 1) * PI.toFloat()) * 20f
        val size = 2f + (index % 3) * 1.5f
        
        drawCircle(
            color = nearColor.copy(alpha = 0.2f + (index % 3) * 0.1f),
            radius = size,
            center = Offset(x, y)
        )
    }
}

@Composable
fun ForestParallaxBackground(
    parallaxOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier
) {
    ParallaxBackground(SceneAtmosphere.FOREST, parallaxOffset, modifier)
}

@Composable
fun DungeonParallaxBackground(
    parallaxOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier
) {
    ParallaxBackground(SceneAtmosphere.DUNGEON, parallaxOffset, modifier)
}

@Composable
fun CampParallaxBackground(
    parallaxOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier
) {
    ParallaxBackground(SceneAtmosphere.CAMP, parallaxOffset, modifier)
}

@Composable
fun DialogueParallaxBackground(
    parallaxOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier
) {
    ParallaxBackground(SceneAtmosphere.DIALOGUE, parallaxOffset, modifier)
}
