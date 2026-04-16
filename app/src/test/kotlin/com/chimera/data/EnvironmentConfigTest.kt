package com.chimera.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EnvironmentConfigTest {

    @Test
    fun `ProviderMode AUTO exists`() {
        assertEquals("AUTO", ProviderMode.AUTO.name)
    }

    @Test
    fun `ProviderMode FAKE exists`() {
        assertEquals("FAKE", ProviderMode.FAKE.name)
    }

    @Test
    fun `ProviderMode GEMINI_ONLY exists`() {
        assertEquals("GEMINI_ONLY", ProviderMode.GEMINI_ONLY.name)
    }

    @Test
    fun `ProviderMode valueOf works for all modes`() {
        ProviderMode.values().forEach { mode ->
            assertEquals(mode, ProviderMode.valueOf(mode.name))
        }
    }

    @Test
    fun `ProviderMode has 4 values`() {
        assertEquals(4, ProviderMode.values().size)
    }
}
