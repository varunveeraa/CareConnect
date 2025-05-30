package com.example.careconnect.repository

import android.util.Log
import com.example.careconnect.firestore.ChatMessage
import com.example.careconnect.firestore.ChatSession
import com.example.careconnect.util.ApiKeyManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val apiKey = ApiKeyManager.GEMINI_API_KEY
    
    suspend fun createChatSession(userId: String, firstMessage: String): ChatSession {
        val sessionId = UUID.randomUUID().toString()
        val chatSession = ChatSession(
            id = sessionId,
            userId = userId,
            title = firstMessage.take(50) + if (firstMessage.length > 50) "..." else "",
            lastMessage = firstMessage,
            lastUpdated = Timestamp.now(),
            messageCount = 1
        )
        
        // Store in users/{userId}/chats/{chatId}
        firestore.collection("users")
            .document(userId)
            .collection("chats")
            .document(sessionId)
            .set(chatSession)
            .await()
            
        return chatSession
    }
    
    suspend fun getChatSessions(userId: String): List<ChatSession> {
        return try {
            // Get from users/{userId}/chats
            val result = firestore.collection("users")
                .document(userId)
                .collection("chats")
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .get()
                .await()
            
            result.toObjects(ChatSession::class.java)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error getting chat sessions", e)
            emptyList()
        }
    }
    
    suspend fun getChatMessages(userId: String, chatId: String): List<ChatMessage> {
        return try {
            // Get from users/{userId}/chats/{chatId}/messages
            val result = firestore.collection("users")
                .document(userId)
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            
            Log.d("ChatRepository", "Raw documents retrieved: ${result.documents.size}")
            
            // Manually parse documents to ensure proper field mapping
            val messages = result.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    val id = doc.id
                    val chatId = data["chatId"] as? String ?: ""
                    val content = data["content"] as? String ?: ""
                    val isUser = data["isUser"] as? Boolean ?: false // Explicit handling
                    val timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now()
                    val userId = data["userId"] as? String ?: ""
                    
                    Log.d("ChatRepository", "Parsing doc $id: isUser=$isUser, content=${content.take(30)}")
                    
                    ChatMessage(
                        id = id,
                        chatId = chatId,
                        content = content,
                        isUser = isUser,
                        timestamp = timestamp,
                        userId = userId
                    )
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error parsing document ${doc.id}", e)
                    null
                }
            }
            
            Log.d("ChatRepository", "Successfully parsed ${messages.size} messages for chat $chatId")
            
            // Log final messages
            messages.forEachIndexed { index, message ->
                Log.d("ChatRepository", "Final Message $index - isUser: ${message.isUser}, content: ${message.content.take(50)}")
            }
            
            messages
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error getting chat messages", e)
            emptyList()
        }
    }
    
    suspend fun saveMessage(userId: String, chatMessage: ChatMessage): ChatMessage {
        val messageId = UUID.randomUUID().toString()
        val messageWithId = chatMessage.copy(
            id = messageId,
            timestamp = Timestamp.now() // Explicitly set timestamp
        )
        
        // Create explicit map to ensure all fields are saved correctly
        val messageData = mapOf(
            "id" to messageWithId.id,
            "chatId" to messageWithId.chatId,
            "content" to messageWithId.content,
            "isUser" to messageWithId.isUser,
            "timestamp" to messageWithId.timestamp,
            "userId" to messageWithId.userId
        )
        
        Log.d("ChatRepository", "Saving message - isUser: ${messageWithId.isUser}, content: ${messageWithId.content}")
        
        // Store in users/{userId}/chats/{chatId}/messages/{messageId}
        firestore.collection("users")
            .document(userId)
            .collection("chats")
            .document(chatMessage.chatId)
            .collection("messages")
            .document(messageId)
            .set(messageData) // Use explicit map instead of object
            .await()
            
        // Update chat session
        updateChatSession(userId, chatMessage.chatId, chatMessage.content)
        
        return messageWithId
    }
    
    private suspend fun updateChatSession(userId: String, chatId: String, lastMessage: String) {
        try {
            // Update users/{userId}/chats/{chatId}
            val sessionRef = firestore.collection("users")
                .document(userId)
                .collection("chats")
                .document(chatId)
                
            val sessionDoc = sessionRef.get().await()
            
            if (sessionDoc.exists()) {
                val currentCount = sessionDoc.getLong("messageCount") ?: 0
                sessionRef.update(
                    mapOf(
                        "lastMessage" to lastMessage,
                        "lastUpdated" to Timestamp.now(),
                        "messageCount" to currentCount + 1
                    )
                ).await()
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error updating chat session", e)
        }
    }

    suspend fun sendToGemini(message: String): String = withContext(Dispatchers.IO) {
        val modelNames = listOf(
            "gemini-pro",
            "gemini-1.5-pro",
            "gemini-1.5-flash",
            "gemini-2.0-flash-exp"
        )
        
        for (modelName in modelNames) {
            try {
                Log.d("ChatRepository", "Trying model: $modelName with message: $message")
                
                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", message)
                                })
                            })
                        })
                    })
                }
                
                Log.d("ChatRepository", "Request body: ${requestBody.toString()}")
                
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 30000
                    readTimeout = 30000
                }
                
                // Write request body
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }
                
                Log.d("ChatRepository", "Making API request to $modelName...")
                val responseCode = connection.responseCode
                Log.d("ChatRepository", "Response code for $modelName: $responseCode")
                
                val responseBody = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error message"
                }
                
                Log.d("ChatRepository", "Response body for $modelName: $responseBody")
                
                if (responseCode == HttpURLConnection.HTTP_OK && responseBody.isNotEmpty()) {
                    val jsonResponse = JSONObject(responseBody)
                    
                    if (jsonResponse.has("candidates")) {
                        val candidates = jsonResponse.getJSONArray("candidates")
                        if (candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            if (candidate.has("content")) {
                                val content = candidate.getJSONObject("content")
                                if (content.has("parts")) {
                                    val parts = content.getJSONArray("parts")
                                    if (parts.length() > 0) {
                                        val aiResponse = parts.getJSONObject(0).getString("text")
                                        Log.d("ChatRepository", "Success with $modelName! AI response: $aiResponse")
                                        return@withContext aiResponse
                                    }
                                }
                            }
                        }
                    }
                    
                    Log.e("ChatRepository", "No valid response content found for $modelName in: $responseBody")
                } else {
                    Log.e("ChatRepository", "API call failed for $modelName with code: $responseCode, body: $responseBody")
                    // Continue to next model
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error calling Gemini API with model $modelName", e)
                // Continue to next model
            }
        }
        
        return@withContext "Sorry, I couldn't connect to the AI service at the moment. Please check your internet connection and try again."
    }
}
