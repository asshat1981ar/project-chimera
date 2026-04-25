package com.chimera.ui.theme

import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class ChimeraTypographyTest {
    @Test
    fun `displayLarge uses CinzelDecorative font family`() {
        assertEquals(CinzelDecorative, ChimeraTypography.displayLarge.fontFamily)
    }

    @Test
    fun `displayLarge uses 36sp`() {
        assertEquals(36.sp, ChimeraTypography.displayLarge.fontSize)
    }

    @Test
    fun `displayMedium uses CinzelDecorative font family`() {
        assertEquals(CinzelDecorative, ChimeraTypography.displayMedium.fontFamily)
    }

    @Test
    fun `headlineLarge uses Cinzel font family`() {
        assertEquals(Cinzel, ChimeraTypography.headlineLarge.fontFamily)
    }

    @Test
    fun `headlineMedium uses Cinzel font family`() {
        assertEquals(Cinzel, ChimeraTypography.headlineMedium.fontFamily)
    }

    @Test
    fun `bodyLarge uses Cinzel font family for serif body`() {
        assertEquals(Cinzel, ChimeraTypography.bodyLarge.fontFamily)
    }

    @Test
    fun `bodyMedium uses Cinzel font family for serif body`() {
        assertEquals(Cinzel, ChimeraTypography.bodyMedium.fontFamily)
    }

    @Test
    fun `labelLarge uses sans-serif family`() {
        assertEquals(androidx.compose.ui.text.font.FontFamily.SansSerif, ChimeraTypography.labelLarge.fontFamily)
    }
}