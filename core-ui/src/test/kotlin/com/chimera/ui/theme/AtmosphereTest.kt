package com.chimera.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AtmosphereTest {
    @Test
    fun palettesAreDistinctForEachAtmosphere() {
        val palettes = SceneAtmosphere.entries.map { AtmosphereTokens.paletteFor(it) }

        assertEquals(SceneAtmosphere.entries.size, palettes.distinct().size)
        assertNotEquals(
            AtmosphereTokens.paletteFor(SceneAtmosphere.FOREST).background,
            AtmosphereTokens.paletteFor(SceneAtmosphere.CAVE).background
        )
        assertNotEquals(
            AtmosphereTokens.paletteFor(SceneAtmosphere.CAMP).accent,
            AtmosphereTokens.paletteFor(SceneAtmosphere.WORLD_MAP).accent
        )
    }

    @Test
    fun fromRouteMapsCanonicalRoutes() {
        assertEquals(SceneAtmosphere.WORLD_MAP, SceneAtmosphere.fromRoute("map"))
        assertEquals(SceneAtmosphere.CAMP, SceneAtmosphere.fromRoute("camp"))
        assertEquals(SceneAtmosphere.CAMP, SceneAtmosphere.fromRoute("inventory"))
        assertEquals(SceneAtmosphere.CAMP, SceneAtmosphere.fromRoute("crafting"))
        assertEquals(SceneAtmosphere.DIALOGUE, SceneAtmosphere.fromRoute("dialogue/npc-1"))
        assertEquals(SceneAtmosphere.DUNGEON, SceneAtmosphere.fromRoute("duel/arena"))
        assertEquals(SceneAtmosphere.FOREST, SceneAtmosphere.fromRoute("home"))
        assertEquals(SceneAtmosphere.DIALOGUE, SceneAtmosphere.fromRoute("journal"))
        assertEquals(SceneAtmosphere.DIALOGUE, SceneAtmosphere.fromRoute("party"))
        assertEquals(SceneAtmosphere.DIALOGUE, SceneAtmosphere.fromRoute("settings"))
        assertEquals(SceneAtmosphere.FOREST, SceneAtmosphere.fromRoute("unknown"))
        assertEquals(SceneAtmosphere.FOREST, SceneAtmosphere.fromRoute(null))
    }
}
