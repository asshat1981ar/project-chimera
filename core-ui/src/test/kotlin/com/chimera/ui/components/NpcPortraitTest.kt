package com.chimera.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NpcPortraitTest {
    
    @Test fun `npcInitial returns first letters from each word`() {
        assertEquals("W", npcInitial("Warden"))
        assertEquals("MA", npcInitial("Mira Ashcroft"))
        assertEquals("A", npcInitial("  aria"))
    }

    @Test fun `npcInitial returns single letter for single-word names`() {
        assertEquals("W", npcInitial("W"))
        assertEquals("A", npcInitial("Aria"))
        assertEquals("X", npcInitial("X"))
    }

    @Test fun `npcInitial handles leading spaces and numbers`() {
        assertEquals("A", npcInitial("  123Aria"))
        assertEquals("A", npcInitial("Aria7"))
    }

    @Test fun `npcInitial returns question mark for empty name`() {
        assertEquals("?", npcInitial(""))
    }

    @Test fun `npcInitial returns question mark for digit-only name`() {
        assertEquals("?", npcInitial("404"))
    }

    @Test fun `npcInitial handles multi-word names`() {
        assertEquals("J", npcInitial("Jean-Odile"))
        assertEquals("M", npcInitial("McDonald"))
    }

    @Test fun `npcInitial trims whitespace`() {
        assertEquals("W", npcInitial("  Warden  "))
        assertEquals("MA", npcInitial("  Mira  Ashcroft  "))
    }

    @Test fun `npcInitial always returns non-empty string`() {
        val names = listOf("Warden", "Mira Ashcroft", "  123Aria", "", "404", "?!#", "7th Shadow", "X")
        for (name in names) {
            val initial = npcInitial(name)
            assertFalse("npcInitial(\"$name\") returned empty", initial.isEmpty())
            assertTrue("npcInitial(\"$name\") should be 1-2 chars", initial.length in 1..2)
        }
    }
}
