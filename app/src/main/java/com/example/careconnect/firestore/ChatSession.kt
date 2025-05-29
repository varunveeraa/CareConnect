package com.example.careconnect.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatSession(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "New Chat",
    val lastMessage: String = "",
    val lastUpdated: Timestamp = Timestamp.now(),
    val messageCount: Int = 0
)