package com.example.careconnect.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Represents a direct message conversation between two users
data class DirectMessage(
    @DocumentId
    val id: String = "",
    val participantIds: List<String> = emptyList(), // UIDs of both participants (always 2)
    val participantNames: Map<String, String> = emptyMap(), // UID -> Name mapping
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val lastMessageSender: String = "",
    val unreadCount: Map<String, Int> = emptyMap(), // UID -> unread count for that user
    val createdAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        participantIds = emptyList(),
        participantNames = emptyMap(),
        lastMessage = "",
        lastMessageTime = Timestamp.now(),
        lastMessageSender = "",
        unreadCount = emptyMap(),
        createdAt = Timestamp.now(),
        isActive = true
    )
    
    // Get the other participant's UID
    fun getOtherParticipant(currentUserUid: String): String? {
        return participantIds.find { it != currentUserUid }
    }
    
    // Get the other participant's name
    fun getOtherParticipantName(currentUserUid: String): String {
        val otherUid = getOtherParticipant(currentUserUid)
        return otherUid?.let { participantNames[it] } ?: "Unknown User"
    }
    
    // Get unread count for a specific user
    fun getUnreadCountFor(userUid: String): Int {
        return unreadCount[userUid] ?: 0
    }
}