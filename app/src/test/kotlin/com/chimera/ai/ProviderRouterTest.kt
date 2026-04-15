package com.chimera.ai

import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderRouterTest {

    private val contract = SceneContract("test", "Test", "npc", "NPC", "room")
    private val input = PlayerInput("hello")
    private val state = CharacterState("npc", 1)

    private fun successProvider(name: String, line: String) = object : DialogueProvider {
        override suspend fun generateTurn(
            contract: SceneContract, playerInput: PlayerInput,
            characterState: CharacterState, recentMemories: List<MemoryShard>,
            turnHistory: List<DialogueTurnResult>
        ) = DialogueTurnResult(npcLine = line)
        override suspend fun generateIntents(
            contract: SceneContract, characterState: CharacterState,
            turnHistory: List<DialogueTurnResult>
        ) = listOf("Intent from $name")
        override suspend fun isAvailable() = true
    }

    private fun failingProvider() = object : DialogueProvider {
        override suspend fun generateTurn(
            contract: SceneContract, playerInput: PlayerInput,
            characterState: CharacterState, recentMemories: List<MemoryShard>,
            turnHistory: List<DialogueTurnResult>
        ): DialogueTurnResult = throw Exception("Provider failed")
        override suspend fun generateIntents(
            contract: SceneContract, characterState: CharacterState,
            turnHistory: List<DialogueTurnResult>
        ): List<String> = throw Exception("Intents failed")
        override suspend fun isAvailable() = true
    }

    private fun unavailableProvider() = object : DialogueProvider {
        override suspend fun generateTurn(
            contract: SceneContract, playerInput: PlayerInput,
            characterState: CharacterState, recentMemories: List<MemoryShard>,
            turnHistory: List<DialogueTurnResult>
        ): DialogueTurnResult = throw Exception("Should not be called")
        override suspend fun generateIntents(
            contract: SceneContract, characterState: CharacterState,
            turnHistory: List<DialogueTurnResult>
        ): List<String> = throw Exception("Should not be called")
        override suspend fun isAvailable() = false
    }

    @Test
    fun `uses first available provider`() = runTest {
        val router = ProviderRouter(listOf(
            ProviderRouter.NamedProvider("Primary", successProvider("Primary", "From primary")),
            ProviderRouter.NamedProvider("Secondary", successProvider("Secondary", "From secondary"))
        ))
        val result = router.generateTurn(contract, input, state, emptyList(), emptyList())
        assertEquals("From primary", result.npcLine)
        assertEquals("Primary", router.activeProviderName)
    }

    @Test
    fun `falls back when first provider fails`() = runTest {
        val router = ProviderRouter(listOf(
            ProviderRouter.NamedProvider("Failing", failingProvider()),
            ProviderRouter.NamedProvider("Backup", successProvider("Backup", "From backup"))
        ))
        val result = router.generateTurn(contract, input, state, emptyList(), emptyList())
        assertEquals("From backup", result.npcLine)
        assertEquals("Backup", router.activeProviderName)
    }

    @Test
    fun `skips unavailable providers`() = runTest {
        val router = ProviderRouter(listOf(
            ProviderRouter.NamedProvider("Unavailable", unavailableProvider()),
            ProviderRouter.NamedProvider("Available", successProvider("Available", "I'm here"))
        ))
        val result = router.generateTurn(contract, input, state, emptyList(), emptyList())
        assertEquals("I'm here", result.npcLine)
    }

    @Test(expected = Exception::class)
    fun `throws when all providers fail`() = runTest {
        val router = ProviderRouter(listOf(
            ProviderRouter.NamedProvider("Fail1", failingProvider()),
            ProviderRouter.NamedProvider("Fail2", failingProvider())
        ))
        router.generateTurn(contract, input, state, emptyList(), emptyList())
    }

    @Test
    fun `isAvailable returns true when any provider available`() = runTest {
        val router = ProviderRouter(listOf(
            ProviderRouter.NamedProvider("Down", unavailableProvider()),
            ProviderRouter.NamedProvider("Up", successProvider("Up", "ok"))
        ))
        assertTrue(router.isAvailable())
    }

    @Test
    fun `generates intents from first working provider`() = runTest {
        val router = ProviderRouter(listOf(
            ProviderRouter.NamedProvider("Primary", successProvider("Primary", "line"))
        ))
        val intents = router.generateIntents(contract, state, emptyList())
        assertEquals(1, intents.size)
        assertEquals("Intent from Primary", intents[0])
    }

    @Test
    fun `intent fallback on failure`() = runTest {
        val router = ProviderRouter(listOf(
            ProviderRouter.NamedProvider("Fail", failingProvider()),
            ProviderRouter.NamedProvider("Ok", successProvider("Ok", "line"))
        ))
        val intents = router.generateIntents(contract, state, emptyList())
        assertEquals("Intent from Ok", intents[0])
    }

    @Test
    fun `empty provider list reports unavailable`() = runTest {
        val router = ProviderRouter(emptyList())
        assertEquals(false, router.isAvailable())
    }
}
