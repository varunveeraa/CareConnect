package com.example.careconnect.repository

import com.example.careconnect.database.User
import com.example.careconnect.database.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    
    fun getLoggedInUser(): Flow<User?> = userDao.getLoggedInUser()
    
    suspend fun registerUser(user: User): Boolean {
        return try {
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun loginUser(email: String, password: String): Boolean {
        val user = userDao.getUserByCredentials(email, password)
        return if (user != null) {
            userDao.logoutAllUsers() // Logout any existing user
            userDao.loginUser(user.id)
            true
        } else {
            false
        }
    }
    
    suspend fun logoutUser(userId: Int) {
        userDao.logoutUser(userId)
    }
    
    suspend fun isUserLoggedIn(): Boolean {
        return userDao.isUserLoggedIn() > 0
    }
}
