package com.example.careconnect.firestore

import com.google.firebase.firestore.PropertyName

data class ActualUser(
    @PropertyName("uid") val uid: String = "",
    @PropertyName("fullName") val fullName: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("dateOfBirth") val dateOfBirth: String = "",
    @PropertyName("gender") val gender: String = "",
    @PropertyName("focusAreas") val focusAreas: List<String> = emptyList(),
    @PropertyName("healthConditions") val healthConditions: List<String> = emptyList(),
    @PropertyName("onboardingCompleted") val onboardingCompleted: Boolean = false,
    @PropertyName("onboardingCompletedAt") val onboardingCompletedAt: Long = 0L,
    @PropertyName("createdAt") val createdAt: Long = 0L,
    @PropertyName("followers") val followers: List<String> = emptyList(), // Array of follower UIDs
    @PropertyName("following") val following: List<String> = emptyList()  // Array of following UIDs
) {
    // No-argument constructor required for Firestore
    constructor() : this(
        uid = "",
        fullName = "",
        email = "",
        dateOfBirth = "",
        gender = "",
        focusAreas = emptyList(),
        healthConditions = emptyList(),
        onboardingCompleted = false,
        onboardingCompletedAt = 0L,
        createdAt = 0L,
        followers = emptyList(),
        following = emptyList()
    )
}
