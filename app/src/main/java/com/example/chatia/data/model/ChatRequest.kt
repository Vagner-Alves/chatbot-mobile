package com.example.chatia.data.model

data class ChatRequest(
    val model: String,
    val messages: List<ResponseMessage>
)

data class ResponseMessage(
    val role: String,
    val content: String
)