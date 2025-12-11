package com.example.chatia.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Message
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Chat: Screen("chat", "Chat", Icons.Default.Message)
    object History: Screen("history", "Histórico", Icons.Default.List)
    object About: Screen("about", "Sobre", Icons.Default.Info)
}