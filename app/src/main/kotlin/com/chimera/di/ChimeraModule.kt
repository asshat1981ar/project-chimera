package com.chimera.di

import com.chimera.BuildConfig
import com.chimera.ai.DialogueOrchestrator
import com.chimera.ai.GeminiAdapter
import com.chimera.ai.GroqAdapter
import com.chimera.ai.OpenRouterAdapter
import com.chimera.ai.ProviderRouter
import com.chimera.core.events.GameEventBus
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

@Module
@InstallIn(SingletonComponent::class)
object ChimeraModule {

    @Provides
    @Singleton
    fun provideGameEventBus(): GameEventBus = GameEventBus()

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

        if (BuildConfig.GEMINI_API_KEY.isNotBlank()) {
            providers.add(ProviderRouter.NamedProvider(
                "Gemini", GeminiAdapter(client, BuildConfig.GEMINI_API_KEY)
            ))
        }
        if (BuildConfig.GROQ_API_KEY.isNotBlank()) {
            providers.add(ProviderRouter.NamedProvider(
                "Groq", GroqAdapter(client, BuildConfig.GROQ_API_KEY)
            ))
        }
        if (BuildConfig.OPENROUTER_API_KEY.isNotBlank()) {
            providers.add(ProviderRouter.NamedProvider(
                "OpenRouter", OpenRouterAdapter(client, BuildConfig.OPENROUTER_API_KEY)
            ))
        }

        return ProviderRouter(providers)
    }

    @Provides
    @Singleton
    fun provideDialogueOrchestrator(
        fallback: com.chimera.ai.FakeDialogueProvider,
        router: ProviderRouter
    ): DialogueOrchestrator {
        val orchestrator = DialogueOrchestrator(fallback)
        if (router.providers.isNotEmpty()) {
            orchestrator.setPrimaryProvider(router)
        }
        return orchestrator
    }
}
