package com.chimera.ui.components

import org.junit.Assert.*
import org.junit.Test

class GothicBottomNavTest {
    @Test
    fun `default container color is Iron`() {
        assertEquals(com.chimera.ui.theme.Iron, GothicBottomNavDefaults.containerColor)
    }

    @Test
    fun `default selected color is AgedGold`() {
        assertEquals(com.chimera.ui.theme.AgedGold, GothicBottomNavDefaults.selectedColor)
    }

    @Test
    fun `default unselected color is FadedBone`() {
        assertEquals(com.chimera.ui.theme.FadedBone, GothicBottomNavDefaults.unselectedColor)
    }

    @Test
    fun `default indicator color is Oxblood`() {
        assertEquals(com.chimera.ui.theme.Oxblood, GothicBottomNavDefaults.indicatorColor)
    }
}