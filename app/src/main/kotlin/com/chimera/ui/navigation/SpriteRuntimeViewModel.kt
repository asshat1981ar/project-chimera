package com.chimera.ui.navigation

import androidx.lifecycle.ViewModel
import com.chimera.core.data.sprites.SpriteLoader
import com.chimera.core.data.sprites.SpriteManifest
import com.chimera.core.model.sprites.SpriteResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Thin Hilt bridge that surfaces the sprite runtime (resolver + loader)
 * to the navigation layer without touching MainActivity (WU-04, 2026-07-14).
 *
 * ChimeraNavHost obtains this via hiltViewModel(), provides [loader] through
 * LocalSpriteLoader (shared LRU cache for every ChimeraSprite), and passes
 * [resolver] to the sprite-wired screens (MapScreen, DialogueSceneScreen).
 *
 * Manifest initialization happens in ChimeraApplication.onCreate; by the time
 * any screen resolves a sprite the map is loaded or resolution returns null
 * and screens fall back to legacy rendering — never a crash.
 *
 * Destination: app/src/main/kotlin/com/chimera/ui/navigation/
 */
@HiltViewModel
class SpriteRuntimeViewModel @Inject constructor(
    manifest: SpriteManifest,
    val loader: SpriteLoader
) : ViewModel() {
    val resolver: SpriteResolver = manifest
}
