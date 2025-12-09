package com.example.chatia.data.repository

import com.example.chatia.data.model.ChatRequest
import com.example.chatia.data.model.ChatResponse
import com.example.chatia.data.remote.OpenAIApi

class ChatRepository(private val openAIApi: OpenAIApi) {

    suspend fun getChatCompletion(request: ChatRequest): Result<ChatResponse> {
        return try {
            val response = openAIApi.getChatCompletion(
                request = request
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}