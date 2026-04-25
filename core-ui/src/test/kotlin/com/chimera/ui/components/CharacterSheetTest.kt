package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class CharacterSheetTest {
    @Test
    fun `default card elevation is positive`() {
        assertTrue(CharacterSheetDefaults.cardElevation > 0.dp)
    }

    @Test
    fun `default stat bar height is positive`() {
        assertTrue(CharacterSheetDefaults.statBarHeight > 0.dp)
    }

    @Test
    fun `default name style is non-null`() {
        assertNotNull(CharacterSheetDefaults.nameStyle)
    }

    @Test
    fun `default title style is non-null`() {
        assertNotNull(CharacterSheetDefaults.titleStyle)
    }
}