package com.chimera.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortraitGenerationServiceTest {

    @Test fun `portrait prompt uses gothic ink-wash style language`() {
        val prompt = buildPortraitPrompt("Elara", "COMPANION", null)
        assertTrue(prompt.contains("gothic"))
        assertTrue(prompt.contains("sumi-e ink wash"))
        assertTrue(prompt.contains("Elara"))
        assertTrue(prompt.contains("trusted companion, loyal ally"))
    }

    @Test fun `portrait prompt includes title when present`() {
        val prompt = buildPortraitPrompt("Thorne", "GUARDIAN", "Warden of the Hollow")
        assertTrue(prompt.contains("Warden of the Hollow,"))
    }

    @Test fun `portrait prompt omits title when null or blank`() {
        val withNull = buildPortraitPrompt("Thorne", "GUARDIAN", null)
        val withBlank = buildPortraitPrompt("Thorne", "GUARDIAN", "  ")
        assertTrue(!withNull.contains("null"))
        assertTrue(!withBlank.contains("null"))
    }

    @Test fun `token prompt uses overhead framing distinct from portrait`() {
        val tokenPrompt = buildTokenPrompt("Elara", "COMPANION", null)
        val portraitPrompt = buildPortraitPrompt("Elara", "COMPANION", null)
        assertTrue(tokenPrompt.contains("overhead map token"))
        assertTrue(tokenPrompt.contains("birds-eye view"))
        assertTrue(tokenPrompt.contains("top-down perspective"))
        assertTrue(tokenPrompt != portraitPrompt)
    }

    @Test fun `roleHint covers every known role and falls back for unknown roles`() {
        assertTrue(roleHint("COMPANION").contains("companion"))
        assertTrue(roleHint("ANTAGONIST").contains("villain"))
        assertTrue(roleHint("MENTOR").contains("elder"))
        assertTrue(roleHint("MERCHANT").contains("merchant"))
        assertTrue(roleHint("GUARDIAN").contains("guardian"))
        assertTrue(roleHint("PROTAGONIST").contains("protagonist"))
        assertTrue(roleHint("SOMETHING_UNKNOWN").contains("fantasy NPC"))
    }

    @Test fun `roleHint is case-insensitive`() {
        assertTrue(roleHint("companion") == roleHint("COMPANION"))
    }

    @Test fun `portrait prompt includes equipment descriptor when present`() {
        val prompt = buildPortraitPrompt("Elara", "COMPANION", null, "a weathered leather cloak")
        assertTrue(prompt.contains("wearing a weathered leather cloak,"))
    }

    @Test fun `portrait prompt omits equipment phrasing when descriptor is null or blank`() {
        val withNull = buildPortraitPrompt("Elara", "COMPANION", null, null)
        val withBlank = buildPortraitPrompt("Elara", "COMPANION", null, "  ")
        assertTrue(!withNull.contains("wearing"))
        assertTrue(!withBlank.contains("wearing"))
    }

    @Test fun `token prompt includes equipment descriptor when present`() {
        val prompt = buildTokenPrompt("Elara", "COMPANION", null, "a weathered leather cloak")
        assertTrue(prompt.contains("wearing a weathered leather cloak,"))
    }

    @Test fun `loadoutHash is order-independent`() {
        assertEquals(loadoutHash(listOf("cloak", "boots")), loadoutHash(listOf("boots", "cloak")))
    }

    @Test fun `loadoutHash differs for different loadouts`() {
        assertNotEquals(loadoutHash(listOf("cloak")), loadoutHash(listOf("boots")))
    }

    @Test fun `loadoutHash uses reserved base sentinel for no equipment`() {
        assertEquals("base", loadoutHash(emptyList()))
    }
}
