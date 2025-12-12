package com.example.chatia.ui.chat

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatia.data.model.Message
import com.example.chatia.data.remote.retrofit.RetrofitClient
import com.example.chatia.data.repository.ChatRepository
import com.example.chatia.ui.theme.ChatIATheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel){
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.oneShotEvents.collect {
            when (it) {
                is ChatOneShotEvent.ShowError -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ChatIA") },
                actions = {
                    // Botão de Nova Conversa (+)
                    IconButton(onClick = { viewModel.startNewChat() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Add, // Importe o icone Add
                            contentDescription = "Nova Conversa"
                        )
                    }
                })
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("me pergunte sobre qualquer coisa") },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Campo de texto para digitar a mensagem" },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(24.dp)

                )
                IconButton(
                    onClick = { 
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(48.dp)
                        .semantics { contentDescription = "Botão para enviar mensagem" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = listState
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                        .semantics { contentDescription = "Carregando resposta da IA" }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class) // Necessário para o combinedClickable
@Composable
fun MessageBubble(message: Message) {
    val bubbleColor = if (message.isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isFromUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    // Ferramentas para Copiar e Feedback Tátil
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .weight(1f, fill = false) // fill=false para não esticar mensagens curtas
                .background(bubbleColor)
                // Adicionamos o comportamento de clique aqui
                .combinedClickable(
                    onClick = { /* Ação de clique simples (opcional) */ },
                    onLongClick = {
                        // 1. Copia o texto para a área de transferência
                        clipboardManager.setText(AnnotatedString(message.text))

                        // 2. Dá um feedback tátil (vibraçãozinha)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        // 3. Mostra um aviso visual
                        Toast.makeText(context, "Texto copiado!", Toast.LENGTH_SHORT).show()
                    }
                )
        ) {
            // Se você já estiver usando MarkdownText (do passo anterior), mantenha-o.
            // Se estiver usando Text padrão, é assim:
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatIATheme {
        // Isso aqui daria erro, pois o Repository agora pede o DAO
        ChatScreen(viewModel = ChatViewModel(ChatRepository(
            RetrofitClient.openAIApi,
            messageDao = TODO()
        )))
    }
}

