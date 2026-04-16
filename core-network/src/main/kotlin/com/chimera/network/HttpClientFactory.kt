package com.chimera.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
            engine {
                connectTimeout = connectTimeoutMs
                socketTimeout = socketTimeoutMs
            }
        }
    }
}
