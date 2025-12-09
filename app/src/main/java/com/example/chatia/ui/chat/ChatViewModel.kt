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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _oneShotEvents = Channel<ChatOneShotEvent>()
    val oneShotEvents = _oneShotEvents.receiveAsFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = Message(text, true)
        _messages.value = _messages.value + userMessage

        _isLoading.value = true

        viewModelScope.launch {
            val chatRequest = ChatRequest(
                model = "gpt-3.5-turbo", // Ou o modelo que você preferir
                messages = _messages.value.map { message ->
                    ResponseMessage(
                        role = if (message.isFromUser) "user" else "assistant",
                        content = message.text
                    )
                }
            )

            val result = repository.getChatCompletion(chatRequest)
            _isLoading.value = false

            result.onSuccess {
                val assistantMessageContent = it.choices.firstOrNull()?.message?.content
                if (assistantMessageContent != null) {
                    val assistantMessage = Message(assistantMessageContent, false)
                    _messages.value = _messages.value + assistantMessage
                } else {
                    _oneShotEvents.send(ChatOneShotEvent.ShowError("Resposta vazia da IA"))
                }
            }.onFailure {
                _oneShotEvents.send(ChatOneShotEvent.ShowError("Erro: ${it.localizedMessage}"))
            }
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