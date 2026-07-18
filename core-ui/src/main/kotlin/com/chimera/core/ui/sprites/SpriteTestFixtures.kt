package com.chimera.core.ui.sprites

import com.chimera.core.model.sprites.*

/**
 * Test fixtures for Compose and unit tests.
 * Provides pre-built SpriteRef instances plus ready-made resolvers.
 *
 * v2 (2026-07-14): no longer subclasses SpriteManifest (which is a final
 * @Singleton with an Android Context). Fixtures now implement/compose the
 * pure SpriteResolver interface, so they run on plain JVM.
 */
object SpriteTestFixtures {

    val elaraNeutralPortrait = SpriteRef(
        id = SpriteIds.npcPortrait("elara", PortraitExpression.NEUTRAL),
        category = SpriteCategory.NPC_PORTRAIT,
        baseName = "npc_elara_neutral",
        hasTransparency = true,
        variants = listOf(SpriteVariant("", 128, DensityQualifier.XHDPI))
    )

    val elaraHostilePortrait = SpriteRef(
        id = SpriteIds.npcPortrait("elara", PortraitExpression.HOSTILE),
        category = SpriteCategory.NPC_PORTRAIT,
        baseName = "npc_elara_hostile",
        hasTransparency = true,
        variants = listOf(SpriteVariant("", 128, DensityQualifier.XHDPI))
    )

    val ruinsActiveMapNode = SpriteRef(
        id = SpriteIds.mapNode("ruins", MapNodeState.ACTIVE),
        category = SpriteCategory.MAP_NODE,
        baseName = "map_ruins_active",
        hasTransparency = true,
        variants = listOf(SpriteVariant("", 96, DensityQualifier.XHDPI))
    )

    val playerStrikeStance = SpriteRef(
        id = SpriteIds.combatStance(isPlayer = true, stanceName = "strike", wounded = false),
        category = SpriteCategory.COMBAT_PLAYER,
        baseName = "combat_player_strike",
        hasTransparency = true,
        variants = listOf(
            SpriteVariant("", 180, DensityQualifier.XHDPI),
            SpriteVariant("_wounded", 180, DensityQualifier.XHDPI)
        )
    )

    val herbBundleItem = SpriteRef(
        id = SpriteIds.campItem("herb_bundle"),
        category = SpriteCategory.CAMP_ITEM,
        baseName = "camp_item_herb_bundle",
        hasTransparency = true,
        variants = listOf(SpriteVariant("", 128, DensityQualifier.XHDPI))
    )

    val goldFrame = SpriteRef(
        id = SpriteIds.uiElement("frame_gold"),
        category = SpriteCategory.SHARED_UI,
        baseName = "ui_frame_gold",
        hasTransparency = true,
        variants = listOf(SpriteVariant("", 512, DensityQualifier.XHDPI))
    )

    /** All fixtures, for bulk registration. */
    val all: List<SpriteRef> = listOf(
        elaraNeutralPortrait,
        elaraHostilePortrait,
        ruinsActiveMapNode,
        playerStrikeStance,
        herbBundleItem,
        goldFrame
    )

    /** Resolver that knows no sprites — exercises every fallback path. */
    fun emptyResolver(): SpriteResolver = EmptySpriteResolver

    /** Resolver pre-loaded with the fixtures above. */
    fun testResolver(): SpriteResolver = MapBackedSpriteResolver(all.associateBy { it.id })
}
