package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class IlluminatedDialogueBubbleTest {
    @Test
    fun `default corner radius is positive`() {
        assertTrue(IlluminatedDialogueBubbleDefaults.cornerRadius > 0.dp)
    }

    @Test
    fun `default border width is positive`() {
        assertTrue(IlluminatedDialogueBubbleDefaults.borderWidth > 0.dp)
    }

    @Test
    fun `default padding is positive`() {
        assertTrue(IlluminatedDialogueBubbleDefaults.contentPadding > 0.dp)
    }

    @Test
    fun `default speaker name style is non-null`() {
        assertNotNull(IlluminatedDialogueBubbleDefaults.speakerStyle)
    }
}