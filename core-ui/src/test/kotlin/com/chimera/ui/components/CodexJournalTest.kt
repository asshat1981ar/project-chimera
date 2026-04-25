package com.chimera.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class CodexJournalTest {
    @Test
    fun `default card elevation is positive`() {
        assertTrue(CodexJournalDefaults.cardElevation > 0.dp)
    }

    @Test
    fun `default title style is non-null`() {
        assertNotNull(CodexJournalDefaults.titleStyle)
    }

    @Test
    fun `default body style is non-null`() {
        assertNotNull(CodexJournalDefaults.bodyStyle)
    }

    @Test
    fun `default category style is non-null`() {
        assertNotNull(CodexJournalDefaults.categoryStyle)
    }
}