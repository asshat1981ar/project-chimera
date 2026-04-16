package com.chimera.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

/**
 * REST client for the Chimera cloud-save Cloudflare Worker.
 *
 * All methods return [CloudSaveResult] — they never throw. A failure is
 * returned as [CloudSaveResult.Failure] so that callers (ViewModels) can
 * apply graceful-degradation: local Room DB is always the source of truth;
 * the cloud sync is best-effort.
 *
 * Retry policy: up to 3 attempts with exponential back-off (1s, 2s, 4s)
 * on network errors and 5xx responses. 401/404/400 are not retried.
 */
class CloudSaveRepository @Inject constructor(
    @Named("cloud_save_base_url") private val baseUrl: String,
    @Named("cloud_save_api_token") private val apiToken: String
) {
    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(HttpRequestRetry) {
            maxRetries = 3
            retryOnServerErrors()
            retryOnException(retryOnTimeout = true)
            exponentialDelay(base = 2.0, initialDelayMs = 1_000L, maxDelayMs = 8_000L)
        }
        engine {
            connectTimeout = 10_000
            socketTimeout  = 20_000
        }
    }

    private fun authHeader() = "Bearer $apiToken"

    // ── Upload (upsert) ───────────────────────────────────────────────────────

    suspend fun uploadSave(request: CloudSaveRequest): CloudSaveResult<CloudSaveAck> = runCatching {
        val response = client.post("$baseUrl/save") {
            headers { append("Authorization", authHeader()) }
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            return CloudSaveResult.Failure("Upload failed: HTTP ${response.status.value}")
        }
        CloudSaveResult.Success(response.body())
    }.getOrElse { CloudSaveResult.Failure("Upload error: ${it.message}") }

    // ── Download ──────────────────────────────────────────────────────────────

    suspend fun downloadSave(slotId: Long): CloudSaveResult<CloudSaveResponse?> = runCatching {
        val response = client.get("$baseUrl/save/$slotId") {
            headers { append("Authorization", authHeader()) }
        }
        when (response.status) {
            HttpStatusCode.OK       -> CloudSaveResult.Success(response.body())
            HttpStatusCode.NotFound -> CloudSaveResult.Success(null)
            else -> CloudSaveResult.Failure("Download failed: HTTP ${response.status.value}")
        }
    }.getOrElse { CloudSaveResult.Failure("Download error: ${it.message}") }

    // ── Delete ────────────────────────────────────────────────────────────────

    suspend fun deleteSave(slotId: Long): CloudSaveResult<CloudSaveAck> = runCatching {
        val response = client.delete("$baseUrl/save/$slotId") {
            headers { append("Authorization", authHeader()) }
        }
        if (!response.status.isSuccess()) {
            return CloudSaveResult.Failure("Delete failed: HTTP ${response.status.value}")
        }
        CloudSaveResult.Success(response.body())
    }.getOrElse { CloudSaveResult.Failure("Delete error: ${it.message}") }

    fun close() = client.close()
}

// ── Result wrapper — never throws to callers ──────────────────────────────────

sealed class CloudSaveResult<out T> {
    data class Success<T>(val data: T) : CloudSaveResult<T>()
    data class Failure(val reason: String) : CloudSaveResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    fun getOrNull(): T? = (this as? Success)?.data
}
