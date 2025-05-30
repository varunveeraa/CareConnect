package com.example.careconnect.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatMessage(
    @DocumentId
    val id: String = "",
    val chatId: String = "",
    val content: String = "",
    val isUser: Boolean = false, // For AI chats - indicates if message is from user
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String = ""
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", false, Timestamp.now(), "")
}
