package com.example.careconnect.firestore

import com.google.firebase.firestore.PropertyName

data class SchedulingReminder(
    @PropertyName("title") val title: String = "",
    @PropertyName("startDate") val startDate: String = "",
    @PropertyName("endDate") val endDate: String = "",
    @PropertyName("reminderTime") val reminderTime: String = "",
    @PropertyName("type") val type: String = "",
    @PropertyName("hasAccountability") val hasAccountability: Boolean = false,
    @PropertyName("accountabilityPartners") val accountabilityPartners: List<String> = emptyList(),
    @PropertyName("id") val id: String = "",
    @PropertyName("createdAt") val createdAt: String = ""
) {
    constructor() : this(
        title = "",
        startDate = "",
        endDate = "",
        reminderTime = "",
        type = "",
        hasAccountability = false,
        accountabilityPartners = emptyList(),
        id = "",
        createdAt = ""
    )
}

data class FirestoreUser(
    @PropertyName("uid") val uid: String = "",
    @PropertyName("fullName") val fullName: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("profilePictureUrl") val profilePictureUrl: String? = null,
    @PropertyName("bio") val bio: String? = null,
    @PropertyName("healthConditions") val healthConditions: String? = null,
    @PropertyName("focusArea") val focusArea: String? = null,
    @PropertyName("followersCount") val followersCount: Int = 0,
    @PropertyName("followingCount") val followingCount: Int = 0,
    @PropertyName("isPrivate") val isPrivate: Boolean = false,
    @PropertyName("dateOfBirth") val dateOfBirth: String = "",
    @PropertyName("gender") val gender: String = "",
    @PropertyName("searchTerms") val searchTerms: List<String> = emptyList(), // For search optimization
    @PropertyName("reminders") val reminders: List<SchedulingReminder> = emptyList()
) {
    // No-argument constructor required for Firestore
    constructor() : this(
        uid = "",
        fullName = "",
        email = "",
        profilePictureUrl = null,
        bio = null,
        healthConditions = null,
        focusArea = null,
        followersCount = 0,
        followingCount = 0,
        isPrivate = false,
        dateOfBirth = "",
        gender = "",
        searchTerms = emptyList(),
        reminders = emptyList()
    )
}
