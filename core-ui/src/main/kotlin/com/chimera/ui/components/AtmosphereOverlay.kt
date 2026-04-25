package com.chimera.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.draw.drawBehind
import com.chimera.ui.theme.AtmospherePalette
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.ParchmentDark
import kotlin.math.sin
import kotlin.random.Random

/**
 * Atmosphere overlay providing vignette and grain effects for scene immersion.
 * 
 * @param vignetteIntensity 0.0 (no vignette) to 1.0 (strong darkening at edges)
 * @param grainIntensity 0.0 (no grain) to 0.3 (heavy film grain)
 * @param vignetteColor The color of the vignette edge darkening
 */
@Composable
fun AtmosphereOverlay(
    vignetteIntensity: Float = 0.4f,
    grainIntensity: Float = 0.1f,
    vignetteColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    val clampedVignette = vignetteIntensity.coerceIn(0f, 1f)
    val clampedGrain = grainIntensity.coerceIn(0f, 0.3f)
    
    // Animated grain seed for subtle flicker
    val infiniteTransition = rememberInfiniteTransition(label = "grain")
    val grainSeed by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grainSeed"
    )
    
    // Cache grain pattern for performance
    val grainSeeds = remember(grainSeed.toInt()) {
        List(200) { Random((grainSeed + it).toInt()).nextFloat() }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw vignette
        if (clampedVignette > 0f) {
            drawVignette(
                intensity = clampedVignette,
                color = vignetteColor
            )
        }
        
        // Draw grain overlay
        if (clampedGrain > 0f) {
            drawGrain(
                intensity = clampedGrain,
                seeds = grainSeeds
            )
        }
    }
}

/**
 * Draws radial vignette effect darkening toward edges.
 */
private fun DrawScope.drawVignette(
    intensity: Float,
    color: Color
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = kotlin.math.sqrt(centerX * centerX + centerY * centerY)
    
    val vignetteBrush = Brush.radialGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            color.copy(alpha = intensity * 0.3f),
            color.copy(alpha = intensity * 0.7f),
            color.copy(alpha = intensity)
        ),
        center = Offset(centerX, centerY),
        radius = maxRadius
    )
    
    drawRect(brush = vignetteBrush)
}

/**
 * Draws deterministic film grain pattern.
 */
private fun DrawScope.drawGrain(
    intensity: Float,
    seeds: List<Float>
) {
    val grainColor = Color.White.copy(alpha = intensity)
    
    seeds.forEach { seed ->
        val x = seed * size.width
        val y = (seed * 7919) % 1f * size.height // Pseudo-random y based on seed
        val alpha = ((seed * 3571) % 1f) * intensity
        
        drawCircle(
            color = grainColor.copy(alpha = alpha),
            radius = 1f,
            center = Offset(x, y)
        )
    }
}

/**
 * Dark vignette for combat/tense scenes.
 */
@Composable
fun CombatVignette(modifier: Modifier = Modifier) {
    AtmosphereOverlay(
        vignetteIntensity = 0.6f,
        grainIntensity = 0.08f,
        vignetteColor = Color(0xFF0A0000), // Deep red-black
        modifier = modifier
    )
}

/**
 * Subtle vignette for dialogue scenes.
 */
@Composable
fun DialogueVignette(modifier: Modifier = Modifier) {
    AtmosphereOverlay(
        vignetteIntensity = 0.3f,
        grainIntensity = 0.05f,
        vignetteColor = Color.Black,
        modifier = modifier
    )
}

/**
 * Heavy vignette for dungeon/cave atmosphere.
 */
@Composable
fun DungeonVignette(modifier: Modifier = Modifier) {
    AtmosphereOverlay(
        vignetteIntensity = 0.7f,
        grainIntensity = 0.15f,
        vignetteColor = Color(0xFF050202),
        modifier = modifier
    )
}

/**
 * Warm vignette for camp/fire scenes.
 */
@Composable
fun CampVignette(modifier: Modifier = Modifier) {
    AtmosphereOverlay(
        vignetteIntensity = 0.4f,
        grainIntensity = 0.06f,
        vignetteColor = Color(0xFF1A0A00), // Warm orange-black
        modifier = modifier
    )
}

/**
 * Ambient corner shadows for map/overview screens.
 */
@Composable
fun AmbientShadows(
    modifier: Modifier = Modifier,
    topLeft: Boolean = true,
    topRight: Boolean = true,
    bottomLeft: Boolean = true,
    bottomRight: Boolean = true
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val shadowSize = minOf(size.width, size.height) * 0.3f
        
        if (topLeft) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset.Zero,
                    radius = shadowSize
                ),
                topLeft = Offset.Zero
            )
        }
        
        if (topRight) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = shadowSize
                ),
                topLeft = Offset(size.width - shadowSize, 0f)
            )
        }
        
        if (bottomLeft) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(0f, size.height),
                    radius = shadowSize
                ),
                topLeft = Offset(0f, size.height - shadowSize)
            )
        }
        
        if (bottomRight) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(size.width, size.height),
                    radius = shadowSize
                ),
                topLeft = Offset(size.width - shadowSize, size.height - shadowSize)
            )
        }
    }
}

/**
 * Atmosphere-themed overlay that applies vignette from the current atmosphere palette.
 */
@Composable
fun AtmosphereThemedOverlay(
    palette: AtmospherePalette,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                palette.overlayVignette.copy(alpha = palette.vignetteAlpha)
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.maxDimension
                        )
                    )
                }
        )
    }
}

@Composable
fun ParchmentTexture(
    modifier: Modifier = Modifier,
    grainAlpha: Float = ParchmentTextureDefaults.grainAlpha,
    stainAlpha: Float = ParchmentTextureDefaults.stainAlpha,
    seed: Long = ParchmentTextureDefaults.seed
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val rng = Random(seed)
                val grainColor = Iron.copy(alpha = grainAlpha)
                val dotCount = (size.width * size.height / 800).toInt()
                repeat(dotCount) {
                    val x = rng.nextFloat() * size.width
                    val y = rng.nextFloat() * size.height
                    drawCircle(
                        color = grainColor,
                        radius = 0.5f,
                        center = Offset(x, y)
                    )
                }
                val stainColor = ParchmentDark.copy(alpha = stainAlpha)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(stainColor, Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.85f),
                        radius = size.maxDimension * 0.4f
                    )
                )
            }
    )
}

object ParchmentTextureDefaults {
    const val grainAlpha = 0.08f
    const val stainAlpha = 0.06f
    const val seed = 42L
}
