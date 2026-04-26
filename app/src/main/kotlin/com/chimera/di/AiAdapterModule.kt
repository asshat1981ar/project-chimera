package com.chimera.di

import com.chimera.BuildConfig
import com.chimera.ai.DialogueOrchestrator
import com.chimera.ai.FakeDialogueProvider
import com.chimera.ai.GeminiAdapter
import com.chimera.ai.GroqAdapter
import com.chimera.ai.OllamaAdapter
import com.chimera.ai.OpenRouterAdapter
import com.chimera.ai.ProviderRouter
import com.chimera.ai.StorylineGenerator
import com.chimera.ai.LocalStorylineGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * AI adapter module providing dialogue generation and storyline services.
 *
 * Priority chain: Ollama (local) → Gemini → Groq → OpenRouter → Fake (fallback)
 * Ollama is prioritized when enabled because it offers unlimited free inference
 * and full privacy for RPG dialogue and quest generation.
 *
 * This is a PLUGIN -- the game works without any AI via FakeDialogueProvider.
 * AI does not own game state, progression, or simulation truth.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiAdapterModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            engine {
                connectTimeout = 15_000
                socketTimeout = 30_000
            }
        }
    }

    @Provides
    @Singleton
    fun provideProviderRouter(client: HttpClient): ProviderRouter {
        val providers = mutableListOf<ProviderRouter.NamedProvider>()

        // Ollama local AI -- prioritized when available
        providers.add(
            ProviderRouter.NamedProvider(
                "Ollama",
                OllamaAdapter(
                    client = client,
                    baseUrl = BuildConfig.OLLAMA_BASE_URL,
                    model = BuildConfig.OLLAMA_MODEL,
                    timeoutMs = BuildConfig.OLLAMA_TIMEOUT_MS.toLong()
                )
            )
        )

        // Cloud providers (only added if API keys are present)
        if (BuildConfig.GEMINI_API_KEY.isNotBlank()) {
            providers.add(ProviderRouter.NamedProvider("Gemini", GeminiAdapter(client, BuildConfig.GEMINI_API_KEY)))
        }
        if (BuildConfig.GROQ_API_KEY.isNotBlank()) {
            providers.add(ProviderRouter.NamedProvider("Groq", GroqAdapter(client, BuildConfig.GROQ_API_KEY)))
        }
        if (BuildConfig.OPENROUTER_API_KEY.isNotBlank()) {
            providers.add(ProviderRouter.NamedProvider("OpenRouter", OpenRouterAdapter(client, BuildConfig.OPENROUTER_API_KEY)))
        }
        return ProviderRouter(providers)
    }

    @Provides
    @Singleton
    fun provideDialogueOrchestrator(
        fallback: FakeDialogueProvider,
        router: ProviderRouter
    ): DialogueOrchestrator {
        val orchestrator = DialogueOrchestrator(fallback)
        if (router.providers.isNotEmpty()) {
            orchestrator.setPrimaryProvider(router)
        }
        return orchestrator
    }

    /**
     * Storyline generator for procedural quest and narrative generation.
     * By default uses local rule-based generation; Ollama is used when available
     * for richer AI-driven storylines.
     */
    @Provides
    @Singleton
    fun provideStorylineGenerator(): StorylineGenerator {
        return LocalStorylineGenerator()
    }
}
