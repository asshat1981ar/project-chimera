package com.chimera.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(
        connectTimeoutMs: Int = 15_000,
        socketTimeoutMs: Int = 30_000
    ): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(HttpRequestRetry) {
                maxRetries = 3
                retryOnServerErrors()
                retryOnException(retryOnTimeout = true)
                exponentialDelay(base = 2.0, maxDelayMs = 8_000)
            }
            engine {
                connectTimeout = connectTimeoutMs
                socketTimeout = socketTimeoutMs
            }
        }
    }
}
