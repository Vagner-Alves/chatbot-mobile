package com.example.chatia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.chatia.data.local.AppDatabase // Corrected import
import com.example.chatia.data.remote.retrofit.RetrofitClient
import com.example.chatia.data.repository.ChatRepository
import com.example.chatia.ui.about.AboutScreen
import com.example.chatia.ui.chat.ChatScreen
import com.example.chatia.ui.chat.ChatViewModel
import com.example.chatia.ui.chat.ChatViewModelFactory
import com.example.chatia.ui.history.HistoryScreen
import com.example.chatia.ui.navigation.BottomNavigationBar
import com.example.chatia.ui.navigation.Screen
import com.example.chatia.ui.theme.ChatIATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Setup de Dependências (Banco e Repositório)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "chat-database"
        )   .fallbackToDestructiveMigration()
            .build()

        val repository = ChatRepository(RetrofitClient.openAIApi, db.messageDao())
        val viewModelFactory = ChatViewModelFactory(repository)

        setContent {
            ChatIATheme {
                val navController = rememberNavController()
                val sharedViewModel: ChatViewModel = viewModel(factory = viewModelFactory)
                Scaffold(
                    bottomBar = {
                        // Componente desacoplado sendo chamado aqui
                        BottomNavigationBar(navController = navController)
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Chat.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.Chat.route) {
                            ChatScreen(viewModel = sharedViewModel)
                        }

                        composable(Screen.History.route) {
                            HistoryScreen(
                                viewModel = sharedViewModel,
                                navController = navController
                            )
                        }

                        composable(Screen.About.route) {
                            AboutScreen()
                        }
                    }
                }
            }
        }
    }
}




