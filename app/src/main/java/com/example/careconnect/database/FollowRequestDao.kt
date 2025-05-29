package com.example.careconnect.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowRequestDao {
    @Query("SELECT * FROM follow_requests WHERE toUserId = :userId AND status = 'pending'")
    fun getPendingRequestsForUser(userId: Int): Flow<List<FollowRequest>>
    
    @Query("SELECT * FROM follow_requests WHERE fromUserId = :userId")
    fun getSentRequestsByUser(userId: Int): Flow<List<FollowRequest>>
    
    @Query("SELECT * FROM follow_requests WHERE fromUserId = :fromUserId AND toUserId = :toUserId")
    suspend fun getRequestBetweenUsers(fromUserId: Int, toUserId: Int): FollowRequest?
    
    @Insert
    suspend fun insertFollowRequest(followRequest: FollowRequest)
    
    @Query("UPDATE follow_requests SET status = :status WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Int, status: String)
    
    @Delete
    suspend fun deleteFollowRequest(followRequest: FollowRequest)
}