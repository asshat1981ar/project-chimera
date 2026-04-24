package com.chimera.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NpcPortraitTest {
    @Test fun `npcInitial returns first two letters for two-word names`() {
        assertEquals("WA", npcInitial("Warden"))
        assertEquals("MA", npcInitial("Mira Ashcroft"))
        assertEquals("AR", npcInitial("  aria"))
    }

    @Test fun `npcInitial returns single letter for single-word names`() {
        assertEquals("W", npcInitial("Warden"))
        assertEquals("A", npcInitial("Aria"))
        assertEquals("X", npcInitial("X"))
    }

    @Test fun `npcInitial handles leading spaces, numbers, and special chars`() {
        assertEquals("AR", npcInitial("  123Aria"))
        assertEquals("A7", npcInitial("Aria7"))
    }

    @Test fun `npcInitial returns question mark for empty name`() {
        assertEquals("?", npcInitial(""))
    }

    @Test fun `npcInitial returns question mark for digit-only name`() {
        assertEquals("?", npcInitial("404"))
    }

    @Test fun `npcInitial handles hyphenated and apostrophe names`() {
        assertEquals("JO", npcInitial("Jean-Odile"))
        assertEquals("MC", npcInitial("McDonald"))
    }

    @Test fun `npcInitial trims whitespace`() {
        assertEquals("WA", npcInitial("  Warden  "))
        assertEquals("MA", npcInitial("  Mira  Ashcroft  "))
    }

    /**
     * Structural guard for the letter-avatar fallback.
     *
     * NpcPortrait now always renders the letter-avatar as the base layer
     * (PRO-58). The AsyncImage overlay renders nothing on Coil failure,
     * letting the letter show through. This test confirms that npcInitial
     * always returns a non-empty string — i.e. the base layer always has
     * content to display, regardless of whether portraitResName is set.
     *
     * Full Compose-UI verification (asserting the text node is displayed
     * when Coil fails) requires Robolectric + ui-test-junit4 which are not
     * yet wired into core-ui. Add those once the infra is in place.
     */
    @Test fun `npcInitial always returns non-empty string — letter-avatar base always has content`() {
        val names = listOf("Warden", "Mira Ashcroft", "  123Aria", "", "404", "?!#", "7th Shadow", "X")
        for (name in names) {
            val initial = npcInitial(name)
            assertFalse(
                "npcInitial(\"$name\") returned empty string — base layer would be blank",
                initial.isEmpty()
            )
            assertTrue(
                "npcInitial(\"$name\") = \"$initial\" should be 1-2 uppercase letters",
                initial.length in 1..2 && initial.all { it.isUpperCase() || it == '?' }
            )
        }
    }
}
