package com.chimera.core.model.sprites

/**
 * Unified sprite reference with all metadata needed for loading and rendering.
 * Immutable data class; actual bitmap loading is handled by SpriteLoader.
 */
data class SpriteRef(
    val id: SpriteId,
    val category: SpriteCategory,
    val baseName: String,
    val hasTransparency: Boolean = true,
    val variants: List<SpriteVariant> = emptyList()
) {
    /**
     * Returns the primary (first) variant's resource name.
     * For simple sprites with no variants, this is the drawable name.
     */
    val primaryDrawableName: String
        get() = if (variants.isNotEmpty()) "${baseName}${variants.first().suffix}"
                else baseName

    /**
     * Checks if a variant with the given suffix exists.
     */
    fun hasVariant(suffix: String): Boolean =
        variants.any { it.suffix == suffix }

    /**
     * Gets the drawable name for a specific variant suffix.
     * Returns primary if variant not found.
     */
    fun getDrawableNameForVariant(suffix: String): String {
        val variant = variants.find { it.suffix == suffix }
        return if (variant != null) "${baseName}${variant.suffix}"
               else primaryDrawableName
    }

    init {
        require(baseName.isNotBlank()) { "baseName cannot be blank for $id" }
        require(baseName.matches(Regex("^[a-z][a-z0-9_]*$"))) {
            "baseName must be snake_case lowercase: $baseName"
        }
    }
}

data class SpriteVariant(
    val suffix: String,
    val resolution: Int,
    val density: DensityQualifier
) {
    init {
        require(resolution > 0) { "Resolution must be positive, got $resolution" }
    }
}

enum class DensityQualifier {
    MDPI,    // 1x
    HDPI,    // 1.5x
    XHDPI,   // 2x
    XXHDPI,  // 3x
    XXXHDPI; // 4x

    val scaleFactor: Float
        get() = when (this) {
            MDPI -> 1.0f
            HDPI -> 1.5f
            XHDPI -> 2.0f
            XXHDPI -> 3.0f
            XXXHDPI -> 4.0f
        }
}
