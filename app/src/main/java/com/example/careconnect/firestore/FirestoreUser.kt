package com.example.careconnect.firestore

import com.google.firebase.firestore.PropertyName

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
    @PropertyName("searchTerms") val searchTerms: List<String> = emptyList() // For search optimization
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
        searchTerms = emptyList()
    )
}