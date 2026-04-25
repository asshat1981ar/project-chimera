package com.chimera.network

import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [CloudSaveRepository] using Ktor's MockEngine.
 */
class CloudSaveRepositoryTest {

    private lateinit var repository: CloudSaveRepository

    private val testBaseUrl = "https://api.chimera.example.com"
    private val testApiToken = "test-token-12345"
    private val testSlotId = 42L

    private val testRequest = CloudSaveRequest(
        slotId = testSlotId,
        playerName = "TestPlayer",
        chapterTag = "prologue",
        playtimeSeconds = 3600L,
        saveDataJson = """{"key":"value"}"""
    )

    @After
    fun tearDown() {
        if (::repository.isInitialized) {
            repository.close()
        }
    }

    // =========================================================================
    // UPLOAD TESTS
    // =========================================================================

    @Test
    fun `uploadSave_success returns Success with CloudSaveAck`() = runTest {
        var capturedRequest: io.ktor.client.request.HttpRequestData? = null
        val mockEngine = MockEngine { request ->
            capturedRequest = request
            respond(
                content = """{"ok":true,"slot_id":42,"updated_at":1234567890}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.uploadSave(testRequest)

        assertThat(result).isInstanceOf(CloudSaveResult.Success::class.java)
        val success = result as CloudSaveResult.Success
        assertThat(success.data.ok).isTrue()
        assertThat(success.data.slotId).isEqualTo(testSlotId)
        assertThat(capturedRequest?.url?.encodedPath).isEqualTo("/save")
        assertThat(capturedRequest?.method).isEqualTo(HttpMethod.Post)
        assertThat(capturedRequest?.headers?.get(HttpHeaders.Authorization)).isEqualTo("Bearer $testApiToken")
    }

    @Test
    fun `uploadSave with 401 Unauthorized returns Failure without retry`() = runTest {
        var callCount = 0
        val mockEngine = MockEngine {
            callCount++
            respond(
                content = """{"error":"Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.uploadSave(testRequest)

        assertThat(callCount).isEqualTo(1)
        assertThat(result).isInstanceOf(CloudSaveResult.Failure::class.java)
        assertThat((result as CloudSaveResult.Failure).reason).contains("401")
    }

    @Test
    fun `uploadSave with 400 Bad Request returns Failure without retry`() = runTest {
        var callCount = 0
        val mockEngine = MockEngine {
            callCount++
            respond(
                content = """{"error":"Bad Request"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.uploadSave(testRequest)

        assertThat(callCount).isEqualTo(1)
        assertThat(result).isInstanceOf(CloudSaveResult.Failure::class.java)
        assertThat((result as CloudSaveResult.Failure).reason).contains("400")
    }

    @Test
    fun `uploadSave with 500 Internal Server Error triggers retry`() = runTest {
        var callCount = 0
        val mockEngine = MockEngine {
            callCount++
            if (callCount == 1) {
                respond(
                    content = """{"error":"Internal Server Error"}""",
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond(
                    content = """{"ok":true,"slot_id":42,"updated_at":1234567890}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.uploadSave(testRequest)

        // Verifies retry occurs: initial attempt + 1 retry before success
        assertThat(callCount).isEqualTo(2)
        assertThat(result).isInstanceOf(CloudSaveResult.Success::class.java)
    }

    @Test
    fun `uploadSave with 503 Service Unavailable triggers retry`() = runTest {
        var callCount = 0
        val mockEngine = MockEngine {
            callCount++
            respond(
                content = """{"error":"Service Unavailable"}""",
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.uploadSave(testRequest)

        // Verifies retry occurs on 5xx errors
        assertThat(callCount).isAtLeast(2)
        assertThat(result).isInstanceOf(CloudSaveResult.Failure::class.java)
        assertThat((result as CloudSaveResult.Failure).reason).contains("503")
    }

    // =========================================================================
    // DOWNLOAD TESTS
    // =========================================================================

    @Test
    fun `downloadSave_success returns Success with CloudSaveResponse`() = runTest {
        var capturedRequest: io.ktor.client.request.HttpRequestData? = null
        val mockEngine = MockEngine { request ->
            capturedRequest = request
            respond(
                content = """{
                    "slot_id": 42,
                    "player_name": "TestPlayer",
                    "chapter_tag": "prologue",
                    "playtime_seconds": 3600,
                    "save_data_json": "{\"key\":\"value\"}",
                    "updated_at": 1234567890
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.downloadSave(testSlotId)

        assertThat(result).isInstanceOf(CloudSaveResult.Success::class.java)
        val success = result as CloudSaveResult.Success
        assertThat(success.data).isNotNull()
        assertThat(success.data?.slotId).isEqualTo(testSlotId)
        assertThat(success.data?.playerName).isEqualTo("TestPlayer")
        assertThat(capturedRequest?.url?.encodedPath).isEqualTo("/save/$testSlotId")
        assertThat(capturedRequest?.method).isEqualTo(HttpMethod.Get)
    }

    @Test
    fun `downloadSave with 404 Not Found returns Success with null`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"error":"Not Found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.downloadSave(999L)

        assertThat(result).isInstanceOf(CloudSaveResult.Success::class.java)
        assertThat((result as CloudSaveResult.Success).data).isNull()
    }

    @Test
    fun `downloadSave with 500 Internal Server Error returns Failure`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"error":"Internal Server Error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.downloadSave(testSlotId)

        assertThat(result).isInstanceOf(CloudSaveResult.Failure::class.java)
        assertThat((result as CloudSaveResult.Failure).reason).contains("500")
    }

    @Test
    fun `downloadSave with 401 Unauthorized returns Failure`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"error":"Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.downloadSave(testSlotId)

        assertThat(result).isInstanceOf(CloudSaveResult.Failure::class.java)
        assertThat((result as CloudSaveResult.Failure).reason).contains("401")
    }

    // =========================================================================
    // DELETE TESTS
    // =========================================================================

    @Test
    fun `deleteSave_success returns Success with CloudSaveAck`() = runTest {
        var capturedRequest: io.ktor.client.request.HttpRequestData? = null
        val mockEngine = MockEngine { request ->
            capturedRequest = request
            respond(
                content = """{"ok":true,"deleted":42}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.deleteSave(testSlotId)

        assertThat(result).isInstanceOf(CloudSaveResult.Success::class.java)
        val success = result as CloudSaveResult.Success
        assertThat(success.data.ok).isTrue()
        assertThat(success.data.deleted).isEqualTo(testSlotId)
        assertThat(capturedRequest?.url?.encodedPath).isEqualTo("/save/$testSlotId")
        assertThat(capturedRequest?.method).isEqualTo(HttpMethod.Delete)
    }

    @Test
    fun `deleteSave with 204 No Content returns Success`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"ok":true}""",
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.deleteSave(testSlotId)

        assertThat(result).isInstanceOf(CloudSaveResult.Success::class.java)
    }

    @Test
    fun `deleteSave with 500 Internal Server Error returns Failure`() = runTest {
        var callCount = 0
        val mockEngine = MockEngine {
            callCount++
            respond(
                content = """{"error":"Internal Server Error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.deleteSave(testSlotId)

        // Verifies retry occurs: maxRetries=3 means 4 total attempts
        assertThat(callCount).isGreaterThan(1)
        assertThat(result).isInstanceOf(CloudSaveResult.Failure::class.java)
        assertThat((result as CloudSaveResult.Failure).reason).contains("500")
    }

    @Test
    fun `deleteSave with 404 Not Found returns Failure`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"error":"Not Found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        val result = repository.deleteSave(999L)

        assertThat(result).isInstanceOf(CloudSaveResult.Failure::class.java)
        assertThat((result as CloudSaveResult.Failure).reason).contains("404")
    }

    // =========================================================================
    // AUTHORIZATION HEADER TESTS
    // =========================================================================

    @Test
    fun `all requests include Bearer token authorization header`() = runTest {
        val authHeaders = mutableListOf<String?>()
        val mockEngine = MockEngine { request ->
            authHeaders.add(request.headers[HttpHeaders.Authorization])
            respond(
                content = """{"ok":true,"slot_id":42}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = CloudSaveRepository(testBaseUrl, testApiToken, createTestClient(mockEngine))

        repository.uploadSave(testRequest)
        repository.downloadSave(testSlotId)
        repository.deleteSave(testSlotId)

        assertThat(authHeaders).hasSize(3)
        authHeaders.forEach { header ->
            assertThat(header).isEqualTo("Bearer $testApiToken")
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private fun createTestClient(mockEngine: MockEngine) = io.ktor.client.HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }
}
