package com.chimera.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

sealed class ParticleType(
    val color: Color,
    val sizeRange: ClosedFloatingPointRange<Float>,
    val lifetime: Long
) {
    class Ember(
        val baseColor: Color = Color(0xFFFF6B35),
        size: ClosedFloatingPointRange<Float> = 2f..6f
    ) : ParticleType(baseColor, size, 3000L)
    
    class Spark(
        val baseColor: Color = Color(0xFFFFD93D),
        size: ClosedFloatingPointRange<Float> = 1f..3f
    ) : ParticleType(baseColor, size, 1500L)
    
    class Dust(
        val baseColor: Color = Color(0xFF8B7355),
        size: ClosedFloatingPointRange<Float> = 1f..4f
    ) : ParticleType(baseColor, size, 5000L)
    
    class Snow(
        val baseColor: Color = Color(0xFFE8E8E8),
        size: ClosedFloatingPointRange<Float> = 2f..5f
    ) : ParticleType(baseColor, size, 8000L)
    
    class Firefly(
        val baseColor: Color = Color(0xFF90EE90),
        size: ClosedFloatingPointRange<Float> = 2f..4f
    ) : ParticleType(baseColor, size, 6000L)
    
    class Magic(
        val baseColor: Color = Color(0xFF9B59B6),
        size: ClosedFloatingPointRange<Float> = 1f..5f
    ) : ParticleType(baseColor, size, 4000L)
}

private data class Particle(
    val id: Int,
    var x: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    val alpha: Float,
    val lifetime: Long
)

enum class EmitterPosition { Top, Bottom, FullScreen }
enum class ParticleDirection { Up, Down, Floating, Spiral }

@Composable
fun EmberOverlay(modifier: Modifier = Modifier, particleCount: Int = 50, baseColor: Color = Color(0xFFFF6B35)) {
    ParticleSystem(modifier, particleCount, ParticleType.Ember(baseColor), EmitterPosition.Bottom, ParticleDirection.Up)
}

@Composable
fun SparkOverlay(modifier: Modifier = Modifier, particleCount: Int = 30, baseColor: Color = Color(0xFFFFD93D)) {
    ParticleSystem(modifier, particleCount, ParticleType.Spark(baseColor), EmitterPosition.Bottom, ParticleDirection.Up)
}

@Composable
fun DustOverlay(modifier: Modifier = Modifier, particleCount: Int = 40, baseColor: Color = Color(0xFF8B7355)) {
    ParticleSystem(modifier, particleCount, ParticleType.Dust(baseColor), EmitterPosition.FullScreen, ParticleDirection.Floating)
}

@Composable
fun SnowOverlay(modifier: Modifier = Modifier, particleCount: Int = 60, baseColor: Color = Color(0xFFE8E8E8)) {
    ParticleSystem(modifier, particleCount, ParticleType.Snow(baseColor), EmitterPosition.Top, ParticleDirection.Down)
}

@Composable
fun FireflyOverlay(modifier: Modifier = Modifier, particleCount: Int = 25, baseColor: Color = Color(0xFF90EE90)) {
    ParticleSystem(modifier, particleCount, ParticleType.Firefly(baseColor), EmitterPosition.FullScreen, ParticleDirection.Floating)
}

@Composable
fun MagicOverlay(modifier: Modifier = Modifier, particleCount: Int = 45, baseColor: Color = Color(0xFF9B59B6)) {
    ParticleSystem(modifier, particleCount, ParticleType.Magic(baseColor), EmitterPosition.FullScreen, ParticleDirection.Spiral)
}

@Composable
private fun ParticleSystem(
    modifier: Modifier,
    particleCount: Int,
    particleType: ParticleType,
    emitterPosition: EmitterPosition,
    direction: ParticleDirection
) {
    val particles = remember { mutableStateListOf<Particle>() }
    var tick by remember { mutableIntStateOf(0) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particleTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timeProgress"
    )
    
    LaunchedEffect(particleCount) {
        while (particles.size < particleCount) {
            particles.add(createParticle(particles.size, particleType, emitterPosition))
        }
        while (true) {
            delay(100)
            tick++
            val now = System.currentTimeMillis()
            particles.removeAll { now - it.lifetime > particleType.lifetime }
            while (particles.size < particleCount) {
                particles.add(createParticle(particles.size, particleType, emitterPosition))
            }
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        particles.forEach { particle ->
            val age = System.currentTimeMillis() - particle.lifetime
            val lifeProgress = age.toFloat() / particleType.lifetime
            val alpha = (1f - lifeProgress).coerceIn(0f, 1f) * particle.alpha
            
            val baseX = particle.x / 1000f * width
            val baseY = particle.y / 1000f * height
            
            val offsetX = when (direction) {
                ParticleDirection.Up, ParticleDirection.Down -> sin(time * 10 + particle.id) * 2f
                ParticleDirection.Floating -> sin(time * 5 + particle.id) * 10f
                ParticleDirection.Spiral -> cos(time * 8 + particle.id) * 5f
            }
            
            val offsetY = when (direction) {
                ParticleDirection.Up -> -(time * 100f) * (1f - lifeProgress)
                ParticleDirection.Down -> time * 50f
                ParticleDirection.Floating -> sin(time * 3 + particle.id) * 20f
                ParticleDirection.Spiral -> sin(time * 6 + particle.id) * 15f
            }
            
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * (1f - lifeProgress * 0.5f),
                center = Offset(baseX + offsetX, baseY + offsetY)
            )
        }
    }
}

private fun createParticle(id: Int, particleType: ParticleType, emitterPosition: EmitterPosition): Particle {
    val random = Random(id)
    return when (emitterPosition) {
        EmitterPosition.Top -> Particle(
            id = id,
            x = random.nextFloat() * 1000f,
            y = -10f,
            size = random.nextFloat() * (particleType.sizeRange.endInclusive - particleType.sizeRange.start) + particleType.sizeRange.start,
            color = particleType.color,
            alpha = 0.6f + random.nextFloat() * 0.4f,
            lifetime = System.currentTimeMillis() + particleType.lifetime
        )
        EmitterPosition.Bottom -> Particle(
            id = id,
            x = random.nextFloat() * 1000f,
            y = 1000f,
            size = random.nextFloat() * (particleType.sizeRange.endInclusive - particleType.sizeRange.start) + particleType.sizeRange.start,
            color = particleType.color,
            alpha = 0.6f + random.nextFloat() * 0.4f,
            lifetime = System.currentTimeMillis() + particleType.lifetime
        )
        EmitterPosition.FullScreen -> Particle(
            id = id,
            x = random.nextFloat() * 1000f,
            y = random.nextFloat() * 1000f,
            size = random.nextFloat() * (particleType.sizeRange.endInclusive - particleType.sizeRange.start) + particleType.sizeRange.start,
            color = particleType.color,
            alpha = 0.4f + random.nextFloat() * 0.4f,
            lifetime = System.currentTimeMillis() + particleType.lifetime
        )
    }
}
