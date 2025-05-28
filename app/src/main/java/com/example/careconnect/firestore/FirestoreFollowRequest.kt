package com.example.careconnect.firestore

import com.google.firebase.firestore.PropertyName

data class FirestoreFollowRequest(
    @PropertyName("id") val id: String = "",
    @PropertyName("fromUserUid") val fromUserUid: String = "",
    @PropertyName("fromUserName") val fromUserName: String = "",
    @PropertyName("toUserUid") val toUserUid: String = "",
    @PropertyName("toUserName") val toUserName: String = "",
    @PropertyName("status") val status: String = "pending", // pending, accepted, rejected
    @PropertyName("timestamp") val timestamp: Long = System.currentTimeMillis()
) {
    // No-argument constructor required for Firestore
    constructor() : this(
        id = "",
        fromUserUid = "",
        fromUserName = "",
        toUserUid = "",
        toUserName = "",
        status = "pending",
        timestamp = System.currentTimeMillis()
    )
}
