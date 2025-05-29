package com.example.careconnect.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowingDao {
    @Query("SELECT u.* FROM users u INNER JOIN following f ON u.id = f.followingId WHERE f.followerId = :userId")
    fun getFollowing(userId: Int): Flow<List<User>>
    
    @Query("SELECT u.* FROM users u INNER JOIN following f ON u.id = f.followerId WHERE f.followingId = :userId")
    fun getFollowers(userId: Int): Flow<List<User>>
    
    @Query("SELECT * FROM following WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun getFollowingRelationship(followerId: Int, followingId: Int): Following?
    
    @Insert
    suspend fun insertFollowing(following: Following)
    
    @Query("DELETE FROM following WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteFollowing(followerId: Int, followingId: Int)
    
    @Query("SELECT COUNT(*) FROM following WHERE followerId = :userId")
    suspend fun getFollowingCount(userId: Int): Int
    
    @Query("SELECT COUNT(*) FROM following WHERE followingId = :userId")
    suspend fun getFollowersCount(userId: Int): Int
}