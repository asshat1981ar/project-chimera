package com.chimera.ui.theme

import org.junit.Assert.assertNotNull
import org.junit.Test

class GothicFontsTest {
    @Test
    fun `CinzelDecorative font family is defined`() {
        assertNotNull(CinzelDecorative)
    }

    @Test
    fun `Cinzel font family is defined`() {
        assertNotNull(Cinzel)
    }

    @Test
    fun `GothicDisplayFallback font family is defined`() {
        assertNotNull(GothicDisplayFallback)
    }
}