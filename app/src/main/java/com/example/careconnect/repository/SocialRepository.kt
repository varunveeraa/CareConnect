package com.example.careconnect.repository

import com.example.careconnect.database.*
import kotlinx.coroutines.flow.Flow

class SocialRepository(
    private val userDao: UserDao,
    private val followRequestDao: FollowRequestDao,
    private val followingDao: FollowingDao
) {
    
    // User search and profile
    suspend fun searchUsers(query: String): List<User> = userDao.searchUsers(query)
    suspend fun getUserById(userId: Int): User? = userDao.getUserById(userId)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    
    /**
     * Create a local user record for a Firestore user to enable social features
     */
    suspend fun createLocalUserForFirestoreUser(
        localUserId: Int,
        fullName: String,
        email: String,
        focusAreas: List<String>,
        healthConditions: List<String>
    ): User {
        val localUser = User(
            id = localUserId,
            fullName = fullName,
            email = email,
            password = "", // Not needed for Firestore users
            dateOfBirth = "",
            gender = "",
            isLoggedIn = false,
            focusArea = focusAreas.joinToString(", "),
            healthConditions = healthConditions.joinToString(", ")
        )
        
        // Insert the user if it doesn't exist
        val existingUser = userDao.getUserById(localUserId)
        if (existingUser == null) {
            userDao.insertUser(localUser)
        }
        
        return localUser
    }
    
    // Follow requests
    suspend fun sendFollowRequest(fromUserId: Int, toUserId: Int) {
        val existingRequest = followRequestDao.getRequestBetweenUsers(fromUserId, toUserId)
        if (existingRequest == null) {
            followRequestDao.insertFollowRequest(
                FollowRequest(fromUserId = fromUserId, toUserId = toUserId)
            )
        }
    }
    
    suspend fun acceptFollowRequest(requestId: Int, fromUserId: Int, toUserId: Int) {
        followRequestDao.updateRequestStatus(requestId, "accepted")
        followingDao.insertFollowing(Following(followerId = fromUserId, followingId = toUserId))
        
        // Update follower/following counts
        val fromUserFollowingCount = followingDao.getFollowingCount(fromUserId)
        val toUserFollowersCount = followingDao.getFollowersCount(toUserId)
        userDao.updateFollowingCount(fromUserId, fromUserFollowingCount)
        userDao.updateFollowersCount(toUserId, toUserFollowersCount)
    }
    
    suspend fun rejectFollowRequest(requestId: Int) {
        followRequestDao.updateRequestStatus(requestId, "rejected")
    }
    
    suspend fun getRequestBetweenUsers(fromUserId: Int, toUserId: Int): FollowRequest? =
        followRequestDao.getRequestBetweenUsers(fromUserId, toUserId)
    
    fun getPendingRequestsForUser(userId: Int): Flow<List<FollowRequest>> =
        followRequestDao.getPendingRequestsForUser(userId)
    
    // Following relationships
    fun getFollowing(userId: Int): Flow<List<User>> = followingDao.getFollowing(userId)
    fun getFollowers(userId: Int): Flow<List<User>> = followingDao.getFollowers(userId)
    
    suspend fun unfollowUser(followerId: Int, followingId: Int) {
        followingDao.deleteFollowing(followerId, followingId)
        
        // Update follower/following counts
        val followerFollowingCount = followingDao.getFollowingCount(followerId)
        val followingFollowersCount = followingDao.getFollowersCount(followingId)
        userDao.updateFollowingCount(followerId, followerFollowingCount)
        userDao.updateFollowersCount(followingId, followingFollowersCount)
    }
    
    suspend fun isFollowing(followerId: Int, followingId: Int): Boolean =
        followingDao.getFollowingRelationship(followerId, followingId) != null
}
