package com.xai.chimera.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface for DialogueAPI service calls
 */
interface DialogueApiService {
    @GET("dialogue/{id}")
    suspend fun getDialogue(@Path("id") id: String): Response<DialogueResponse>
    
    @POST("dialogue/generate")
    suspend fun generateDialogue(@Body request: DialogueRequest): Response<DialogueResponse>
}

data class DialogueRequest(
    val prompt: String,
    val context: String,
    val options: Map<String, Any>? = null
)

data class DialogueResponse(
    val id: String,
    val text: String,
    val emotions: List<Emotion>,
    val nextPrompts: List<String>? = null
)

data class Emotion(
    val type: String,
    val intensity: Float
)
