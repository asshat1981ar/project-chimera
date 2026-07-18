package com.chimera.core.model.sprites

/**
 * Unique identifier for every sprite asset in the game.
 * Type-safe asset referencing prevents runtime crashes from string typos.
 *
 * Example: SpriteId("npc_elara_hostile")
 */
@JvmInline
value class SpriteId(val value: String) {
    init {
        require(value.isNotBlank()) { "SpriteId cannot be blank" }
        require(value.length <= 128) { "SpriteId exceeds 128 characters: $value" }
    }

    override fun toString(): String = value
}
