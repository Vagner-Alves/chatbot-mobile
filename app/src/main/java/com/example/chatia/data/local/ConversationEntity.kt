package com.example.chatia.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // O título será o começo da primeira mensagem
    val timestamp: Long = System.currentTimeMillis()
)