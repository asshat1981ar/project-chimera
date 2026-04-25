package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class ManuscriptCardTest {
    @Test
    fun `default border width is positive`() {
        assertTrue(ManuscriptCardDefaults.borderWidth > 0.dp)
    }

    @Test
    fun `default corner radius is positive`() {
        assertTrue(ManuscriptCardDefaults.cornerRadius > 0.dp)
    }

    @Test
    fun `default elevation is positive`() {
        assertTrue(ManuscriptCardDefaults.elevation > 0.dp)
    }

    @Test
    fun `default content padding is positive`() {
        assertTrue(ManuscriptCardDefaults.contentPadding > 0.dp)
    }
}