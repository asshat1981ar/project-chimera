package com.chimera.core.data.sprites

import android.content.Context
import com.chimera.core.model.sprites.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JSON-backed manifest for all sprite assets.
 *
 * Loaded once at app startup from assets/sprite_manifest.json.
 * Provides O(1) lookup by SpriteId; all convenience resolution methods come
 * from the [SpriteResolver] interface (default implementations over [resolve]),
 * so UI code and tests depend on the interface, never on this class.
 *
 * Thread-safe via loading mutex. All lookup methods are non-suspending
 * and operate on an immutable map after initialization.
 *
 * v2 (2026-07-14):
 * - Implements SpriteResolver (interface extraction for testability).
 * - FIX: entry category is now taken from the parent categories[] block.
 *   v1 derived it from the ID prefix ("npc" -> no enum match -> everything
 *   silently fell back to SHARED_UI, breaking getByCategory and default sizing).
 *
 * Maps to ROADMAP Workstream F: Atmosphere and visual system.
 */
@Singleton
class SpriteManifest @Inject constructor(
    @ApplicationContext private val context: Context
) : SpriteResolver {
    companion object {
        const val MANIFEST_FILE = "sprite_manifest.json"
        private const val TAG = "SpriteManifest"
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    private val loadMutex = Mutex()
    private var _spriteMap: Map<SpriteId, SpriteRef>? = null
    private val categoryIndex = mutableMapOf<SpriteCategory, MutableList<SpriteRef>>()

    override val isLoaded: Boolean get() = _spriteMap != null

    override val totalSprites: Int get() = _spriteMap?.size ?: 0

    /**
     * Initialize the manifest. Call during app startup (e.g., in Application.onCreate).
     * Safe to call multiple times; subsequent calls are no-ops.
     */
    suspend fun initialize() {
        if (_spriteMap != null) return

        loadMutex.withLock {
            if (_spriteMap != null) return // Double-check after lock

            val map = loadManifest()
            _spriteMap = map

            // Build category index for fast category-scoped queries
            categoryIndex.clear()
            map.values.forEach { ref ->
                categoryIndex.getOrPut(ref.category) { mutableListOf() }.add(ref)
            }

            Timber.tag(TAG).i("Loaded ${map.size} sprites across ${categoryIndex.size} categories")
        }
    }

    override fun resolve(id: SpriteId): SpriteRef? = _spriteMap?.get(id)

    override fun exists(id: SpriteId): Boolean = _spriteMap?.containsKey(id) ?: false

    /**
     * Get all sprites in a category. (Manifest-specific; not part of SpriteResolver.)
     */
    fun getByCategory(category: SpriteCategory): List<SpriteRef> =
        categoryIndex[category]?.toList() ?: emptyList()

    // --- Internal ---

    private fun loadManifest(): Map<SpriteId, SpriteRef> {
        return try {
            context.assets.open(MANIFEST_FILE).use { stream ->
                val container = json.decodeFromStream<ManifestContainer>(stream)
                container.categories
                    .flatMap { categoryDto ->
                        categoryDto.entries.map { entry ->
                            entry.toSpriteRef(categoryName = categoryDto.category)
                        }
                    }
                    .associateBy { it.id }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load sprite manifest from $MANIFEST_FILE")
            emptyMap()
        }
    }

    // --- JSON DTOs ---

    @Serializable
    data class ManifestContainer(
        val version: String = "1.0.0",
        val style: String = "gothic_manuscript_inkwash",
        val total_assets: Int = 0,
        val categories: List<CategoryDto> = emptyList()
    )

    @Serializable
    data class CategoryDto(
        val category: String,
        val description: String = "",
        val entries: List<SpriteEntryDto> = emptyList()
    )

    @Serializable
    data class SpriteEntryDto(
        val id: String,
        val baseName: String,
        val transparent: Boolean = true,
        val variants: List<SpriteVariantDto> = emptyList(),
        // Optional metadata fields for debugging/tooling
        val npcId: String? = null,
        val expression: String? = null,
        val nodeType: String? = null,
        val state: String? = null,
        val stance: String? = null,
        val itemId: String? = null
    ) {
        /** [categoryName] comes from the enclosing CategoryDto block. */
        fun toSpriteRef(categoryName: String): SpriteRef = SpriteRef(
            id = SpriteId(id),
            category = SpriteCategory.fromString(categoryName)
                ?: SpriteCategory.SHARED_UI.also {
                    Timber.tag(TAG).w("Unknown sprite category '$categoryName' for $id; using SHARED_UI")
                },
            baseName = baseName,
            hasTransparency = transparent,
            variants = variants.map { it.toSpriteVariant() }
        )
    }

    @Serializable
    data class SpriteVariantDto(
        val suffix: String = "",
        val resolution: Int,
        val density: String = "XHDPI"
    ) {
        fun toSpriteVariant(): SpriteVariant = SpriteVariant(
            suffix = suffix,
            resolution = resolution,
            density = DensityQualifier.valueOf(density.uppercase())
        )
    }
}
