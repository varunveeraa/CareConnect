package com.example.careconnect.repository

import android.util.Log
import com.example.careconnect.firestore.SchedulingReminder
import com.example.careconnect.firestore.FirestoreUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ReminderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val auth = FirebaseAuth.getInstance()
    private val firestoreSocialRepository = com.example.careconnect.repository.FirestoreSocialRepository()
    
    companion object {
        private const val TAG = "ReminderRepository"
    }
    
    /**
     * Add a new reminder to current user's profile - SIMPLIFIED VERSION
     */
    suspend fun addReminder(reminder: SchedulingReminder): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            val reminderWithId = reminder.copy(
                id = UUID.randomUUID().toString(),
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            
            // Convert to simple map for Firestore
            val reminderMap = mapOf(
                "id" to reminderWithId.id,
                "title" to reminderWithId.title,
                "startDate" to reminderWithId.startDate,
                "endDate" to reminderWithId.endDate,
                "reminderTime" to reminderWithId.reminderTime,
                "type" to reminderWithId.type,
                "hasAccountability" to reminderWithId.hasAccountability,
                "accountabilityPartners" to reminderWithId.accountabilityPartners,
                "createdAt" to reminderWithId.createdAt
            )
            
            val userDocRef = usersCollection.document(currentUser.uid)
            
            // Use FieldValue.arrayUnion for simple append
            userDocRef.update("reminders", FieldValue.arrayUnion(reminderMap)).await()
            
            Log.d(TAG, "Reminder added successfully")
            true
        } catch (e: Exception) {
            // If update fails (document doesn't exist), create it
            try {
                val currentUser = auth.currentUser ?: return false
                val reminderWithId = reminder.copy(
                    id = UUID.randomUUID().toString(),
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )
                
                val reminderMap = mapOf(
                    "id" to reminderWithId.id,
                    "title" to reminderWithId.title,
                    "startDate" to reminderWithId.startDate,
                    "endDate" to reminderWithId.endDate,
                    "reminderTime" to reminderWithId.reminderTime,
                    "type" to reminderWithId.type,
                    "hasAccountability" to reminderWithId.hasAccountability,
                    "accountabilityPartners" to reminderWithId.accountabilityPartners,
                    "createdAt" to reminderWithId.createdAt
                )
                
                usersCollection.document(currentUser.uid)
                    .set(mapOf("reminders" to listOf(reminderMap)), com.google.firebase.firestore.SetOptions.merge())
                    .await()
                
                Log.d(TAG, "Reminder added to new document")
                true
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to add reminder: ${e2.message}")
                false
            }
        }
    }
    
    /**
     * Get all reminders for current user - SIMPLIFIED VERSION
     */
    suspend fun getCurrentUserReminders(): List<SchedulingReminder> {
        return try {
            val currentUser = auth.currentUser ?: return emptyList()
            
            val document = usersCollection.document(currentUser.uid).get().await()
            
            if (document.exists()) {
                val remindersData = document.get("reminders") as? List<Map<String, Any>> ?: emptyList()
                
                // Convert maps back to SchedulingReminder objects
                remindersData.mapNotNull { reminderMap ->
                    try {
                        SchedulingReminder(
                            id = reminderMap["id"] as? String ?: "",
                            title = reminderMap["title"] as? String ?: "",
                            startDate = reminderMap["startDate"] as? String ?: "",
                            endDate = reminderMap["endDate"] as? String ?: "",
                            reminderTime = reminderMap["reminderTime"] as? String ?: "",
                            type = reminderMap["type"] as? String ?: "",
                            hasAccountability = reminderMap["hasAccountability"] as? Boolean ?: false,
                            accountabilityPartners = (reminderMap["accountabilityPartners"] as? List<String>) ?: emptyList(),
                            createdAt = reminderMap["createdAt"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing reminder: $reminderMap", e)
                        null
                    }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reminders: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Delete a reminder from current user's profile - SIMPLIFIED VERSION
     */
    suspend fun deleteReminder(reminderId: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            val document = usersCollection.document(currentUser.uid).get().await()
            if (document.exists()) {
                val remindersData = document.get("reminders") as? List<Map<String, Any>> ?: emptyList()
                val reminderToDelete = remindersData.find { it["id"] == reminderId }
                
                if (reminderToDelete != null) {
                    usersCollection.document(currentUser.uid)
                        .update("reminders", FieldValue.arrayRemove(reminderToDelete))
                        .await()
                    
                    Log.d(TAG, "Reminder deleted successfully")
                    true
                } else {
                    Log.e(TAG, "Reminder not found")
                    false
                }
            } else {
                Log.e(TAG, "User document not found")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reminder: ${e.message}")
            false
        }
    }
    
    /**
     * Get followers of current user (for accountability partner selection)
     */
    suspend fun getCurrentUserFollowers(): List<Map<String, Any>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user")
                return emptyList()
            }
            
            // Use the Firestore social repository to get followers
            val followerUids = firestoreSocialRepository.getFollowers(currentUser.uid)
            val followers = firestoreSocialRepository.getUserDetailsByUids(followerUids)
            
            Log.d(TAG, "Found ${followers.size} followers")
            followers
        } catch (e: Exception) {
            Log.e(TAG, "Error getting followers", e)
            emptyList()
        }
    }
}
