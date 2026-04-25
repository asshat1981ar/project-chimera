package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class HeraldicMapNodeTest {
    @Test
    fun `default discovered color is Oxblood`() {
        assertEquals(com.chimera.ui.theme.Oxblood, HeraldicMapNodeDefaults.discoveredColor)
    }

    @Test
    fun `default undiscovered color is Iron`() {
        assertEquals(com.chimera.ui.theme.Iron, HeraldicMapNodeDefaults.undiscoveredColor)
    }

    @Test
    fun `default border color is AgedGold`() {
        assertEquals(com.chimera.ui.theme.AgedGold, HeraldicMapNodeDefaults.borderColor)
    }

    @Test
    fun `default node size is positive`() {
        assertTrue(HeraldicMapNodeDefaults.nodeSize > 0.dp)
    }
}