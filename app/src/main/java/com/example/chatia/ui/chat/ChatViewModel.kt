package com.example.chatia.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatia.data.local.ConversationEntity
import com.example.chatia.data.model.ChatRequest
import com.example.chatia.data.model.Message
import com.example.chatia.data.model.ResponseMessage
import com.example.chatia.data.repository.ChatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    // ID da conversa atual (null = nova conversa)
    private val _currentConversationId = MutableStateFlow<Long?>(null)

    // Lista de Conversas (para a tela de Histórico)
    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensagens da conversa ATUAL (reage quando mudamos o ID)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<Message>> = _currentConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) // Se não tem ID, lista vazia
            else repository.getMessages(id).map { entities ->
                entities.map { Message(it.text, it.isFromUser) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _oneShotEvents = Channel<ChatOneShotEvent>()
    val oneShotEvents = _oneShotEvents.receiveAsFlow()

    // Função para começar um chat novo (limpa a tela)
    fun startNewChat() {
        _currentConversationId.value = null
    }

    // Função para selecionar um chat do histórico
    fun selectConversation(id: Long) {
        _currentConversationId.value = id
    }

    // Função para deletar conversa
    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null // Se deletou a atual, limpa a tela
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _isLoading.value = true

        viewModelScope.launch {
            // 1. Verifica se já existe conversa, se não, cria
            var chatId = _currentConversationId.value
            if (chatId == null) {
                // Usa os primeiros 30 chars como título
                val title = if (text.length > 30) text.take(30) + "..." else text
                chatId = repository.createConversation(title)
                _currentConversationId.value = chatId
            }

            // 2. Salva msg do usuário
            repository.saveMessage(chatId, text, true)

            // 3. API Request (mantém histórico do contexto)
            val historyForContext = messages.value.map {
                ResponseMessage(if (it.isFromUser) "user" else "assistant", it.text)
            }
            // Adiciona a msg atual ao contexto (pois o flow do banco pode demorar ms pra atualizar)
            val currentContext = historyForContext + ResponseMessage("user", text)

            val chatRequest = ChatRequest(
                model = "gpt-3.5-turbo",
                messages = currentContext
            )

            val result = repository.getChatCompletion(chatRequest)
            _isLoading.value = false

            result.onSuccess {
                val content = it.choices.firstOrNull()?.message?.content
                if (content != null) {
                    repository.saveMessage(chatId, content, false)
                }
            }.onFailure {
                _oneShotEvents.send(ChatOneShotEvent.ShowError("Erro: ${it.localizedMessage}"))
            }
        }
    }
}

// Classe para eventos únicos (como erros)
sealed class ChatOneShotEvent {
    data class ShowError(val message: String) : ChatOneShotEvent()
}
class ChatViewModelFactory(private val repository: com.example.chatia.data.repository.ChatRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}