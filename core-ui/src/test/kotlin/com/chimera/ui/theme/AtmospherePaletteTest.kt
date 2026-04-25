package com.chimera.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AtmospherePaletteTest {
    @Test
    fun `Forest vignette uses moss-toned color`() {
        assertEquals(Color(0xFF1A3A1A), AtmosphereTokens.Forest.overlayVignette)
    }

    @Test
    fun `Forest grain intensity is 10 percent`() {
        assertEquals(0.10f, AtmosphereTokens.Forest.grainIntensity, 0.01f)
    }

    @Test
    fun `Forest vignette alpha is 30 percent`() {
        assertEquals(0.30f, AtmosphereTokens.Forest.vignetteAlpha, 0.01f)
    }

    @Test
    fun `Cave vignette uses dark iron color`() {
        assertEquals(Color(0xFF1A1A1A), AtmosphereTokens.Cave.overlayVignette)
    }

    @Test
    fun `Cave vignette alpha is 45 percent`() {
        assertEquals(0.45f, AtmosphereTokens.Cave.vignetteAlpha, 0.01f)
    }

    @Test
    fun `Dungeon vignette uses blood-dark color`() {
        assertEquals(Color(0xFF1A0A0A), AtmosphereTokens.Dungeon.overlayVignette)
    }

    @Test
    fun `Dungeon vignette alpha is 55 percent`() {
        assertEquals(0.55f, AtmosphereTokens.Dungeon.vignetteAlpha, 0.01f)
    }

    @Test
    fun `Camp vignette uses warm amber color`() {
        assertEquals(Color(0xFF2A1A0A), AtmosphereTokens.Camp.overlayVignette)
    }

    @Test
    fun `Camp vignette alpha is 35 percent`() {
        assertEquals(0.35f, AtmosphereTokens.Camp.vignetteAlpha, 0.01f)
    }

    @Test
    fun `WorldMap vignette uses deep blue-black color`() {
        assertEquals(Color(0xFF1A1A2A), AtmosphereTokens.WorldMap.overlayVignette)
    }

    @Test
    fun `WorldMap vignette alpha is 25 percent`() {
        assertEquals(0.25f, AtmosphereTokens.WorldMap.vignetteAlpha, 0.01f)
    }

    @Test
    fun `Dialogue vignette uses warm sepia color`() {
        assertEquals(Color(0xFF2A1A0A), AtmosphereTokens.Dialogue.overlayVignette)
    }

    @Test
    fun `Dialogue vignette alpha is 30 percent`() {
        assertEquals(0.30f, AtmosphereTokens.Dialogue.vignetteAlpha, 0.01f)
    }

    @Test
    fun `each atmosphere has non-empty vignette style`() {
        SceneAtmosphere.values().forEach { atm ->
            val palette = AtmosphereTokens.paletteFor(atm)
            assertTrue(
                "${atm.name} vignetteStyle should not be empty",
                palette.vignetteStyle.isNotEmpty()
            )
        }
    }

    @Test
    fun `paletteFor returns correct palette for each atmosphere`() {
        SceneAtmosphere.values().forEach { atm ->
            assertNotNull(AtmosphereTokens.paletteFor(atm))
        }
    }
}