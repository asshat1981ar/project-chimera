package com.chimera.ui.components

import org.junit.Assert.*
import org.junit.Test

class ParchmentTextureTest {
    @Test
    fun `default alpha values are within valid range`() {
        assertTrue(ParchmentTextureDefaults.grainAlpha in 0f..1f)
        assertTrue(ParchmentTextureDefaults.stainAlpha in 0f..1f)
    }

    @Test
    fun `default grain color is not transparent`() {
        assertTrue(ParchmentTextureDefaults.grainAlpha > 0f)
    }
}

class AtmosphereThemedOverlayTest {
    @Test
    fun `vignette alpha values are within range for all atmospheres`() {
        val palettes = listOf(
            com.chimera.ui.theme.AtmosphereTokens.Forest,
            com.chimera.ui.theme.AtmosphereTokens.Cave,
            com.chimera.ui.theme.AtmosphereTokens.Dungeon,
            com.chimera.ui.theme.AtmosphereTokens.Camp,
            com.chimera.ui.theme.AtmosphereTokens.WorldMap,
            com.chimera.ui.theme.AtmosphereTokens.Dialogue
        )
        for (palette in palettes) {
            assertTrue(
                "vignetteAlpha for ${palette.vignetteStyle} should be 0-1, was ${palette.vignetteAlpha}",
                palette.vignetteAlpha in 0f..1f
            )
        }
    }
}