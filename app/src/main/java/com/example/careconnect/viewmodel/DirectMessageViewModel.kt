package com.example.careconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.firestore.DirectMessage
import com.example.careconnect.firestore.DirectChatMessage
import com.example.careconnect.repository.DirectMessageRepository
import com.example.careconnect.repository.FirestoreUserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

class DirectMessageViewModel : ViewModel() {
    private val repository = DirectMessageRepository()
    private val userRepository = FirestoreUserRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _conversations = MutableStateFlow<List<DirectMessage>>(emptyList())
    val conversations: StateFlow<List<DirectMessage>> = _conversations.asStateFlow()
    
    private val _currentConversation = MutableStateFlow<DirectMessage?>(null)
    val currentConversation: StateFlow<DirectMessage?> = _currentConversation.asStateFlow()
    
    private val _currentMessages = MutableStateFlow<List<DirectChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<DirectChatMessage>> = _currentMessages.asStateFlow()
    
    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    companion object {
        private const val TAG = "DirectMessageViewModel"
    }
    
    init {
        // Load conversations when ViewModel is created
        auth.currentUser?.uid?.let { userId ->
            Log.d(TAG, "Initializing DirectMessageViewModel for user: $userId")
            loadConversations(userId)
            observeConversations(userId)
            updateTotalUnreadCount(userId)
        } ?: run {
            Log.w(TAG, "No current user found during DirectMessageViewModel initialization")
        }
    }
    
    // Ensure proper initialization - call this when app starts or user changes
    fun ensureInitialized() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            Log.d(TAG, "Ensuring DirectMessageViewModel is initialized for user: $currentUserId")
            if (_conversations.value.isEmpty()) {
                Log.d(TAG, "Conversations list is empty, loading conversations")
                loadConversations(currentUserId)
                observeConversations(currentUserId)
                updateTotalUnreadCount(currentUserId)
            } else {
                Log.d(TAG, "Conversations already loaded: ${_conversations.value.size} conversations")
            }
        } else {
            Log.w(TAG, "Cannot initialize: no current user")
        }
    }
    
    // Load conversations for current user
    private fun loadConversations(userId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading conversations for user: $userId")
                _isLoading.value = true
                _error.value = null
                
                val conversations = repository.getConversations(userId)
                Log.d(TAG, "Loaded ${conversations.size} conversations")
                
                conversations.forEachIndexed { index, conv ->
                    Log.d(TAG, "Conversation $index: ${conv.id} with ${conv.participantIds} - lastMessage: '${conv.lastMessage}'")
                }
                
                _conversations.value = conversations
                updateTotalUnreadCount(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading conversations", e)
                handleError("Failed to load conversations: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Observe real-time conversation updates
    private fun observeConversations(userId: String) {
        viewModelScope.launch {
            repository.getConversationsFlow(userId).collect { conversations ->
                _conversations.value = conversations
                updateTotalUnreadCount(userId)
            }
        }
    }
    
    // Test Firestore connectivity
    fun testConnection() {
        viewModelScope.launch {
            try {
                val result = repository.testFirestoreConnection()
                Log.d(TAG, "Connection test result: $result")
                // You could expose this as a state if needed
            } catch (e: Exception) {
                Log.e(TAG, "Connection test error", e)
            }
        }
    }
    
    // Create or get conversation with another user and provide callback
    fun startConversationWith(
        otherUserId: String, 
        otherUserName: String,
        onConversationReady: ((DirectMessage) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting conversation with: $otherUserId")
                _isLoading.value = true
                _error.value = null
                
                val currentUser = auth.currentUser ?: run {
                    handleError("User not authenticated")
                    return@launch
                }
                
                // Get current user's name
                val currentUserData = try {
                    userRepository.getUserByUid(currentUser.uid)
                } catch (e: Exception) {
                    null
                }
                
                val currentUserName = currentUserData?.fullName
                    ?: currentUser.displayName
                    ?: currentUser.email?.substringBefore("@")
                    ?: "Unknown User"
                
                // Validate inputs
                if (otherUserId.isEmpty() || otherUserName.isEmpty()) {
                    handleError("Invalid user information")
                    return@launch
                }
                
                if (currentUser.uid == otherUserId) {
                    handleError("Cannot start conversation with yourself")
                    return@launch
                }
                
                val conversation = repository.createOrGetConversation(
                    currentUser.uid,
                    otherUserId,
                    currentUserName,
                    otherUserName
                )
                
                // Immediately add to conversations list if it's new
                val currentConversations = _conversations.value.toMutableList()
                if (!currentConversations.any { it.id == conversation.id }) {
                    currentConversations.add(0, conversation) // Add at beginning (most recent)
                    _conversations.value = currentConversations
                }
                
                selectConversation(conversation)
                Log.d(TAG, "Conversation created/selected: ${conversation.id}")
                
                // Call the callback with the conversation
                onConversationReady?.invoke(conversation)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error starting conversation", e)
                handleError("Failed to start conversation: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Select a conversation to view
    fun selectConversation(conversation: DirectMessage) {
        Log.d(TAG, "=== SELECTING CONVERSATION ===")
        Log.d(TAG, "Conversation ID: ${conversation.id}")
        Log.d(TAG, "Participants: ${conversation.participantIds}")
        Log.d(TAG, "Last message: '${conversation.lastMessage}'")
        
        _currentConversation.value = conversation
        
        // Clear previous messages first
        _currentMessages.value = emptyList()
        
        // Start loading messages
        loadMessages(conversation.id)
        
        // Mark messages as read when opening conversation
        auth.currentUser?.uid?.let { userId ->
            Log.d(TAG, "Marking messages as read for user: $userId")
            viewModelScope.launch {
                repository.markMessagesAsRead(conversation.id, userId)
                repository.markMessagesAsDelivered(conversation.id, userId)
                // Refresh conversations to update unread counts
                loadConversations(userId)
            }
        }
        
        Log.d(TAG, "=== CONVERSATION SELECTED ===")
    }
    
    // Load messages for current conversation
    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading messages for conversation: $conversationId")
                repository.getMessagesFlow(conversationId).collect { messages ->
                    Log.d(TAG, "Received ${messages.size} messages for conversation $conversationId")
                    messages.forEachIndexed { index, msg ->
                        Log.d(TAG, "Message $index: ${msg.id} from ${msg.senderId} - '${msg.content.take(50)}...'")
                    }
                    _currentMessages.value = messages
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading messages", e)
                handleError("Failed to load messages: ${e.message}")
            }
        }
    }
    
    // Send a message in current conversation
    fun sendMessage(content: String) {
        val currentConversation = _currentConversation.value ?: run {
            handleError("No conversation selected")
            return
        }
        
        val currentUser = auth.currentUser ?: run {
            handleError("User not authenticated")
            return
        }
        
        if (content.isBlank()) {
            handleError("Message cannot be empty")
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending message: '${content.take(50)}...' in conversation: ${currentConversation.id}")
                _isLoading.value = true
                
                val otherUserId = currentConversation.getOtherParticipant(currentUser.uid) ?: run {
                    handleError("Cannot find conversation participant")
                    return@launch
                }
                
                Log.d(TAG, "Sending from ${currentUser.uid} to $otherUserId in conversation ${currentConversation.id}")
                
                val sentMessage = repository.sendMessage(
                    conversationId = currentConversation.id,
                    senderId = currentUser.uid,
                    receiverId = otherUserId,
                    content = content.trim()
                )
                
                Log.d(TAG, "Message sent successfully: ${sentMessage.id}")
                
                // Refresh conversations to update last message
                loadConversations(currentUser.uid)
                
                Log.d(TAG, "Conversations refreshed after sending message")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                handleError("Failed to send message: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Update total unread count
    private fun updateTotalUnreadCount(userId: String) {
        viewModelScope.launch {
            try {
                val count = repository.getTotalUnreadCount(userId)
                _totalUnreadCount.value = count
            } catch (e: Exception) {
                Log.e(TAG, "Error updating unread count", e)
            }
        }
    }
    
    // Refresh conversations
    fun refreshConversations() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            Log.d(TAG, "Refreshing conversations for user: $currentUserId")
            loadConversations(currentUserId)
        } else {
            Log.w(TAG, "Cannot refresh conversations: no current user")
            handleError("User not authenticated")
        }
    }
    
    // Clear current conversation
    fun clearCurrentConversation() {
        _currentConversation.value = null
        _currentMessages.value = emptyList()
    }
    
    // Delete conversation
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteConversation(conversationId)
                
                // Refresh conversations
                auth.currentUser?.uid?.let { userId ->
                    loadConversations(userId)
                }
                
                // Clear current conversation if it was deleted
                if (_currentConversation.value?.id == conversationId) {
                    clearCurrentConversation()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting conversation", e)
                handleError("Failed to delete conversation: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Get conversation by ID with refresh if not found
    suspend fun getConversationByIdWithRefresh(conversationId: String): DirectMessage? {
        // First try to get from current conversations
        var conversation = getConversationById(conversationId)
        
        if (conversation == null) {
            // If not found, refresh conversations and try again
            auth.currentUser?.uid?.let { userId ->
                loadConversations(userId)
                conversation = getConversationById(conversationId)
            }
        }
        
        return conversation
    }
    
    // Get conversation by ID
    fun getConversationById(conversationId: String): DirectMessage? {
        return _conversations.value.find { it.id == conversationId }
    }
    
    // Handle errors
    private fun handleError(message: String) {
        _error.value = message
        Log.e(TAG, message)
    }
    
    // Clear error
    fun clearError() {
        _error.value = null
    }
}
