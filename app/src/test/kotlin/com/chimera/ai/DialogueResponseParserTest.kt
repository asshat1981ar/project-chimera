package com.chimera.ai

import com.chimera.model.SceneContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DialogueResponseParserTest {

    @Test
    fun `parses valid JSON response`() {
        val json = """{"npcLine":"Hello traveler.","emotion":"warm","relationshipDelta":0.05,"flags":[],"memoryCandidates":[]}"""
        val result = DialogueResponseParser.parse(json)
        assertNotNull(result)
        assertEquals("Hello traveler.", result!!.npcLine)
        assertEquals("warm", result.emotion)
        assertEquals(0.05f, result.relationshipDelta, 0.001f)
    }

    @Test
    fun `strips markdown code fences`() {
        val raw = """```json
{"npcLine":"Fenced response.","emotion":"neutral","relationshipDelta":0.0,"flags":[],"memoryCandidates":[]}
```"""
        val result = DialogueResponseParser.parse(raw)
        assertNotNull(result)
        assertEquals("Fenced response.", result!!.npcLine)
    }

    @Test
    fun `handles preamble text before JSON`() {
        val raw = """Here is the NPC response:
{"npcLine":"With preamble.","emotion":"guarded","relationshipDelta":-0.1,"flags":["scene_ending"],"memoryCandidates":["Player was cautious"]}"""
        val result = DialogueResponseParser.parse(raw)
        assertNotNull(result)
        assertEquals("With preamble.", result!!.npcLine)
        assertTrue(result.flags.contains("scene_ending"))
        assertEquals(1, result.memoryCandidates.size)
    }

    @Test
    fun `clamps relationship delta to valid range`() {
        val json = """{"npcLine":"Extreme delta.","emotion":"hostile","relationshipDelta":5.0,"flags":[],"memoryCandidates":[]}"""
        val result = DialogueResponseParser.parse(json)
        assertNotNull(result)
        assertTrue(result!!.relationshipDelta <= 0.25f)
    }

    @Test
    fun `handles alternative field names via manual parse`() {
        val json = """{"response":"Alt field name.","emotion":"cold"}"""
        val result = DialogueResponseParser.parse(json)
        assertNotNull(result)
        assertEquals("Alt field name.", result!!.npcLine)
    }

    @Test
    fun `returns null for completely invalid input`() {
        val result = DialogueResponseParser.parse("not json at all")
        assertNull(result)
    }

    @Test
    fun `returns null for empty string`() {
        val result = DialogueResponseParser.parse("")
        assertNull(result)
    }

    @Test
    fun `parses intent array`() {
        val json = """["I seek truth.","None of your concern.","Tell me more.","I should go."]"""
        val intents = DialogueResponseParser.parseIntents(json)
        assertNotNull(intents)
        assertEquals(4, intents!!.size)
        assertEquals("I seek truth.", intents[0])
    }

    @Test
    fun `parses intents from markdown fenced response`() {
        val raw = """```json
["Option A","Option B","Option C"]
```"""
        val intents = DialogueResponseParser.parseIntents(raw)
        assertNotNull(intents)
        assertEquals(3, intents!!.size)
    }

    @Test
    fun `intent parse returns null for invalid input`() {
        val intents = DialogueResponseParser.parseIntents("not an array")
        assertNull(intents)
    }

    @Test
    fun `handles missing optional fields gracefully`() {
        val json = """{"npcLine":"Minimal response."}"""
        val result = DialogueResponseParser.parse(json)
        assertNotNull(result)
        assertEquals("Minimal response.", result!!.npcLine)
        assertEquals("neutral", result.emotion)
        assertEquals(0f, result.relationshipDelta, 0.001f)
        assertTrue(result.flags.isEmpty())
    }

    @Test
    fun `limits memory candidates to 3`() {
        val json = """{"npcLine":"Many memories.","emotion":"warm","relationshipDelta":0.0,"flags":[],"memoryCandidates":["a","b","c","d","e"]}"""
        val result = DialogueResponseParser.parse(json)
        assertNotNull(result)
        assertTrue(result!!.memoryCandidates.size <= 3)
    }

    // --- SceneContract validation tests ---

    private val contractWithForbidden = SceneContract(
        sceneId = "test", sceneTitle = "Test", npcId = "npc", npcName = "NPC",
        setting = "room", forbiddenTopics = listOf("escape_route", "king_identity")
    )

    @Test
    fun `parseAndValidate passes clean response`() {
        val json = """{"npcLine":"The hollow is ancient.","emotion":"neutral","relationshipDelta":0.0,"flags":[],"memoryCandidates":[]}"""
        val result = DialogueResponseParser.parseAndValidate(json, contractWithForbidden)
        assertNotNull(result)
        assertEquals("The hollow is ancient.", result!!.npcLine)
    }

    @Test
    fun `parseAndValidate strips forbidden topic mention`() {
        val json = """{"npcLine":"Let me tell you about the escape route out of here.","emotion":"neutral","relationshipDelta":0.0,"flags":[],"memoryCandidates":[]}"""
        val result = DialogueResponseParser.parseAndValidate(json, contractWithForbidden)
        assertNotNull(result)
        assertTrue(result!!.npcLine.contains("cannot speak"))
    }

    @Test
    fun `parseAndValidate detects underscore-separated forbidden topics`() {
        val json = """{"npcLine":"The king identity is known to few.","emotion":"neutral","relationshipDelta":0.0,"flags":[],"memoryCandidates":[]}"""
        val result = DialogueResponseParser.parseAndValidate(json, contractWithForbidden)
        assertNotNull(result)
        assertTrue(result!!.npcLine.contains("cannot speak"))
    }

    @Test
    fun `parseAndValidate with no forbidden topics passes everything`() {
        val noForbidden = contractWithForbidden.copy(forbiddenTopics = emptyList())
        val json = """{"npcLine":"Talk about escape route freely.","emotion":"neutral","relationshipDelta":0.0,"flags":[],"memoryCandidates":[]}"""
        val result = DialogueResponseParser.parseAndValidate(json, noForbidden)
        assertNotNull(result)
        assertEquals("Talk about escape route freely.", result!!.npcLine)
    }
}
