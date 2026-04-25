package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class InventoryGridTest {
    @Test
    fun `default columns is positive`() {
        assertTrue(InventoryGridDefaults.columns > 0)
    }

    @Test
    fun `default slot size is positive`() {
        assertTrue(InventoryGridDefaults.slotSize > 0.dp)
    }

    @Test
    fun `default slot border color is Oxblood`() {
        assertEquals(com.chimera.ui.theme.Oxblood, InventoryGridDefaults.slotBorderColor)
    }

    @Test
    fun `default empty slot color is Iron`() {
        assertEquals(com.chimera.ui.theme.Iron, InventoryGridDefaults.emptySlotColor)
    }
}