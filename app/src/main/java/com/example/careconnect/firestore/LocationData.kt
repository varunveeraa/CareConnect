package com.example.careconnect.firestore

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",
    val accuracy: Float = 0f
)