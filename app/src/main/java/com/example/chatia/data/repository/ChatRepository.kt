package com.example.chatia.data.repository

import com.example.chatia.data.local.MessageDao
import com.example.chatia.data.local.MessageEntity
import com.example.chatia.data.model.ChatRequest
import com.example.chatia.data.model.ChatResponse
import com.example.chatia.data.remote.OpenAIApi
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val openAIApi: OpenAIApi,
    private val messageDao: MessageDao
) {
    // Ler do banco
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    // Salvar no banco
    suspend fun saveMessage(text: String, isFromUser: Boolean) {
        messageDao.insertMessage(MessageEntity(text = text, isFromUser = isFromUser))
    }

    suspend fun getChatCompletion(request: ChatRequest): Result<ChatResponse> {
        return try {
            val response = openAIApi.getChatCompletion(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}