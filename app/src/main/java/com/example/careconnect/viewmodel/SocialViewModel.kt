package com.example.careconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.database.FollowRequest
import com.example.careconnect.database.User
import com.example.careconnect.firestore.FirestoreUser
import com.example.careconnect.firestore.ActualUser
import com.example.careconnect.firestore.FirestoreFollowRequest
import com.example.careconnect.repository.SocialRepository
import com.example.careconnect.repository.FirestoreUserRepository
import com.example.careconnect.repository.FirestoreSocialRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SocialViewModel(
    private val socialRepository: SocialRepository,
    private val firestoreUserRepository: FirestoreUserRepository = FirestoreUserRepository(),
    private val firestoreSocialRepository: FirestoreSocialRepository = FirestoreSocialRepository()
) : ViewModel() {
    
    private val _searchResults = MutableStateFlow<List<Any>>(emptyList())
    val searchResults: StateFlow<List<Any>> = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()
    
    private val _selectedFirestoreUser = MutableStateFlow<FirestoreUser?>(null)
    val selectedFirestoreUser: StateFlow<FirestoreUser?> = _selectedFirestoreUser.asStateFlow()
    
    private val _selectedActualUser = MutableStateFlow<ActualUser?>(null)
    val selectedActualUser: StateFlow<ActualUser?> = _selectedActualUser.asStateFlow()
    
    // Firestore social features
    private val _firestorePendingRequests = MutableStateFlow<List<FirestoreFollowRequest>>(emptyList())
    val firestorePendingRequests: StateFlow<List<FirestoreFollowRequest>> = _firestorePendingRequests.asStateFlow()
    
    private val _firestoreFollowerUids = MutableStateFlow<List<String>>(emptyList())
    val firestoreFollowerUids: StateFlow<List<String>> = _firestoreFollowerUids.asStateFlow()
    
    private val _firestoreFollowers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val firestoreFollowers: StateFlow<List<Map<String, Any>>> = _firestoreFollowers.asStateFlow()

    private val _firestoreFollowingUids = MutableStateFlow<List<String>>(emptyList())
    val firestoreFollowingUids: StateFlow<List<String>> = _firestoreFollowingUids.asStateFlow()
    
    private val _firestoreFollowingUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val firestoreFollowingUsers: StateFlow<List<Map<String, Any>>> = _firestoreFollowingUsers.asStateFlow()
    
    /**
     * Search users in Firestore by fullName only
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("SocialViewModel", "Searching for: '$query'")
                
                // Use simple fullName-only search
                val rawResults = firestoreUserRepository.searchByFullNameOnly(query)
                android.util.Log.d("SocialViewModel", "Raw search returned ${rawResults.size} results")
                
                // Convert raw data to display objects
                val displayResults = rawResults.map { data ->
                    RawFirestoreUser(
                        uid = data["uid"] as? String ?: "",
                        fullName = data["fullName"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        focusAreas = data["focusAreas"] as? List<String> ?: emptyList(),
                        healthConditions = data["healthConditions"] as? List<String> ?: emptyList(),
                        dateOfBirth = data["dateOfBirth"] as? String ?: "",
                        gender = data["gender"] as? String ?: "",
                        onboardingCompleted = data["onboardingCompleted"] as? Boolean ?: false
                    )
                }
                
                _searchResults.value = displayResults
                android.util.Log.d("SocialViewModel", "Final results: ${displayResults.size}")
                displayResults.forEach { user ->
                    android.util.Log.d("SocialViewModel", "Result: '${user.fullName}' (${user.uid})")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Search error: ${e.message}", e)
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get ActualUser by UID
     */
    fun getActualUserByUid(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // For now, we'll implement this by searching all users
                val allUsers = firestoreUserRepository.searchRealUsers("")
                val user = allUsers.find { it.uid == uid }
                _selectedActualUser.value = user
            } catch (e: Exception) {
                _selectedActualUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get Firestore user by UID
     */
    fun getFirestoreUserByUid(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = firestoreUserRepository.getUserByUid(uid)
                _selectedFirestoreUser.value = user
            } catch (e: Exception) {
                _selectedFirestoreUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Save a user to Firestore
     */
    fun saveUserToFirestore(firestoreUser: FirestoreUser) {
        viewModelScope.launch {
            try {
                firestoreUserRepository.saveUser(firestoreUser)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Keep existing local database methods for social features
    fun getUserById(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = socialRepository.getUserById(userId)
                _selectedUser.value = user
            } catch (e: Exception) {
                _selectedUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendFollowRequest(fromUserId: Int, toUserId: Int) {
        viewModelScope.launch {
            try {
                socialRepository.sendFollowRequest(fromUserId, toUserId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun acceptFollowRequest(request: FollowRequest) {
        viewModelScope.launch {
            try {
                socialRepository.acceptFollowRequest(request.id, request.fromUserId, request.toUserId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun rejectFollowRequest(requestId: Int) {
        viewModelScope.launch {
            try {
                socialRepository.rejectFollowRequest(requestId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun getPendingRequests(userId: Int): Flow<List<FollowRequest>> =
        socialRepository.getPendingRequestsForUser(userId)
    
    fun getFollowing(userId: Int): Flow<List<User>> =
        socialRepository.getFollowing(userId)
    
    fun getFollowers(userId: Int): Flow<List<User>> =
        socialRepository.getFollowers(userId)
    
    suspend fun isFollowing(followerId: Int, followingId: Int): Boolean =
        socialRepository.isFollowing(followerId, followingId)
    
    suspend fun getRequestBetweenUsers(fromUserId: Int, toUserId: Int): FollowRequest? =
        socialRepository.getRequestBetweenUsers(fromUserId, toUserId)
    
    fun unfollowUser(followerId: Int, followingId: Int) {
        viewModelScope.launch {
            try {
                socialRepository.unfollowUser(followerId, followingId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Send follow request to a Firestore user
     * Maps Firestore user to local user system for social features
     */
    fun sendFollowRequestToFirestoreUser(fromUserId: Int, firestoreUser: RawFirestoreUser) {
        viewModelScope.launch {
            try {
                // For now, we'll use a simple mapping: Firestore UID hash to local user ID
                val toUserId = firestoreUser.uid.hashCode().let { if (it < 0) -it else it } % 10000
                
                android.util.Log.d("SocialViewModel", "Sending follow request from user $fromUserId to Firestore user ${firestoreUser.fullName} (mapped to local ID: $toUserId)")
                
                // Create local user record for the Firestore user if it doesn't exist
                val localUser = socialRepository.getUserById(toUserId)
                if (localUser == null) {
                    android.util.Log.d("SocialViewModel", "Creating local user record for Firestore user")
                    socialRepository.createLocalUserForFirestoreUser(
                        localUserId = toUserId,
                        fullName = firestoreUser.fullName,
                        email = firestoreUser.email,
                        focusAreas = firestoreUser.focusAreas,
                        healthConditions = firestoreUser.healthConditions
                    )
                }
                
                // Send the follow request using existing social system
                socialRepository.sendFollowRequest(fromUserId, toUserId)
                
                android.util.Log.d("SocialViewModel", "Follow request sent successfully")
                
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Error sending follow request to Firestore user", e)
            }
        }
    }
    
    /**
     * Check if current user has sent a follow request to a Firestore user
     */
    suspend fun hasRequestToFirestoreUser(fromUserId: Int, firestoreUser: RawFirestoreUser): FollowRequest? {
        return try {
            val toUserId = firestoreUser.uid.hashCode().let { if (it < 0) -it else it } % 10000
            socialRepository.getRequestBetweenUsers(fromUserId, toUserId)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if current user is following a Firestore user
     */
    suspend fun isFollowingFirestoreUser(fromUserId: Int, firestoreUser: RawFirestoreUser): Boolean {
        return try {
            val toUserId = firestoreUser.uid.hashCode().let { if (it < 0) -it else it } % 10000
            socialRepository.isFollowing(fromUserId, toUserId)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Debug method to check if users exist in Firestore
     */
    fun debugCheckFirestoreUsers() {
        viewModelScope.launch {
            try {
                android.util.Log.d("SocialViewModel", "=== DEBUG: CHECKING FIRESTORE ===")
                
                // Use the same search method but with empty query to get all users
                val allRawData = firestoreUserRepository.searchByFullNameOnly("")
                android.util.Log.d("SocialViewModel", "Found ${allRawData.size} total documents")
                
                allRawData.forEach { data ->
                    val fullName = data["fullName"] as? String
                    val uid = data["uid"] as? String
                    android.util.Log.d("SocialViewModel", "Document: fullName='$fullName', uid='$uid'")
                }
                
                // Test search for "Gold"
                android.util.Log.d("SocialViewModel", "=== TESTING SEARCH FOR 'Gold' ===")
                val goldSearch = firestoreUserRepository.searchByFullNameOnly("Gold")
                android.util.Log.d("SocialViewModel", "Search for 'Gold' returned ${goldSearch.size} results")
                
                goldSearch.forEach { data ->
                    val fullName = data["fullName"] as? String
                    android.util.Log.d("SocialViewModel", "Gold search result: '$fullName'")
                }
                
                android.util.Log.d("SocialViewModel", "=== END DEBUG ===")
                
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Debug: Error checking Firestore users", e)
            }
        }
    }
    
    /**
     * Send follow request to a Firestore user - FULLY IN FIRESTORE
     */
    fun sendFollowRequestToFirestoreUser(
        fromUserUid: String,
        fromUserName: String, 
        firestoreUser: RawFirestoreUser
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("SocialViewModel", "Sending Firestore follow request from $fromUserName to ${firestoreUser.fullName}")
                
                val success = firestoreSocialRepository.sendFollowRequest(
                    fromUserUid = fromUserUid,
                    fromUserName = fromUserName,
                    toUserUid = firestoreUser.uid,
                    toUserName = firestoreUser.fullName
                )
                
                if (success) {
                    android.util.Log.d("SocialViewModel", "Firestore follow request sent successfully")
                    // Refresh pending requests
                    loadFirestorePendingRequests(fromUserUid)
                } else {
                    android.util.Log.e("SocialViewModel", "Failed to send Firestore follow request")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Error sending Firestore follow request", e)
            }
        }
    }
    
    /**
     * Load pending follow requests from Firestore for a user
     */
    fun loadFirestorePendingRequests(userUid: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("SocialViewModel", "=== LOADING PENDING REQUESTS ===")
                android.util.Log.d("SocialViewModel", "Loading requests for UID: '$userUid'")
                
                val requests = firestoreSocialRepository.getPendingRequestsForUser(userUid)
                
                android.util.Log.d("SocialViewModel", "Repository returned ${requests.size} requests")
                android.util.Log.d("SocialViewModel", "Setting _firestorePendingRequests.value...")
                
                _firestorePendingRequests.value = requests
                
                android.util.Log.d("SocialViewModel", "StateFlow updated. Current value: ${_firestorePendingRequests.value.size}")
                android.util.Log.d("SocialViewModel", "=== END LOADING ===")
                
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Error loading Firestore pending requests", e)
                _firestorePendingRequests.value = emptyList()
            }
        }
    }
    
    /**
     * Load following list from Firestore for a user
     */
    fun loadFirestoreFollowing(userUid: String) {
        viewModelScope.launch {
            try {
                val followingUids = firestoreSocialRepository.getFollowing(userUid)
                _firestoreFollowingUids.value = followingUids
                android.util.Log.d("SocialViewModel", "Loaded ${followingUids.size} Firestore following UIDs")
                
                // Get user data for each following UID using the repository method
                val followingUsers = firestoreSocialRepository.getUserDetailsByUids(followingUids)
                _firestoreFollowingUsers.value = followingUsers
                android.util.Log.d("SocialViewModel", "Loaded ${followingUsers.size} Firestore following users")
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Error loading Firestore following", e)
            }
        }
    }
    
    /**
     * Load followers list from Firestore for a user
     */
    fun loadFirestoreFollowers(userUid: String) {
        viewModelScope.launch {
            try {
                val followerUids = firestoreSocialRepository.getFollowers(userUid)
                _firestoreFollowerUids.value = followerUids
                android.util.Log.d("SocialViewModel", "Loaded ${followerUids.size} Firestore follower UIDs")
                
                // Get user data for each follower UID using the repository method
                val followers = firestoreSocialRepository.getUserDetailsByUids(followerUids)
                _firestoreFollowers.value = followers
                android.util.Log.d("SocialViewModel", "Loaded ${followers.size} Firestore followers")
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Error loading Firestore followers", e)
            }
        }
    }
    
    /**
     * Accept a Firestore follow request
     */
    fun acceptFirestoreFollowRequest(request: FirestoreFollowRequest) {
        viewModelScope.launch {
            try {
                val success = firestoreSocialRepository.acceptFollowRequest(request.id)
                if (success) {
                    android.util.Log.d("SocialViewModel", "Firestore follow request accepted")
                    // Refresh pending requests
                    loadFirestorePendingRequests(request.toUserUid)
                }
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Error accepting Firestore follow request", e)
            }
        }
    }
    
    /**
     * Reject a Firestore follow request
     */
    fun rejectFirestoreFollowRequest(request: FirestoreFollowRequest) {
        viewModelScope.launch {
            try {
                val success = firestoreSocialRepository.rejectFollowRequest(request.id)
                if (success) {
                    android.util.Log.d("SocialViewModel", "Firestore follow request rejected")
                    // Refresh pending requests
                    loadFirestorePendingRequests(request.toUserUid)
                }
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Error rejecting Firestore follow request", e)
            }
        }
    }
    
    /**
     * Check if user is following another user in Firestore
     */
    suspend fun isFollowingInFirestore(followerUid: String, followingUid: String): Boolean {
        return try {
            firestoreSocialRepository.isFollowing(followerUid, followingUid)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if there's a pending request between users in Firestore
     */
    suspend fun getFirestoreRequestBetweenUsers(fromUserUid: String, toUserUid: String): FirestoreFollowRequest? {
        return try {
            firestoreSocialRepository.getFollowRequestBetweenUsers(fromUserUid, toUserUid)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Unfollow a user in Firestore
     */
    fun unfollowInFirestore(followerUid: String, followingUid: String) {
        viewModelScope.launch {
            try {
                val success = firestoreSocialRepository.unfollowUser(followerUid, followingUid)
                if (success) {
                    android.util.Log.d("SocialViewModel", "User unfollowed in Firestore")
                    // Refresh following list
                    loadFirestoreFollowing(followerUid)
                }
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "Error unfollowing in Firestore", e)
            }
        }
    }
    
    // Simple data class for raw Firestore data
    data class RawFirestoreUser(
        val uid: String,
        val fullName: String,
        val email: String,
        val focusAreas: List<String>,
        val healthConditions: List<String>,
        val dateOfBirth: String,
        val gender: String,
        val onboardingCompleted: Boolean
    )
}
