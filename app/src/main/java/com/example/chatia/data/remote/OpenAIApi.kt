package com.example.chatia.data.remote

import com.example.chatia.data.model.ChatRequest
import com.example.chatia.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Body request: ChatRequest
    ): ChatResponse
}