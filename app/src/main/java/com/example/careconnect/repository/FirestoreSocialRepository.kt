package com.example.careconnect.repository

import android.util.Log
import com.example.careconnect.firestore.FirestoreFollowRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import java.util.*

class FirestoreSocialRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val followRequestsCollection = firestore.collection("followRequests")
    private val usersCollection = firestore.collection("users")
    
    companion object {
        private const val TAG = "FirestoreSocialRepo"
    }
    
    /**
     * Get user details for a list of UIDs
     */
    suspend fun getUserDetailsByUids(uids: List<String>): List<Map<String, Any>> {
        return try {
            if (uids.isEmpty()) return emptyList()
            
            val users = mutableListOf<Map<String, Any>>()
            uids.forEach { uid ->
                val userDoc = usersCollection.document(uid).get().await()
                userDoc.data?.let { users.add(it) }
            }
            Log.d(TAG, "Retrieved details for ${users.size} users")
            users
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user details", e)
            emptyList()
        }
    }

    /**
     * Send a follow request in Firestore
     */
    suspend fun sendFollowRequest(
        fromUserUid: String,
        fromUserName: String,
        toUserUid: String,
        toUserName: String
    ): Boolean {
        return try {
            // Check if request already exists
            val existingRequest = getFollowRequestBetweenUsers(fromUserUid, toUserUid)
            if (existingRequest != null) {
                Log.d(TAG, "Follow request already exists")
                return false
            }
            
            val requestId = UUID.randomUUID().toString()
            val followRequest = FirestoreFollowRequest(
                id = requestId,
                fromUserUid = fromUserUid,
                fromUserName = fromUserName,
                toUserUid = toUserUid,
                toUserName = toUserName,
                status = "pending"
            )
            
            followRequestsCollection.document(requestId).set(followRequest).await()
            Log.d(TAG, "Follow request sent from $fromUserName to $toUserName")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending follow request", e)
            false
        }
    }
    
    /**
     * Get pending follow requests for a user
     */
    suspend fun getPendingRequestsForUser(userUid: String): List<FirestoreFollowRequest> {
        return try {
            Log.d(TAG, "=== QUERYING FOLLOW REQUESTS ===")
            Log.d(TAG, "Querying for toUserUid: '$userUid'")
            
            val result = followRequestsCollection
                .whereEqualTo("toUserUid", userUid)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            
            Log.d(TAG, "Query completed. Documents returned: ${result.documents.size}")
            val requests = result.toObjects(FirestoreFollowRequest::class.java)
            Log.d(TAG, "=== END QUERY DEBUG ===")
            requests
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending requests", e)
            emptyList()
        }
    }
    
    /**
     * Get sent requests by a user
     */
    suspend fun getSentRequestsByUser(userUid: String): List<FirestoreFollowRequest> {
        return try {
            val result = followRequestsCollection
                .whereEqualTo("fromUserUid", userUid)
                .get()
                .await()
            
            result.toObjects(FirestoreFollowRequest::class.java)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sent requests", e)
            emptyList()
        }
    }
    
    /**
     * Get follow request between two users
     */
    suspend fun getFollowRequestBetweenUsers(fromUserUid: String, toUserUid: String): FirestoreFollowRequest? {
        return try {
            val result = followRequestsCollection
                .whereEqualTo("fromUserUid", fromUserUid)
                .whereEqualTo("toUserUid", toUserUid)
                .get()
                .await()
            
            if (result.documents.isNotEmpty()) {
                result.toObjects(FirestoreFollowRequest::class.java).firstOrNull()
            } else {
                null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting request between users", e)
            null
        }
    }
    
    /**
     * Accept a follow request - Updates both user documents
     */
    suspend fun acceptFollowRequest(requestId: String): Boolean {
        return try {
            // Get the request first
            val requestDoc = followRequestsCollection.document(requestId).get().await()
            val request = requestDoc.toObject(FirestoreFollowRequest::class.java)
            
            if (request != null) {
                Log.d(TAG, "Accepting follow request: ${request.fromUserName} -> ${request.toUserName}")
                
                // Update request status
                followRequestsCollection.document(requestId)
                    .update("status", "accepted")
                    .await()
                
                // Update follower's following list (add toUserUid to fromUser's following)
                usersCollection.document(request.fromUserUid)
                    .update("following", FieldValue.arrayUnion(request.toUserUid))
                    .await()
                
                // Update followee's followers list (add fromUserUid to toUser's followers) 
                usersCollection.document(request.toUserUid)
                    .update("followers", FieldValue.arrayUnion(request.fromUserUid))
                    .await()
                
                Log.d(TAG, "Successfully updated user documents: ${request.fromUserName} now follows ${request.toUserName}")
                true
            } else {
                Log.e(TAG, "Follow request not found")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting follow request", e)
            false
        }
    }
    
    /**
     * Reject a follow request
     */
    suspend fun rejectFollowRequest(requestId: String): Boolean {
        return try {
            followRequestsCollection.document(requestId)
                .update("status", "rejected")
                .await()
            
            Log.d(TAG, "Follow request rejected")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting follow request", e)
            false
        }
    }
    
    /**
     * Get users that a user is following by reading their following array
     */
    suspend fun getFollowing(userUid: String): List<String> {
        return try {
            val userDoc = usersCollection.document(userUid).get().await()
            val following = userDoc.get("following") as? List<String> ?: emptyList()
            Log.d(TAG, "User $userUid is following: $following")
            following
        } catch (e: Exception) {
            Log.e(TAG, "Error getting following list", e)
            emptyList()
        }
    }
    
    /**
     * Get users following a user by reading their followers array
     */
    suspend fun getFollowers(userUid: String): List<String> {
        return try {
            val userDoc = usersCollection.document(userUid).get().await()
            val followers = userDoc.get("followers") as? List<String> ?: emptyList()
            Log.d(TAG, "User $userUid has followers: $followers")
            followers
        } catch (e: Exception) {
            Log.e(TAG, "Error getting followers list", e)
            emptyList()
        }
    }
    
    /**
     * Check if user A is following user B by checking A's following array
     */
    suspend fun isFollowing(followerUid: String, followingUid: String): Boolean {
        return try {
            val userDoc = usersCollection.document(followerUid).get().await()
            val following = userDoc.get("following") as? List<String> ?: emptyList()
            val isFollowing = following.contains(followingUid)
            Log.d(TAG, "Is $followerUid following $followingUid? $isFollowing")
            isFollowing
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if following", e)
            false
        }
    }
    
    /**
     * Unfollow a user - Removes from both user documents
     */
    suspend fun unfollowUser(followerUid: String, followingUid: String): Boolean {
        return try {
            // Remove from follower's following list
            usersCollection.document(followerUid)
                .update("following", FieldValue.arrayRemove(followingUid))
                .await()
            
            // Remove from followee's followers list
            usersCollection.document(followingUid)
                .update("followers", FieldValue.arrayRemove(followerUid))
                .await()
            
            Log.d(TAG, "Successfully unfollowed: $followerUid unfollowed $followingUid")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error unfollowing user", e)
            false
        }
    }
}
