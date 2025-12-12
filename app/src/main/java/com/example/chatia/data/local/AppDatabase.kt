package com.example.chatia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatia.data.local.MessageDao
import com.example.chatia.data.local.MessageEntity
import com.example.chatia.data.local.ConversationEntity
import com.example.chatia.data.local.ConversationDao

@Database(entities = [MessageEntity::class, ConversationEntity::class], version = 2) // Versão 2
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
}