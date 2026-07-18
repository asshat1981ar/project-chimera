package com.chimera.core.model.sprites

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for the sprite model layer (no Android deps).
 * Destination: core-model/src/test/kotlin/com/chimera/core/model/sprites/
 * Run: ./gradlew :core-model:test
 */
class SpriteModelTest {

    // --- SpriteIds naming conventions ---

    @Test
    fun `npcPortrait id follows convention`() {
        assertEquals(
            "npc_elara_hostile",
            SpriteIds.npcPortrait("elara", PortraitExpression.HOSTILE).value
        )
    }

    @Test
    fun `combatStance id includes wounded suffix only when wounded`() {
        assertEquals(
            "combat_player_strike",
            SpriteIds.combatStance(isPlayer = true, stanceName = "STRIKE", wounded = false).value
        )
        assertEquals(
            "combat_opponent_ward_wounded",
            SpriteIds.combatStance(isPlayer = false, stanceName = "Ward", wounded = true).value
        )
    }

    @Test
    fun `sanitize normalizes spaces hyphens case and strips illegal chars`() {
        assertEquals("herb_bundle", SpriteIds.sanitize("Herb Bundle"))
        assertEquals("omen_stone", SpriteIds.sanitize("omen-stone"))
        assertEquals("voids_ash", SpriteIds.sanitize("Void's Ash"))
    }

    @Test
    fun `mapNode id uses stateName`() {
        assertEquals(
            "map_ruins_active",
            SpriteIds.mapNode("ruins", MapNodeState.ACTIVE).value
        )
    }

    // --- SpriteId validation ---

    @Test(expected = IllegalArgumentException::class)
    fun `blank SpriteId rejected`() {
        SpriteId("  ")
    }

    // --- SpriteRef ---

    @Test
    fun `primaryDrawableName uses first variant suffix`() {
        val ref = SpriteRef(
            id = SpriteId("combat_player_strike"),
            category = SpriteCategory.COMBAT_PLAYER,
            baseName = "combat_player_strike",
            variants = listOf(
                SpriteVariant("", 180, DensityQualifier.XHDPI),
                SpriteVariant("_wounded", 180, DensityQualifier.XHDPI)
            )
        )
        assertEquals("combat_player_strike", ref.primaryDrawableName)
        assertTrue(ref.hasVariant("_wounded"))
        assertEquals("combat_player_strike_wounded", ref.getDrawableNameForVariant("_wounded"))
        assertEquals("combat_player_strike", ref.getDrawableNameForVariant("_missing"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `SpriteRef rejects non snake_case baseName`() {
        SpriteRef(
            id = SpriteId("bad"),
            category = SpriteCategory.SHARED_UI,
            baseName = "BadName"
        )
    }

    // --- PortraitExpression ---

    @Test
    fun `fromDisposition maps thresholds per plan section 5_1`() {
        assertEquals(PortraitExpression.OATHBOUND, PortraitExpression.fromDisposition(0.8f))
        assertEquals(PortraitExpression.GRATEFUL, PortraitExpression.fromDisposition(0.5f))
        assertEquals(PortraitExpression.NEUTRAL, PortraitExpression.fromDisposition(0.0f))
        assertEquals(PortraitExpression.TENSE, PortraitExpression.fromDisposition(-0.4f))
        assertEquals(PortraitExpression.WOUNDED, PortraitExpression.fromDisposition(-0.7f))
        assertEquals(PortraitExpression.HOSTILE, PortraitExpression.fromDisposition(-0.9f))
    }

    @Test
    fun `only wounded and hostile exceed ink wash threshold`() {
        val heavy = PortraitExpression.entries.filter { it.inkWashIntensity > 0.3f }
        assertEquals(
            setOf(PortraitExpression.WOUNDED, PortraitExpression.HOSTILE),
            heavy.toSet()
        )
    }

    // --- MapNodeState ---

    @Test
    fun `only ACTIVE pulses and opacities are sane`() {
        assertEquals(listOf(MapNodeState.ACTIVE), MapNodeState.entries.filter { it.pulses })
        MapNodeState.entries.forEach { state ->
            assertTrue("${state.name} opacity in (0,1]", state.defaultOpacity > 0f && state.defaultOpacity <= 1f)
        }
        assertEquals(0.3f, MapNodeState.HIDDEN.defaultOpacity)
        assertEquals(0.5f, MapNodeState.BLOCKED.defaultOpacity)
    }

    @Test
    fun `fromName round trips`() {
        MapNodeState.entries.forEach { state ->
            assertEquals(state, MapNodeState.fromName(state.stateName))
        }
        assertNull(MapNodeState.fromName("nonsense"))
    }

    // --- Resolver defaults over MapBackedSpriteResolver ---

    @Test
    fun `resolver convenience methods use canonical ids`() {
        val portrait = SpriteRef(
            id = SpriteIds.npcPortrait("elara", PortraitExpression.NEUTRAL),
            category = SpriteCategory.NPC_PORTRAIT,
            baseName = "npc_elara_neutral"
        )
        val resolver: SpriteResolver = MapBackedSpriteResolver(portrait)

        assertNotNull(resolver.resolveNpcPortrait("Elara", PortraitExpression.NEUTRAL))
        assertNull(resolver.resolveNpcPortrait("elara", PortraitExpression.HOSTILE))
        assertNull(resolver.resolveNpcToken("elara"))
        assertEquals(1, resolver.totalSprites)
        assertEquals(
            mapOf(PortraitExpression.NEUTRAL to portrait),
            resolver.resolveAllNpcPortraits("elara")
        )
    }

    @Test
    fun `empty resolver resolves nothing`() {
        assertNull(EmptySpriteResolver.resolve(SpriteId("anything")))
        assertEquals(0, EmptySpriteResolver.totalSprites)
        assertTrue(EmptySpriteResolver.isLoaded)
    }
}
