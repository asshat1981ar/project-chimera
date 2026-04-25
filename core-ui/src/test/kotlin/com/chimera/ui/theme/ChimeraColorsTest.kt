package com.chimera.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ChimeraColorsTest {

    @Test
    fun `Oxblood primary is refined dark crimson`() {
        assertEquals(Color(0xFF5C1A1A), Oxblood)
    }

    @Test
    fun `AgedGold secondary is refined warm gold`() {
        assertEquals(Color(0xFFC89B3C), AgedGold)
    }

    @Test
    fun `Vellum text is warm cream`() {
        assertEquals(Color(0xFFF5ECD7), Vellum)
    }

    @Test
    fun `Iron surface is elevated dark gray`() {
        assertEquals(Color(0xFF2A2A2E), Iron)
    }

    @Test
    fun `Verdigris tertiary is muted sage`() {
        assertEquals(Color(0xFF4A7C59), Verdigris)
    }

    @Test
    fun `backward compat aliases exist`() {
        assertEquals(Oxblood, HollowCrimson)
        assertEquals(AgedGold, EmberGold)
        assertEquals(Vellum, ParchmentWhite)
        assertEquals(Iron, CharcoalSurface)
        assertEquals(Verdigris, VoidGreen)
    }

    @Test
    fun `gold accent colors exist`() {
        assertEquals(Color(0xFFD4A84E), AgedGoldBright)
        assertEquals(Color(0xFF9E7A1A), AgedGoldMuted)
    }

    @Test
    fun `parchment texture colors exist`() {
        assertEquals(Color(0xFFF5ECD7), ParchmentLight)
        assertEquals(Color(0xFFE8DCC8), ParchmentDark)
    }
}