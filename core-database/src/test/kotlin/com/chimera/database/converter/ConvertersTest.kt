package com.chimera.database.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromFloatMap serializes empty map`() {
        val result = converters.fromFloatMap(emptyMap())
        assertEquals("{}", result)
    }

    @Test
    fun `fromFloatMap serializes populated map`() {
        val map = mapOf("joy" to 0.8f, "anger" to 0.3f)
        val json = converters.fromFloatMap(map)
        assertTrue(json.contains("joy"))
        assertTrue(json.contains("anger"))
    }

    @Test
    fun `toFloatMap deserializes valid JSON`() {
        val json = """{"joy":0.8,"anger":0.3}"""
        val map = converters.toFloatMap(json)
        assertEquals(2, map.size)
        assertEquals(0.8f, map["joy"]!!, 0.01f)
        assertEquals(0.3f, map["anger"]!!, 0.01f)
    }

    @Test
    fun `toFloatMap returns empty map for malformed JSON`() {
        val result = converters.toFloatMap("not valid json")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toFloatMap returns empty map for empty string`() {
        val result = converters.toFloatMap("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toFloatMap returns empty map for null-like JSON`() {
        val result = converters.toFloatMap("null")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `round trip preserves data`() {
        val original = mapOf("trust" to 0.75f, "fear" to 0.1f, "hope" to 0.5f)
        val json = converters.fromFloatMap(original)
        val restored = converters.toFloatMap(json)
        assertEquals(original.size, restored.size)
        original.forEach { (key, value) ->
            assertEquals(value, restored[key]!!, 0.001f)
        }
    }

    @Test
    fun `handles empty JSON object`() {
        val result = converters.toFloatMap("{}")
        assertTrue(result.isEmpty())
    }
}
