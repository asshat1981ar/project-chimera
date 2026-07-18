package com.chimera.core.model.sprites

/**
 * Categories map directly to directory structure in res/ and determine
 * loading behavior, default sizing, and caching strategy.
 */
enum class SpriteCategory(
    val directoryPrefix: String,
    val defaultSizeDp: Int,
    val supportsTinting: Boolean,
    val description: String
) {
    NPC_PORTRAIT(
        directoryPrefix = "npc/portraits",
        defaultSizeDp = 120,
        supportsTinting = true,
        description = "NPC character portraits with expression variants"
    ),
    NPC_TOKEN(
        directoryPrefix = "npc/tokens",
        defaultSizeDp = 32,
        supportsTinting = true,
        description = "Overhead map tokens for NPCs"
    ),
    COMBAT_PLAYER(
        directoryPrefix = "combat/player",
        defaultSizeDp = 180,
        supportsTinting = true,
        description = "Player combat stance sprites"
    ),
    COMBAT_OPPONENT(
        directoryPrefix = "combat/opponent",
        defaultSizeDp = 180,
        supportsTinting = true,
        description = "Opponent combat stance sprites"
    ),
    MAP_NODE(
        directoryPrefix = "map/nodes",
        defaultSizeDp = 48,
        supportsTinting = false,
        description = "Map node sprites by type and quest state"
    ),
    MAP_CONNECTION(
        directoryPrefix = "map/connections",
        defaultSizeDp = 24,
        supportsTinting = false,
        description = "Path segment sprites between map nodes"
    ),
    CAMP_ITEM(
        directoryPrefix = "camp/items",
        defaultSizeDp = 64,
        supportsTinting = false,
        description = "Inventory and crafting item sprites"
    ),
    CAMP_AMBIENT(
        directoryPrefix = "camp/ambient",
        defaultSizeDp = 96,
        supportsTinting = true,
        description = "Campfire, tent, and ambient camp elements"
    ),
    CHARACTER_CREATION(
        directoryPrefix = "character/creation",
        defaultSizeDp = 200,
        supportsTinting = false,
        description = "Character creation screen assets"
    ),
    PLAYER_CARD(
        directoryPrefix = "character/card",
        defaultSizeDp = 80,
        supportsTinting = true,
        description = "Player status card chrome"
    ),
    SHARED_UI(
        directoryPrefix = "shared/ui",
        defaultSizeDp = 32,
        supportsTinting = true,
        description = "Reusable UI chrome (seals, frames, borders)"
    ),
    SHARED_EFFECT(
        directoryPrefix = "shared/effects",
        defaultSizeDp = 16,
        supportsTinting = true,
        description = "Particle textures (embers, smoke, sparks)"
    );

    companion object {
        fun fromString(name: String): SpriteCategory? =
            entries.find { it.name.equals(name, ignoreCase = true) }
    }
}
