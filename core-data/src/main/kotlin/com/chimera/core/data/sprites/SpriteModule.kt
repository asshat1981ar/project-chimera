package com.chimera.core.data.sprites

import com.chimera.core.model.sprites.SpriteResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt bindings for the sprite system (WU-04, 2026-07-14).
 *
 * Binds the Android-backed [SpriteManifest] as the app-wide [SpriteResolver]
 * so ViewModels and composables can depend on the pure interface. Tests and
 * previews substitute MapBackedSpriteResolver/EmptySpriteResolver without Hilt.
 *
 * Destination: core-data/src/main/kotlin/com/chimera/core/data/sprites/
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SpriteModule {

    @Binds
    @Singleton
    abstract fun bindSpriteResolver(manifest: SpriteManifest): SpriteResolver
}
