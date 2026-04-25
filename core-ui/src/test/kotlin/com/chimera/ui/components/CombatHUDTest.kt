package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class CombatHUDTest {
    @Test
    fun `default health bar fill color is Oxblood`() {
        assertEquals(com.chimera.ui.theme.Oxblood, CombatHUDDefaults.healthBarFillColor)
    }

    @Test
    fun `default mana bar fill color is Verdigris`() {
        assertEquals(com.chimera.ui.theme.Verdigris, CombatHUDDefaults.manaBarFillColor)
    }

    @Test
    fun `default stamina bar fill color is AgedGold`() {
        assertEquals(com.chimera.ui.theme.AgedGold, CombatHUDDefaults.staminaBarFillColor)
    }

    @Test
    fun `default track color is Iron`() {
        assertEquals(com.chimera.ui.theme.Iron, CombatHUDDefaults.trackColor)
    }

    @Test
    fun `default bar height is positive`() {
        assertTrue(CombatHUDDefaults.barHeight > 0.dp)
    }
}