package com.example.chatia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.chatia.data.local.AppDatabase
import com.example.chatia.data.remote.retrofit.RetrofitClient
import com.example.chatia.data.repository.ChatRepository
import com.example.chatia.ui.chat.ChatScreen
import com.example.chatia.ui.chat.ChatViewModel
import com.example.chatia.ui.chat.ChatViewModelFactory
import com.example.chatia.ui.chat.MessageBubble
import com.example.chatia.ui.theme.ChatIATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Inicializa o Banco de Dados (Room)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "chat-database"
        ).build()

        // 2. Cria o repositório passando a API (Retrofit) e o DAO (Banco local)
        val repository = ChatRepository(
            RetrofitClient.openAIApi,
            db.messageDao()
        )

        // 3. Cria a Factory para o ViewModel
        val viewModelFactory = ChatViewModelFactory(repository)

        setContent {
            ChatIATheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Chat.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        // Rota do CHAT
                        composable(Screen.Chat.route) {
                            // Obtém o ViewModel usando a Factory criada acima
                            val viewModel: ChatViewModel = viewModel(factory = viewModelFactory)
                            ChatScreen(viewModel = viewModel)
                        }

                        // Rota do HISTÓRICO
                        composable(Screen.History.route) {
                            // Reutiliza o mesmo ViewModel para exibir o histórico
                            val viewModel: ChatViewModel = viewModel(factory = viewModelFactory)
                            HistoryScreen(viewModel = viewModel)
                        }

                        // Rota SOBRE
                        composable(Screen.About.route) {
                            AboutScreen()
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Chat : Screen("chat", "Chat", Icons.Default.Message)
    object History : Screen("history", "Histórico", Icons.Default.List)
    object About : Screen("about", "Sobre", Icons.Default.Info)
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screen.Chat, Screen.History, Screen.About)
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryScreen(viewModel: ChatViewModel) { // <-- AQUI estava faltando o parâmetro
    // Observa a lista de mensagens do banco de dados (conectado no passo 4)
    val messages by viewModel.messages.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Histórico de Conversas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (messages.isEmpty()) {
                Text(
                    text = "Nenhuma conversa salva ainda.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                // Exibe a lista usando o componente LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        // Reutiliza o componente visual que já criamos para o Chat
                        MessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ChatIA Mobile",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Versão 1.0.0",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Desenvolvido por Vagner Alves",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Projeto acadêmico utilizando Kotlin, Jetpack Compose e OpenAI API.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
