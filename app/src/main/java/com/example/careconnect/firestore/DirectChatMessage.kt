package com.example.careconnect.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Represents a single message in a direct message conversation
data class DirectChatMessage(
    @DocumentId
    val id: String = "",
    val conversationId: String = "", // Reference to DirectMessage document
    val senderId: String = "", // UID of the sender
    val receiverId: String = "", // UID of the receiver
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val messageType: String = "text", // text, image, etc.
    val deliveryStatus: String = "sent", // sent, delivered, read
    val isEdited: Boolean = false,
    val editedAt: Timestamp? = null
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        conversationId = "",
        senderId = "",
        receiverId = "",
        content = "",
        timestamp = Timestamp.now(),
        messageType = "text",
        deliveryStatus = "sent",
        isEdited = false,
        editedAt = null
    )
}

enum class MessageType {
    TEXT, IMAGE, FILE
}

enum class DeliveryStatus {
    SENT, DELIVERED, READ
}