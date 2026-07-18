package com.chimera.core.data.sprites

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.chimera.core.model.sprites.SpriteRef
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LRU-cached drawable loader for sprite assets.
 *
 * Bridges SpriteRef metadata to composable ImageBitmap instances.
 * Handles resource resolution, caching, and fallback for missing assets.
 *
 * v3 (2026-07-14): decodes via AppCompatResources + Drawable.toBitmap()
 * instead of BitmapFactory.decodeResource. This supports BOTH raster PNGs
 * and Android vector drawables — the art pipeline now ships map/UI sprites
 * as vectors (resolution-independent, text-diffable), while photographic
 * assets can remain PNG. Vectors rasterize at [renderSizePx] (or their
 * intrinsic size when larger) so quality holds at map zoom levels.
 *
 * Memory budget: ~50MB max cache (configurable), stores at render resolution.
 * All decoding happens on Dispatchers.IO and is cached as ImageBitmap.
 */
@Singleton
class SpriteLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SpriteLoader"
        private const val DEFAULT_CACHE_SIZE = 128 // Max entries
        private const val DEFAULT_MEMORY_MB = 50
        private const val DEFAULT_RENDER_SIZE_PX = 192 // 96dp @2x; crisp for 40-96dp draw sizes
    }

    /**
     * LRU cache with size-aware eviction.
     */
    private val cache = object : LinkedHashMap<String, ImageBitmap>(
        DEFAULT_CACHE_SIZE, 0.75f, true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
            return size > DEFAULT_CACHE_SIZE
        }
    }

    private val cacheMutex = Mutex()

    /**
     * Statistics for debugging and profiling.
     */
    data class CacheStats(
        val hits: Int,
        val misses: Int,
        val evictions: Int,
        val currentSize: Int
    )

    private var hits = 0
    private var misses = 0

    /**
     * Load a sprite as an ImageBitmap. Uses cache if available.
     * Returns null if the drawable resource is not found.
     */
    suspend fun load(spriteRef: SpriteRef, renderSizePx: Int = DEFAULT_RENDER_SIZE_PX): ImageBitmap? {
        val key = "${spriteRef.primaryDrawableName}@$renderSizePx"

        // Check cache
        cacheMutex.withLock {
            cache[key]?.let {
                hits++
                return it
            }
        }

        misses++

        // Decode on IO dispatcher
        return withContext(Dispatchers.IO) {
            loadFromResources(spriteRef.primaryDrawableName, renderSizePx)
        }?.also { bitmap ->
            cacheMutex.withLock {
                cache[key] = bitmap
            }
        }
    }

    /**
     * Load multiple sprites in parallel. Useful for screen preparation.
     */
    suspend fun preload(spriteRefs: List<SpriteRef>): Map<String, ImageBitmap?> {
        return coroutineScope {
            spriteRefs.associate { ref ->
                ref.primaryDrawableName to async { load(ref) }
            }.mapValues { (_, deferred) -> deferred.await() }
        }
    }

    /**
     * Clear the entire cache. Call on memory pressure or scene transition.
     */
    suspend fun clearCache() {
        cacheMutex.withLock {
            cache.clear()
            hits = 0
            misses = 0
        }
        Timber.tag(TAG).d("Sprite cache cleared")
    }

    /**
     * Get current cache statistics.
     */
    fun getStats(): CacheStats = CacheStats(
        hits = hits,
        misses = misses,
        evictions = 0, // LinkedHashMap handles silently
        currentSize = cache.size
    )

    // --- Internal ---

    /**
     * Resolves a drawable by name and rasterizes it.
     * Works for PNG (BitmapDrawable), vector drawables, and any other
     * Drawable type. Vectors rasterize at max(intrinsic, renderSizePx),
     * preserving aspect ratio.
     */
    private fun loadFromResources(drawableName: String, renderSizePx: Int): ImageBitmap? {
        return try {
            val resId = context.resources.getIdentifier(
                drawableName, "drawable", context.packageName
            )
            if (resId == 0) {
                Timber.tag(TAG).w("Drawable not found: $drawableName")
                return null
            }

            val drawable = AppCompatResources.getDrawable(context, resId)
            if (drawable == null) {
                Timber.tag(TAG).w("Drawable failed to inflate: $drawableName")
                return null
            }

            val intrinsicW = drawable.intrinsicWidth
            val intrinsicH = drawable.intrinsicHeight
            val (w, h) = if (intrinsicW > 0 && intrinsicH > 0) {
                if (maxOf(intrinsicW, intrinsicH) >= renderSizePx) {
                    intrinsicW to intrinsicH
                } else {
                    // Upscale render target for small vectors, keep aspect
                    val scale = renderSizePx.toFloat() / maxOf(intrinsicW, intrinsicH)
                    (intrinsicW * scale).toInt() to (intrinsicH * scale).toInt()
                }
            } else {
                renderSizePx to renderSizePx
            }

            drawable.toBitmap(width = w, height = h).asImageBitmap()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load sprite: $drawableName")
            null
        }
    }
}
