        package com.xai.chimera.api

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.mock.Calls
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

class DialogueApiServiceTest {

    private lateinit var service: DialogueApiService

    @Before
    fun setup() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = mock(DialogueApiService::class.java)
    }

    @Test
    fun `getDialogue returns successful response`() = runBlocking {
        val expectedResponse = DialogueResponse(
            id = "123",
            text = "Hello",
            emotions = mapOf("happy" to 0.9f),
            nextPrompts = listOf("How are you?"),
            conversationContext = emptyMap(),
            emotionalMetadata = null
        )
        `when`(service.getDialogue("123")).thenReturn(Response.success(expectedResponse))

        val response = service.getDialogue("123")
        assertTrue(response.isSuccessful)
        assertEquals(expectedResponse, response.body())
    }

    @Test
    fun `generateDialogue returns successful response`() = runBlocking {
        val request = DialogueRequest(
            prompt = "Hello",
            context = "Test context",
            options = null
        )
        val expectedResponse = DialogueResponse(
            id = "456",
            text = "Hi there!",
            emotions = emptyMap(),
            nextPrompts = null,
            conversationContext = emptyMap(),
            emotionalMetadata = null
        )
        `when`(service.generateDialogue(request)).thenReturn(Response.success(expectedResponse))

        val response = service.generateDialogue(request)
        assertTrue(response.isSuccessful)
        assertEquals(expectedResponse, response.body())
    }

    @Test
    fun `getDialogue returns error response`() = runBlocking {
        val errorResponse = Response.error<DialogueResponse>(
            404,
            ResponseBody.create("application/json".toMediaTypeOrNull(), "{\"error\":\"Not found\"}")
        )
        `when`(service.getDialogue("999")).thenReturn(errorResponse)

        val response = service.getDialogue("999")
        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }
}
