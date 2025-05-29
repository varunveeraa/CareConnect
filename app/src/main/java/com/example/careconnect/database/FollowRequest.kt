package com.example.careconnect.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "follow_requests")
data class FollowRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fromUserId: Int,
    val toUserId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending" // pending, accepted, rejected
)