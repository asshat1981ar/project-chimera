package com.chimera.data

import com.chimera.BuildConfig

/**
 * Provider mode determines how the AI dialogue system operates.
 */
enum class ProviderMode {
    /** Use cloud AI with automatic fallback to authored templates. */
    AUTO,
    /** Use only authored templates (offline mode). */
    FAKE,
    /** Use only Gemini provider (for testing). */
    GEMINI_ONLY,
    /** Use only Groq provider (for testing). */
    GROQ_ONLY
}

/**
 * Environment configuration derived from build variant.
 * Set via product flavors in build.gradle.kts (mock/dev/prod).
 */
object EnvironmentConfig {
    val providerMode: ProviderMode = try {
        ProviderMode.valueOf(BuildConfig.PROVIDER_MODE)
    } catch (_: Exception) {
        ProviderMode.AUTO
    }

    val apiBaseUrl: String = BuildConfig.API_BASE_URL
    val isDemoMode: Boolean = BuildConfig.DEMO_MODE
    val isDebug: Boolean = BuildConfig.DEBUG
    val geminiApiKey: String = BuildConfig.GEMINI_API_KEY
    val groqApiKey: String = BuildConfig.GROQ_API_KEY
    val openRouterApiKey: String = BuildConfig.OPENROUTER_API_KEY
}
