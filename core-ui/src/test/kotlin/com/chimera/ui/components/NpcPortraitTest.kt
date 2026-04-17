package com.chimera.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NpcPortraitTest {
    @Test fun `npcInitial returns first letter uppercase`() {
        assertEquals("W", npcInitial("Warden"))
    }
    @Test fun `npcInitial handles leading spaces and numbers`() {
        assertEquals("A", npcInitial("  123Aria"))
    }
    @Test fun `npcInitial returns question mark for empty name`() {
        assertEquals("?", npcInitial(""))
    }
    @Test fun `npcInitial returns question mark for digit-only name`() {
        assertEquals("?", npcInitial("404"))
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
        val names = listOf("Warden", "  123Aria", "", "404", "?!#", "7th Shadow")
        for (name in names) {
            val initial = npcInitial(name)
            assertFalse(
                "npcInitial(\"$name\") returned empty string — base layer would be blank",
                initial.isEmpty()
            )
        }
    }
}
