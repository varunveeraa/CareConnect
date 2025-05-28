package com.example.careconnect.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ChatDataFixer {
    
    suspend fun fixExistingChatData(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        
        try {
            Log.d("ChatDataFixer", "Starting to fix chat data for user: $userId")
            
            // Get all chat sessions for the user
            val chatSessions = firestore.collection("users")
                .document(userId)
                .collection("chats")
                .get()
                .await()
            
            for (chatDoc in chatSessions.documents) {
                val chatId = chatDoc.id
                Log.d("ChatDataFixer", "Fixing chat: $chatId")
                
                // Get all messages in this chat
                val messages = firestore.collection("users")
                    .document(userId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .orderBy("timestamp")
                    .get()
                    .await()
                
                // Smart detection: First message is usually user, then alternate
                // But also check content patterns to be more accurate
                messages.documents.forEachIndexed { index, messageDoc ->
                    val currentData = messageDoc.data
                    val content = currentData?.get("content") as? String ?: ""
                    
                    // Detect if message is likely from AI based on content patterns
                    val looksLikeAI = isLikelyAIMessage(content)
                    
                    // Determine correct isUser value
                    val shouldBeUser = if (index == 0) {
                        // First message should be user unless it clearly looks like AI
                        !looksLikeAI
                    } else {
                        // For subsequent messages, alternate but respect AI patterns
                        if (looksLikeAI) false else (index % 2 == 0)
                    }
                    
                    val currentIsUser = currentData?.get("isUser") as? Boolean ?: true
                    
                    if (currentIsUser != shouldBeUser) {
                        Log.d("ChatDataFixer", "Fixing message ${messageDoc.id}: '$content' -> isUser: $currentIsUser to $shouldBeUser")
                        
                        // Update the document
                        messageDoc.reference.update("isUser", shouldBeUser).await()
                    } else {
                        Log.d("ChatDataFixer", "Message ${messageDoc.id} already correct: isUser=$currentIsUser")
                    }
                }
            }
            
            Log.d("ChatDataFixer", "Completed fixing chat data for user: $userId")
            
        } catch (e: Exception) {
            Log.e("ChatDataFixer", "Error fixing chat data", e)
        }
    }
    
    private fun isLikelyAIMessage(content: String): Boolean {
        val aiPatterns = listOf(
            "I can help",
            "I'm here to",
            "As an AI",
            "I understand",
            "Let me",
            "Based on",
            "I recommend",
            "You might want to",
            "It's important to",
            "I suggest",
            "Here are some",
            "You can try",
            "I'd be happy to",
            "Sorry, I couldn't",
            "Sorry, there was an error"
        )
        
        return aiPatterns.any { pattern ->
            content.contains(pattern, ignoreCase = true)
        }
    }
}
