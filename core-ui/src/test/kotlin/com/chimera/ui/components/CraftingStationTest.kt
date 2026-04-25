package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class CraftingStationTest {
    @Test
    fun `default card elevation is positive`() {
        assertTrue(CraftingStationDefaults.cardElevation > 0.dp)
    }

    @Test
    fun `default progress bar fill color is AgedGold`() {
        assertEquals(com.chimera.ui.theme.AgedGold, CraftingStationDefaults.progressFillColor)
    }

    @Test
    fun `default recipe name style is non-null`() {
        assertNotNull(CraftingStationDefaults.recipeNameStyle)
    }

    @Test
    fun `default ingredient style is non-null`() {
        assertNotNull(CraftingStationDefaults.ingredientStyle)
    }
}