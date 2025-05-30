package com.example.careconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.database.User
import com.example.careconnect.firestore.ChatMessage
import com.example.careconnect.firestore.ChatSession
import com.example.careconnect.viewmodel.ChatViewModel
import com.example.careconnect.util.ChatDataFixer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUser: User,
    chatViewModel: ChatViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    var showChatHistory by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUserUid = firebaseAuth.currentUser?.uid ?: currentUser.email

    val chatSessions by chatViewModel.chatSessions.collectAsState()
    val currentChatMessages by chatViewModel.currentChatMessages.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val currentChatId by chatViewModel.currentChatId.collectAsState()

    // LazyColumn state for auto-scrolling
    val listState = rememberLazyListState()

    // Load chat sessions when the screen is first opened
    LaunchedEffect(currentUserUid) {
        chatViewModel.loadChatSessions(currentUserUid)
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(currentChatMessages.size) {
        if (currentChatMessages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(currentChatMessages.size - 1)
            }
        }
    }

    // Function to send message
    fun sendMessage() {
        if (messageText.isNotBlank() && !isLoading) {
            chatViewModel.sendMessage(messageText, currentUserUid)
            messageText = ""
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with history toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showChatHistory) "Chat History" else "AI Assistant",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                if (!showChatHistory && currentChatId != null) {
                    IconButton(onClick = { chatViewModel.startNewChat() }) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat")
                    }
                }
                IconButton(onClick = { showChatHistory = !showChatHistory }) {
                    Icon(
                        if (showChatHistory) Icons.AutoMirrored.Filled.Chat else Icons.Default.History,
                        contentDescription = if (showChatHistory) "Current Chat" else "Chat History"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showChatHistory) {
            // Chat History View
            ChatHistoryView(
                chatSessions = chatSessions,
                onChatSelect = { session ->
                    chatViewModel.selectChat(session.id, currentUserUid)
                    showChatHistory = false
                }
            )
        } else {
            // Current Chat View
            Column(modifier = Modifier.weight(1f)) {
                if (currentChatMessages.isEmpty() && currentChatId == null) {
                    // Welcome message for new chat
                    WelcomeMessage()
                } else {
                    // Chat messages
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        state = listState,
                        reverseLayout = false,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentChatMessages) { message ->
                            ChatBubble(message = message, currentUser = currentUser)
                        }

                        if (isLoading) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }
                }
            }

            // Message input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Ask me anything...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { sendMessage() }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { sendMessage() },
                    enabled = messageText.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.height(56.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = "AI Assistant",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI Health Assistant",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ask me about health patterns, medication reminders, or general health questions",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = "AI",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = "AI is typing...",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ChatHistoryView(
    chatSessions: List<ChatSession>,
    onChatSelect: (ChatSession) -> Unit
) {
    if (chatSessions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ChatBubbleOutline,
                contentDescription = "No Chats",
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No chat history yet",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Text(
                text = "Start a conversation to see your chat history here",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn {
            items(chatSessions) { session ->
                ChatSessionItem(
                    session = session,
                    onClick = { onChatSelect(session) }
                )
            }
        }
    }
}

@Composable
fun ChatSessionItem(
    session: ChatSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.title,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatTimestamp(session.lastUpdated.toDate()),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = session.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val actualMessageCount = maxOf(0, session.messageCount - 1)
            Text(
                text = if (actualMessageCount == 1) "1 message" else "$actualMessageCount messages",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, currentUser: User) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
                    .widthIn(max = 240.dp)
            ) {
                Text(
                    text = message.content.trim(),
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatMessageTimestamp(message.timestamp.toDate()),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser.fullName.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatTimestamp(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInHours = diffInMillis / (1000 * 60 * 60)
    val diffInDays = diffInHours / 24

    return when {
        diffInHours < 1 -> "Just now"
        diffInHours < 24 -> "${diffInHours}h ago"
        diffInDays < 7 -> "${diffInDays}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}

private fun formatMessageTimestamp(date: Date): String {
    val now = Date()
    val calendar = Calendar.getInstance()
    val messageCalendar = Calendar.getInstance().apply { time = date }
    
    val isToday = calendar.get(Calendar.DAY_OF_YEAR) == messageCalendar.get(Calendar.DAY_OF_YEAR) &&
                  calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR)
    
    val isYesterday = calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.let { yesterday ->
        yesterday.get(Calendar.DAY_OF_YEAR) == messageCalendar.get(Calendar.DAY_OF_YEAR) &&
        yesterday.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR)
    }
    
    return when {
        isToday -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        isYesterday -> "Yesterday ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(date)
    }
}
