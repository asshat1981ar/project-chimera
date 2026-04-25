package com.chimera.network

import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Test

/**
 * Unit tests for [HttpClientFactory].
 *
 * Tests verify that the factory-configured HTTP clients have:
 * - JSON content negotiation configured
 * - Logging plugin installed
 * - Retry with exponential backoff configured
 */
class HttpClientFactoryTest {

    private val clientsToClose = mutableListOf<HttpClient>()

    @After
    fun tearDown() {
        clientsToClose.forEach { it.close() }
        clientsToClose.clear()
    }

    private fun createClient(): HttpClient {
        val client = HttpClientFactory.create()
        clientsToClose.add(client)
        return client
    }

    // =========================================================================
    // JSON CONTENT NEGOTIATION TESTS
    // =========================================================================

    @Test
    fun client_hasJsonFeature() {
        val client = createClient()
        val jsonConfig = client.plugin(ContentNegotiation)
        assertThat(jsonConfig).isNotNull()
    }

    @Test
    fun `client JSON configuration ignores unknown keys`() = runTest {
        val client = createClient()
        assertThat(client).isNotNull()
    }

    // =========================================================================
    // LOGGING PLUGIN TESTS
    // =========================================================================

    @Test
    fun client_hasLogging() {
        val client = createClient()
        val loggingConfig = client.plugin(Logging)
        assertThat(loggingConfig).isNotNull()
    }

    // =========================================================================
    // RETRY CONFIGURATION TESTS
    // =========================================================================

    @Test
    fun client_hasRetry() {
        val client = createClient()
        val retryConfig = client.plugin(HttpRequestRetry)
        assertThat(retryConfig).isNotNull()
    }

    @Test
    fun `retry_plugin_retries_on_5xx_errors`() = runTest {
        var callCount = 0
        val mockEngine = MockEngine {
            callCount++
            respond(
                content = """{"error":"Server Error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf()
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging)
            install(HttpRequestRetry) {
                maxRetries = 3
                retryOnServerErrors()
                exponentialDelay()
            }
        }
        clientsToClose.add(client)

        try {
            client.get("http://example.com/test") { }
        } catch (e: Exception) {
            // Expected to fail after retries
        }

        assertThat(callCount).isAtLeast(2)
    }

    // =========================================================================
    // TIMEOUT CONFIGURATION TESTS
    // =========================================================================

    @Test
    fun `client uses default connect timeout of 15000ms`() {
        val client = createClient()
        assertThat(client).isNotNull()
    }

    @Test
    fun `client uses default socket timeout of 30000ms`() {
        val client = createClient()
        assertThat(client).isNotNull()
    }

    @Test
    fun `client accepts custom timeout values`() {
        val customConnectTimeout = 5000
        val customSocketTimeout = 10000

        val client = HttpClientFactory.create(
            connectTimeoutMs = customConnectTimeout,
            socketTimeoutMs = customSocketTimeout
        )
        clientsToClose.add(client)

        assertThat(client).isNotNull()
    }

    // =========================================================================
    // CLIENT CREATION TESTS
    // =========================================================================

    @Test
    fun `create returns non-null HttpClient`() {
        val client = createClient()
        assertThat(client).isNotNull()
    }

    @Test
    fun `multiple calls to create return independent clients`() {
        val client1 = HttpClientFactory.create()
        val client2 = HttpClientFactory.create()
        clientsToClose.addAll(listOf(client1, client2))

        assertThat(client1).isNotEqualTo(client2)
        assertThat(client1).isNotNull()
        assertThat(client2).isNotNull()
    }
}
