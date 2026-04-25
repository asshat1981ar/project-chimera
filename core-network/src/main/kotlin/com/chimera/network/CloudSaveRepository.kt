package com.chimera.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.contentType
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

/**
 * REST client for the Chimera cloud-save Cloudflare Worker.
 *
 * All methods return [CloudSaveResult] — they never throw. A failure is
 * returned as [CloudSaveResult.Failure] so that callers (ViewModels) can
 * apply graceful-degradation: local Room DB is always the source of truth;
 * the cloud sync is best-effort.
 *
 * Retry policy: up to 3 attempts with exponential back-off on network errors
 * and 5xx responses. 401/404/400 are not retried.
 */
class CloudSaveRepository(
    private val baseUrl: String,
    private val apiToken: String,
    private val client: HttpClient = HttpClientFactory.create(connectTimeoutMs = 10_000, socketTimeoutMs = 20_000)
) {
    private fun authHeader() = "Bearer $apiToken"

    suspend fun uploadSave(request: CloudSaveRequest): CloudSaveResult<CloudSaveAck> = runCatching {
        val response = client.post("$baseUrl/save") {
            headers { append("Authorization", authHeader()) }
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            return@runCatching CloudSaveResult.Failure("Upload failed: HTTP ${response.status.value}")
        }
        CloudSaveResult.Success(response.body<CloudSaveAck>())
    }.getOrElse { CloudSaveResult.Failure("Upload error: ${it.message}") }

    suspend fun downloadSave(slotId: Long): CloudSaveResult<CloudSaveResponse?> = runCatching {
        val response = client.get("$baseUrl/save/$slotId") {
            headers { append("Authorization", authHeader()) }
        }
        when (response.status) {
            HttpStatusCode.OK       -> CloudSaveResult.Success(response.body<CloudSaveResponse>())
            HttpStatusCode.NotFound -> CloudSaveResult.Success(null)
            else -> CloudSaveResult.Failure("Download failed: HTTP ${response.status.value}")
        }
    }.getOrElse { CloudSaveResult.Failure("Download error: ${it.message}") }

    suspend fun deleteSave(slotId: Long): CloudSaveResult<CloudSaveAck> = runCatching {
        val response = client.delete("$baseUrl/save/$slotId") {
            headers { append("Authorization", authHeader()) }
        }
        if (!response.status.isSuccess()) {
            return@runCatching CloudSaveResult.Failure("Delete failed: HTTP ${response.status.value}")
        }
        CloudSaveResult.Success(response.body<CloudSaveAck>())
    }.getOrElse { CloudSaveResult.Failure("Delete error: ${it.message}") }

    fun close() = client.close()
}

sealed class CloudSaveResult<out T> {
    data class Success<T>(val data: T) : CloudSaveResult<T>()
    data class Failure(val reason: String) : CloudSaveResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    fun getOrNull(): T? = (this as? Success)?.data
}
