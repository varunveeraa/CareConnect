package com.example.careconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.firestore.ChatMessage
import com.example.careconnect.firestore.ChatSession
import com.example.careconnect.repository.ChatRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()
    
    private val _currentChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentChatMessages: StateFlow<List<ChatMessage>> = _currentChatMessages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()
    
    fun loadChatSessions(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sessions = chatRepository.getChatSessions(userId)
                _chatSessions.value = sessions
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadChatMessages(chatId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentChatId.value = chatId
            try {
                val messages = chatRepository.getChatMessages(userId, chatId)
                _currentChatMessages.value = messages
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendMessage(content: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val chatId = _currentChatId.value
                
                val userMessage = ChatMessage(
                    chatId = chatId ?: "",
                    content = content,
                    isUser = true,
                    timestamp = Timestamp.now(),
                    userId = userId
                )
                
                // If no current chat, create a new session
                val finalChatId = if (chatId == null) {
                    val newSession = chatRepository.createChatSession(userId, content)
                    _currentChatId.value = newSession.id
                    _chatSessions.value = listOf(newSession) + _chatSessions.value
                    newSession.id
                } else {
                    chatId
                }
                
                // Save user message
                val savedUserMessage = chatRepository.saveMessage(userId, userMessage.copy(chatId = finalChatId))
                _currentChatMessages.value = _currentChatMessages.value + savedUserMessage
                
                // Get AI response
                val aiResponse = chatRepository.sendToGemini(content)
                
                val aiMessage = ChatMessage(
                    chatId = finalChatId,
                    content = aiResponse,
                    isUser = false,
                    timestamp = Timestamp.now(),
                    userId = userId
                )
                
                // Save AI message
                val savedAiMessage = chatRepository.saveMessage(userId, aiMessage)
                _currentChatMessages.value = _currentChatMessages.value + savedAiMessage
                
                // Refresh chat sessions to update last message
                loadChatSessions(userId)
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun startNewChat() {
        _currentChatId.value = null
        _currentChatMessages.value = emptyList()
    }
    
    fun selectChat(chatId: String, userId: String) {
        loadChatMessages(chatId, userId)
    }
}
