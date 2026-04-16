package com.chimera.data

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NightEventProviderTest {

    private lateinit var provider: NightEventProvider

    @Before
    fun setup() {
        provider = NightEventProvider()
    }

    @Test
    fun `returns non-null event for neutral morale`() {
        val event = provider.getRandomEvent(0.5f)
        assertNotNull(event)
        assertTrue(event.title.isNotBlank())
    }

    @Test
    fun `returns non-null event for low morale`() {
        val event = provider.getRandomEvent(0.1f)
        assertNotNull(event)
    }

    @Test
    fun `returns non-null event for high morale`() {
        val event = provider.getRandomEvent(0.9f)
        assertNotNull(event)
    }

    @Test
    fun `all events have at least 2 choices`() {
        // Run many times to sample the full pool
        val seen = mutableSetOf<String>()
        repeat(100) {
            val event = provider.getRandomEvent(0.5f)
            seen.add(event.id)
            assertTrue("Event ${event.id} has ${event.choices.size} choices", event.choices.size >= 2)
        }
        // Should have seen multiple distinct events
        assertTrue("Only saw ${seen.size} distinct events", seen.size >= 3)
    }

    @Test
    fun `all events have non-blank narrative`() {
        repeat(50) {
            val event = provider.getRandomEvent(0.5f)
            assertTrue(event.narrative.isNotBlank())
        }
    }

    @Test
    fun `all choices have non-blank outcome text`() {
        repeat(50) {
            val event = provider.getRandomEvent(0.5f)
            event.choices.forEach { choice ->
                assertTrue("Choice '${choice.text}' has blank outcome", choice.outcome.isNotBlank())
            }
        }
    }

    @Test
    fun `low morale biases toward tension events`() {
        var tensionCount = 0
        val tensionIds = setOf("strange_noise", "companion_argument", "supplies_stolen", "watch_fire_dies")
        repeat(100) {
            val event = provider.getRandomEvent(0.1f)
            if (event.id in tensionIds) tensionCount++
        }
        // At 10% morale, 60% chance of tension events per call
        assertTrue("Expected tension bias, got $tensionCount/100", tensionCount > 30)
    }

    @Test
    fun `morale deltas are bounded`() {
        repeat(50) {
            val event = provider.getRandomEvent(0.5f)
            event.choices.forEach { choice ->
                assertTrue(choice.moraleDelta >= -0.15f)
                assertTrue(choice.moraleDelta <= 0.15f)
            }
        }
    }
}
