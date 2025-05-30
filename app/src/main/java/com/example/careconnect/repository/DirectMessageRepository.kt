package com.example.careconnect.repository

import android.util.Log
import com.example.careconnect.firestore.DirectMessage
import com.example.careconnect.firestore.DirectChatMessage
import com.example.careconnect.firestore.DeliveryStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DirectMessageRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    companion object {
        private const val CONVERSATIONS_COLLECTION = "direct_conversations"
        private const val MESSAGES_SUBCOLLECTION = "messages"
        private const val TAG = "DirectMessageRepository"
    }
    
    // Test Firestore connectivity
    suspend fun testFirestoreConnection(): String {
        return try {
            Log.d(TAG, "=== TESTING FIRESTORE CONNECTION ===")
            
            // Try to read from a simple collection
            val testResult = firestore.collection("test")
                .limit(1)
                .get()
                .await()
            
            Log.d(TAG, "Firestore connection successful")
            "✅ Firestore connected successfully"
        } catch (e: Exception) {
            Log.e(TAG, "Firestore connection failed", e)
            "❌ Firestore connection failed: ${e.message}"
        }
    }
    
    // Get all conversations for a user
    suspend fun getConversations(userId: String): List<DirectMessage> {
        return try {
            Log.d(TAG, "Getting conversations for user: $userId")
            
            // First try with ordering
            var result = try {
                firestore.collection(CONVERSATIONS_COLLECTION)
                    .whereArrayContains("participantIds", userId)
                    .whereEqualTo("isActive", true)
                    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to order by lastMessageTime, trying without ordering", e)
                // Fallback without ordering if lastMessageTime field doesn't exist for some documents
                firestore.collection(CONVERSATIONS_COLLECTION)
                    .whereArrayContains("participantIds", userId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()
            }
            
            val conversations = result.toObjects(DirectMessage::class.java)
            Log.d(TAG, "Found ${conversations.size} conversations for user $userId")
            
            conversations.forEach { conv ->
                Log.d(TAG, "Conversation: ${conv.id} - participants: ${conv.participantIds} - lastMessage: '${conv.lastMessage}' - active: ${conv.isActive}")
            }
            
            // Sort manually by lastMessageTime if needed
            conversations.sortedByDescending { it.lastMessageTime.seconds }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting conversations for user: $userId", e)
            emptyList()
        }
    }
    
    // Real-time flow of conversations for a user
    fun getConversationsFlow(userId: String): Flow<List<DirectMessage>> = callbackFlow {
        Log.d(TAG, "Setting up real-time conversations listener for user: $userId")
        
        val listener = try {
            firestore.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participantIds", userId)
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to conversations", error)
                        return@addSnapshotListener
                    }
                    
                    val conversations = snapshot?.toObjects(DirectMessage::class.java) ?: emptyList()
                    Log.d(TAG, "Real-time update: ${conversations.size} conversations for user $userId")
                    
                    conversations.forEach { conv ->
                        Log.d(TAG, "Real-time conversation: ${conv.id} with ${conv.participantIds} - '${conv.lastMessage}'")
                    }
                    
                    // Sort by lastMessageTime
                    val sortedConversations = conversations.sortedByDescending { it.lastMessageTime.seconds }
                    trySend(sortedConversations)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up conversations listener", e)
            // Send empty list as fallback
            trySend(emptyList())
            null
        }
        
        awaitClose { 
            listener?.remove()
            Log.d(TAG, "Removed conversations listener for user: $userId")
        }
    }
    
    // Create or get existing conversation between two users
    suspend fun createOrGetConversation(
        currentUserId: String,
        otherUserId: String,
        currentUserName: String,
        otherUserName: String
    ): DirectMessage {
        return try {
            Log.d(TAG, "Creating/getting conversation: $currentUserId <-> $otherUserId")
            
            // Check if conversation already exists
            val existingConversations = firestore.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participantIds", currentUserId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .toObjects(DirectMessage::class.java)
            
            val existingConversation = existingConversations.find { conversation ->
                conversation.participantIds.contains(otherUserId) && 
                conversation.participantIds.size == 2
            }
            
            if (existingConversation != null) {
                Log.d(TAG, "Found existing conversation: ${existingConversation.id}")
                return existingConversation
            }
            
            // Create new conversation
            val conversationId = UUID.randomUUID().toString()
            val newConversation = DirectMessage(
                id = conversationId,
                participantIds = listOf(currentUserId, otherUserId),
                participantNames = mapOf(
                    currentUserId to currentUserName,
                    otherUserId to otherUserName
                ),
                lastMessage = "",
                lastMessageTime = Timestamp.now(),
                lastMessageSender = "",
                unreadCount = mapOf(
                    currentUserId to 0,
                    otherUserId to 0
                ),
                createdAt = Timestamp.now(),
                isActive = true
            )
            
            Log.d(TAG, "=== CREATING NEW CONVERSATION ===")
            Log.d(TAG, "Conversation ID: $conversationId")
            Log.d(TAG, "Participants: ${newConversation.participantIds}")
            Log.d(TAG, "Participant names: ${newConversation.participantNames}")
            
            val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId)
            Log.d(TAG, "Saving to path: ${conversationRef.path}")
            
            conversationRef.set(newConversation).await()
            
            Log.d(TAG, "Conversation saved to Firestore")
            
            // Verify the conversation was saved
            val savedConversation = conversationRef.get().await()
            if (savedConversation.exists()) {
                Log.d(TAG, "Verification: Conversation exists in Firestore")
                val retrievedConversation = savedConversation.toObject(DirectMessage::class.java)
                Log.d(TAG, "Retrieved conversation: $retrievedConversation")
            } else {
                Log.e(TAG, "ERROR: Conversation not found in Firestore after saving!")
            }
            
            Log.d(TAG, "=== CONVERSATION CREATION COMPLETE ===")
            newConversation
        } catch (e: Exception) {
            Log.e(TAG, "Error creating/getting conversation", e)
            throw e
        }
    }
    
    // Get messages for a conversation
    suspend fun getMessages(conversationId: String): List<DirectChatMessage> {
        return try {
            Log.d(TAG, "Getting messages for conversation: $conversationId")
            val result = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val messages = result.toObjects(DirectChatMessage::class.java)
            Log.d(TAG, "Found ${messages.size} messages")
            messages
        } catch (e: Exception) {
            Log.e(TAG, "Error getting messages", e)
            emptyList()
        }
    }
    
    // Real-time flow of messages for a conversation
    fun getMessagesFlow(conversationId: String): Flow<List<DirectChatMessage>> = callbackFlow {
        Log.d(TAG, "=== SETTING UP MESSAGE LISTENER ===")
        Log.d(TAG, "ConversationId: $conversationId")
        
        val messagesPath = "$CONVERSATIONS_COLLECTION/$conversationId/$MESSAGES_SUBCOLLECTION"
        Log.d(TAG, "Messages path: $messagesPath")
        
        val listener = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(MESSAGES_SUBCOLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "=== ERROR IN MESSAGE LISTENER ===", error)
                    return@addSnapshotListener
                }
                
                Log.d(TAG, "=== MESSAGE LISTENER UPDATE ===")
                Log.d(TAG, "Snapshot exists: ${snapshot != null}")
                Log.d(TAG, "Document count: ${snapshot?.size() ?: 0}")
                
                val messages = snapshot?.toObjects(DirectChatMessage::class.java) ?: emptyList()
                Log.d(TAG, "Parsed ${messages.size} messages")
                
                messages.forEachIndexed { index, msg ->
                    Log.d(TAG, "Message $index: ${msg.id} | ${msg.senderId} -> ${msg.receiverId} | '${msg.content}'")
                }
                
                trySend(messages)
                Log.d(TAG, "=== MESSAGE UPDATE SENT TO UI ===")
            }
        
        awaitClose { 
            Log.d(TAG, "=== REMOVING MESSAGE LISTENER ===")
            listener.remove() 
        }
    }
    
    // Send a message
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        receiverId: String,
        content: String
    ): DirectChatMessage {
        return try {
            Log.d(TAG, "=== SENDING MESSAGE ===")
            Log.d(TAG, "ConversationId: $conversationId")
            Log.d(TAG, "SenderId: $senderId")
            Log.d(TAG, "ReceiverId: $receiverId")
            Log.d(TAG, "Content: '$content'")
            
            val messageId = UUID.randomUUID().toString()
            Log.d(TAG, "Generated messageId: $messageId")
            
            val message = DirectChatMessage(
                id = messageId,
                conversationId = conversationId,
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                timestamp = Timestamp.now(),
                messageType = "text",
                deliveryStatus = DeliveryStatus.SENT.name.lowercase(),
                isEdited = false
            )
            
            Log.d(TAG, "Created message object: $message")
            
            // Save message to Firestore
            val messageRef = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_SUBCOLLECTION)
                .document(messageId)
            
            Log.d(TAG, "Saving to Firestore path: ${messageRef.path}")
            
            messageRef.set(message).await()
            
            Log.d(TAG, "Message saved to Firestore successfully")
            
            // Verify the message was saved
            val savedMessage = messageRef.get().await()
            if (savedMessage.exists()) {
                Log.d(TAG, "Verification: Message exists in Firestore")
                val retrievedMessage = savedMessage.toObject(DirectChatMessage::class.java)
                Log.d(TAG, "Retrieved message: $retrievedMessage")
            } else {
                Log.e(TAG, "ERROR: Message not found in Firestore after saving!")
            }
            
            // Update conversation with last message info
            Log.d(TAG, "Updating conversation last message...")
            updateConversationLastMessage(conversationId, content, senderId, receiverId)
            Log.d(TAG, "Conversation update completed")
            
            Log.d(TAG, "=== MESSAGE SEND COMPLETE ===")
            message
        } catch (e: Exception) {
            Log.e(TAG, "=== ERROR SENDING MESSAGE ===", e)
            throw e
        }
    }
    
    // Update conversation with last message and increment unread count
    private suspend fun updateConversationLastMessage(
        conversationId: String,
        lastMessage: String,
        senderId: String,
        receiverId: String
    ) {
        try {
            val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            
            val conversation = conversationRef.get().await().toObject(DirectMessage::class.java)
            if (conversation != null) {
                val updatedUnreadCount = conversation.unreadCount.toMutableMap()
                val currentReceiverUnread = updatedUnreadCount[receiverId] ?: 0
                updatedUnreadCount[receiverId] = currentReceiverUnread + 1
                
                conversationRef.update(
                    mapOf(
                        "lastMessage" to lastMessage,
                        "lastMessageTime" to Timestamp.now(),
                        "lastMessageSender" to senderId,
                        "unreadCount" to updatedUnreadCount
                    )
                ).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating conversation last message", e)
        }
    }
    
    // Mark messages as read
    suspend fun markMessagesAsRead(conversationId: String, userId: String) {
        try {
            Log.d(TAG, "Marking messages as read for user: $userId in conversation: $conversationId")
            
            // Reset unread count for the user
            val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            
            val conversation = conversationRef.get().await().toObject(DirectMessage::class.java)
            if (conversation != null) {
                val updatedUnreadCount = conversation.unreadCount.toMutableMap()
                updatedUnreadCount[userId] = 0
                
                conversationRef.update("unreadCount", updatedUnreadCount).await()
            }
            
            // Update message delivery status to read for messages where user is receiver
            val unreadMessages = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_SUBCOLLECTION)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("deliveryStatus", DeliveryStatus.DELIVERED.name.lowercase())
                .get()
                .await()
            
            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "deliveryStatus", DeliveryStatus.READ.name.lowercase())
            }
            batch.commit().await()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read", e)
        }
    }
    
    // Mark messages as delivered
    suspend fun markMessagesAsDelivered(conversationId: String, userId: String) {
        try {
            Log.d(TAG, "Marking messages as delivered for user: $userId in conversation: $conversationId")
            
            val sentMessages = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_SUBCOLLECTION)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("deliveryStatus", DeliveryStatus.SENT.name.lowercase())
                .get()
                .await()
            
            val batch = firestore.batch()
            sentMessages.documents.forEach { doc ->
                batch.update(doc.reference, "deliveryStatus", DeliveryStatus.DELIVERED.name.lowercase())
            }
            batch.commit().await()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as delivered", e)
        }
    }
    
    // Get total unread count for a user across all conversations
    suspend fun getTotalUnreadCount(userId: String): Int {
        return try {
            val conversations = getConversations(userId)
            conversations.sumOf { it.getUnreadCountFor(userId) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total unread count", e)
            0
        }
    }
    
    // Delete conversation (soft delete)
    suspend fun deleteConversation(conversationId: String) {
        try {
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .update("isActive", false)
                .await()
            
            Log.d(TAG, "Conversation deleted: $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting conversation", e)
            throw e
        }
    }
}
