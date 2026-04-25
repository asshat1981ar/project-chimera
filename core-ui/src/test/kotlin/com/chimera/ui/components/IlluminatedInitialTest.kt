package com.chimera.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class IlluminatedInitialTest {
    @Test
    fun `extractInitial returns first uppercase letter of text`() {
        assertEquals("W", extractInitial("Welcome, Wanderer"))
    }

    @Test
    fun `extractInitial returns question mark for empty text`() {
        assertEquals("?", extractInitial(""))
    }

    @Test
    fun `extractInitial returns question mark for blank text`() {
        assertEquals("?", extractInitial("   "))
    }

    @Test
    fun `extractInitial returns first letter ignoring leading spaces`() {
        assertEquals("C", extractInitial("  Chapter One"))
    }

    @Test
    fun `extractInitial returns question mark for digits-only text`() {
        assertEquals("?", extractInitial("12345"))
    }
}