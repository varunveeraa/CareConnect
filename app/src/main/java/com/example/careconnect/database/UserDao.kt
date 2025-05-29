package com.example.careconnect.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun getUserByCredentials(email: String, password: String): User?
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<User?>
    
    @Query("UPDATE users SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun loginUser(userId: Int)
    
    @Query("UPDATE users SET isLoggedIn = 0 WHERE id = :userId")
    suspend fun logoutUser(userId: Int)
    
    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()
    
    @Query("SELECT COUNT(*) FROM users WHERE isLoggedIn = 1")
    suspend fun isUserLoggedIn(): Int
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?
    
    @Query("SELECT * FROM users WHERE fullName LIKE '%' || :searchQuery || '%' OR email LIKE '%' || :searchQuery || '%'")
    suspend fun searchUsers(searchQuery: String): List<User>
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("UPDATE users SET followersCount = :count WHERE id = :userId")
    suspend fun updateFollowersCount(userId: Int, count: Int)
    
    @Query("UPDATE users SET followingCount = :count WHERE id = :userId")
    suspend fun updateFollowingCount(userId: Int, count: Int)
}
