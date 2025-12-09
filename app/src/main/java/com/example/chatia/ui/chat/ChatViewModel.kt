package com.example.chatia.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatia.data.model.ChatRequest
import com.example.chatia.data.model.Message
import com.example.chatia.data.model.ResponseMessage
import com.example.chatia.data.remote.retrofit.RetrofitClient
import com.example.chatia.data.repository.ChatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 2. Declaração de Eventos de Erro (Toast, etc)
    private val _oneShotEvents = Channel<ChatOneShotEvent>()
    val oneShotEvents = _oneShotEvents.receiveAsFlow()
    val messages: StateFlow<List<Message>> = repository.allMessages
        .map { entities ->
            entities.map { Message(it.text, it.isFromUser) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ... (isLoading e oneShotEvents permanecem iguais)

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _isLoading.value = true

        viewModelScope.launch {
            // 1. Salva a mensagem do usuário no banco
            repository.saveMessage(text, true)

            // 2. Monta o request para a API
            val chatRequest = ChatRequest(
                model = "gpt-3.5-turbo",
                messages = messages.value.map { // Pega o histórico atual
                    ResponseMessage(if (it.isFromUser) "user" else "assistant", it.text)
                }
            )

            // 3. Chama a API
            val result = repository.getChatCompletion(chatRequest)
            _isLoading.value = false

            result.onSuccess {
                val content = it.choices.firstOrNull()?.message?.content
                if (content != null) {
                    // 4. Salva a resposta da IA no banco (a UI atualiza sozinha via Flow)
                    repository.saveMessage(content, false)
                }
            }
            // ... (tratamento de erro igual)
        }
    }
}

sealed class ChatOneShotEvent {
    data class ShowError(val message: String) : ChatOneShotEvent()
}

// Factory para instanciar o ViewModel com o repositório
class ChatViewModelFactory(private val repository: ChatRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}