package com.example.chatia.data.repository

import com.example.chatia.data.local.ConversationEntity
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
    // Listar todas as conversas para o Histórico
    val allConversations: Flow<List<ConversationEntity>> = messageDao.getAllConversations()

    // Pegar mensagens de uma conversa específica
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByConversation(conversationId)
    }

    // Criar nova conversa
    suspend fun createConversation(title: String): Long {
        return messageDao.insertConversation(ConversationEntity(title = title))
    }

    // Deletar conversa
    suspend fun deleteConversation(conversationId: Long) {
        messageDao.deleteConversation(conversationId)
    }

    // Salvar mensagem vinculada a uma conversa
    suspend fun saveMessage(conversationId: Long, text: String, isFromUser: Boolean) {
        messageDao.insertMessage(
            MessageEntity(conversationId = conversationId, text = text, isFromUser = isFromUser)
        )
    }

    // Chamada API (sem alterações)
    suspend fun getChatCompletion(request: ChatRequest): Result<ChatResponse> {
        return try {
            val response = openAIApi.getChatCompletion(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}