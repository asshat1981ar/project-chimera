package com.chimera.ui.assets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.chimera.ui.components.DustOverlay
import com.chimera.ui.components.EmberOverlay
import com.chimera.ui.components.FireflyOverlay
import com.chimera.ui.components.MagicOverlay
import com.chimera.ui.components.SnowOverlay
import com.chimera.ui.components.SparkOverlay
import com.chimera.ui.theme.SceneAtmosphere
import com.chimera.ui.theme.AtmosphereTokens

/**
 * Scene visual configuration mapping scenes to their visual properties.
 */
data class SceneVisualConfig(
    val atmosphere: SceneAtmosphere,
    val particleType: ParticleType,
    val hasParticles: Boolean,
    val hasVignette: Boolean,
    val hasGrain: Boolean,
    val vignetteIntensity: Float,
    val grainIntensity: Float
)

/**
 * Particle types for scene visuals.
 */
enum class ParticleType {
    NONE,
    EMBERS,
    SPARKS,
    DUST,
    SNOW,
    FIREFLIES,
    MAGIC
}

/**
 * Composition local for scene visual configuration.
 */
val LocalSceneVisuals = staticCompositionLocalOf { 
    SceneVisualConfig(
        atmosphere = SceneAtmosphere.FOREST,
        particleType = ParticleType.NONE,
        hasParticles = false,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.4f,
        grainIntensity = 0.1f
    )
}

/**
 * Scene visuals mapping for all game scenes.
 * Maps scene IDs to their visual configuration.
 */
object SceneVisuals {
    
    /**
     * Get visual config for a scene by its ID.
     */
    fun forScene(sceneId: String): SceneVisualConfig {
        return when {
            // Act 1 Forest scenes
            sceneId.startsWith("forest_") -> ForestVisual
            sceneId.startsWith("cave_") -> CaveVisual
            sceneId.startsWith("dungeon_") || sceneId.startsWith("hollow_") -> DungeonVisual
            sceneId.startsWith("camp_") || sceneId.startsWith("rest_") -> CampVisual
            sceneId.startsWith("map_") || sceneId.startsWith("world_") -> WorldMapVisual
            sceneId.startsWith("dialogue_") || sceneId.startsWith("prologue") -> DialogueVisual
            
            // Act transitions
            sceneId.contains("act_transition") -> ActTransitionVisual
            sceneId.contains("opening") -> CinematicVisual
            sceneId.contains("finale") -> FinaleVisual
            
            // Combat scenes
            sceneId.contains("duel") -> CombatVisual
            
            // Default fallback
            else -> DefaultVisual
        }
    }
    
    val DefaultVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.FOREST,
        particleType = ParticleType.NONE,
        hasParticles = false,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.4f,
        grainIntensity = 0.1f
    )
    
    val ForestVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.FOREST,
        particleType = ParticleType.FIREFLIES,
        hasParticles = true,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.35f,
        grainIntensity = 0.08f
    )
    
    val CaveVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.CAVE,
        particleType = ParticleType.DUST,
        hasParticles = true,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.55f,
        grainIntensity = 0.15f
    )
    
    val DungeonVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.DUNGEON,
        particleType = ParticleType.NONE,
        hasParticles = false,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.7f,
        grainIntensity = 0.2f
    )
    
    val CampVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.CAMP,
        particleType = ParticleType.EMBERS,
        hasParticles = true,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.4f,
        grainIntensity = 0.12f
    )
    
    val WorldMapVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.WORLD_MAP,
        particleType = ParticleType.NONE,
        hasParticles = false,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.25f,
        grainIntensity = 0.05f
    )
    
    val DialogueVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.DIALOGUE,
        particleType = ParticleType.NONE,
        hasParticles = false,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.3f,
        grainIntensity = 0.08f
    )
    
    val ActTransitionVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.DIALOGUE,
        particleType = ParticleType.MAGIC,
        hasParticles = true,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.5f,
        grainIntensity = 0.15f
    )
    
    val CinematicVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.FOREST,
        particleType = ParticleType.NONE,
        hasParticles = false,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.2f,
        grainIntensity = 0.05f
    )
    
    val FinaleVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.DUNGEON,
        particleType = ParticleType.MAGIC,
        hasParticles = true,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.6f,
        grainIntensity = 0.18f
    )
    
    val CombatVisual = SceneVisualConfig(
        atmosphere = SceneAtmosphere.DUNGEON,
        particleType = ParticleType.SPARKS,
        hasParticles = true,
        hasVignette = true,
        hasGrain = true,
        vignetteIntensity = 0.65f,
        grainIntensity = 0.12f
    )
}

/**
 * Composable that provides scene visuals context.
 */
@Composable
fun SceneVisualsProvider(
    sceneId: String,
    content: @Composable () -> Unit
) {
    val config = SceneVisuals.forScene(sceneId)
    
    CompositionLocalProvider(LocalSceneVisuals provides config) {
        content()
    }
}

/**
 * Composable that renders particles based on scene config.
 */
@Composable
fun SceneParticles(
    sceneId: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val config = SceneVisuals.forScene(sceneId)
    
    if (!config.hasParticles) return
    
    when (config.particleType) {
        ParticleType.NONE -> { /* No particles */ }
        ParticleType.EMBERS -> {
            EmberOverlay(modifier = modifier)
        }
        ParticleType.SPARKS -> {
            SparkOverlay(modifier = modifier)
        }
        ParticleType.DUST -> {
            DustOverlay(modifier = modifier)
        }
        ParticleType.SNOW -> {
            SnowOverlay(modifier = modifier)
        }
        ParticleType.FIREFLIES -> {
            FireflyOverlay(modifier = modifier)
        }
        ParticleType.MAGIC -> {
            MagicOverlay(modifier = modifier)
        }
    }
}
