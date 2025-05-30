package com.example.careconnect.repository

import android.util.Log
import com.example.careconnect.firestore.LocationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LocationRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun saveLocationData(latitude: Double, longitude: Double, accuracy: Float): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val locationData = LocationData(
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis(),
                    userId = currentUser.uid,
                    accuracy = accuracy
                )
                
                firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("locations")
                    .add(locationData)
                    .await()
                
                Log.d("LocationRepository", "Location saved successfully")
                true
            } else {
                Log.e("LocationRepository", "User not authenticated")
                false
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error saving location", e)
            false
        }
    }
    
    suspend fun getLatestLocationData(): LocationData? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val result = firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("locations")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()
                
                if (!result.isEmpty) {
                    result.documents[0].toObject(LocationData::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting location", e)
            null
        }
    }
}
