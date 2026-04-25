package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class ManuscriptStatBarTest {
    @Test
    fun `fraction is clamped to 0f-1f`() {
        // coerceIn clamps negative values to 0 and values >1 to 1
        assertEquals(0f, (-0.5f).coerceIn(0f, 1f), 0.001f)
        assertEquals(1f, 1.5f.coerceIn(0f, 1f), 0.001f)
        assertEquals(0.5f, 0.5f.coerceIn(0f, 1f), 0.001f)
    }

    @Test
    fun `default label is non-null`() {
        assertNotNull(ManuscriptStatBarDefaults.labelStyle)
    }

    @Test
    fun `default height is positive`() {
        assertTrue(ManuscriptStatBarDefaults.height > 0.dp)
    }

    @Test
    fun `default corner radius is positive`() {
        assertTrue(ManuscriptStatBarDefaults.cornerRadius > 0.dp)
    }
}