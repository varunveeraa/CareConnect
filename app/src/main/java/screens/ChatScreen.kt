package com.example.careconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue

data class ChatMessage(
    val sender: String, // "LLM" or "EJ"
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage("EJ", "Is dad’s low activity a concern this week?"),
            ChatMessage("LLM", "He’s averaged only 1,200 steps/day this week—down from his usual 3,500. This drop may lead to stiffness and muscle weakness. Encourage light activity and hydration if he’s feeling sluggish.")
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Browse") },
                    label = { Text("BROWSE") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.InsertChart, contentDescription = "Patterns") },
                    label = { Text("PATTERNS") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("HOME") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("CHAT") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("SETTINGS") },
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Chat", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom
            ) {
                messages.forEach { message ->
                    ChatBubble(message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Send Message") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (messageText.text.isNotBlank()) {
                            messages.add(ChatMessage("EJ", messageText.text))
                            messageText = TextFieldValue("")
                        }
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isLLM = message.sender == "LLM"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isLLM) Arrangement.Start else Arrangement.End
    ) {
        if (isLLM) {
            Box(modifier = Modifier
                .clip(CircleShape)
                .background(Color.LightGray)
                .padding(4.dp)) {
                Text("LLM", fontSize = 10.sp)
            }
        }

        Box(
            modifier = Modifier
                .background(
                    if (isLLM) Color(0xFFDDEEFF) else Color(0xFFEFEFEF),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Visible
            )
        }

        if (!isLLM) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier
                .clip(CircleShape)
                .background(Color.LightGray)
                .padding(4.dp)) {
                Text("EJ", fontSize = 10.sp)
            }
        }
    }
}


