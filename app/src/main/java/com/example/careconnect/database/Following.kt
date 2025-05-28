package com.example.careconnect.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "following")
data class Following(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val followerId: Int,
    val followingId: Int,
    val timestamp: Long = System.currentTimeMillis()
)