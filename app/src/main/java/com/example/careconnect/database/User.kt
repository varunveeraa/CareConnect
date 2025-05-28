package com.example.careconnect.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val password: String, // In real app, this should be hashed
    val dateOfBirth: String,
    val gender: String,
    val isLoggedIn: Boolean = false,
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val healthConditions: String? = null, // JSON string of conditions
    val focusArea: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isPrivate: Boolean = false
)
