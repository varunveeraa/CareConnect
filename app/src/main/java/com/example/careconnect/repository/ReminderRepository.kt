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
     * Test Firestore connection and permissions
     */
    suspend fun testFirestoreConnection(): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Test: No authenticated user")
                return false
            }
            
            Log.d(TAG, "Test: Testing Firestore connection for user: ${currentUser.uid}")
            
            // Try to read a simple document
            val testDocRef = usersCollection.document(currentUser.uid)
            val testDoc = testDocRef.get().await()
            Log.d(TAG, "Test: Read operation successful. Document exists: ${testDoc.exists()}")
            
            // Try to write a simple test field
            testDocRef.set(
                mapOf("testField" to "testValue", "lastTest" to System.currentTimeMillis()),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            Log.d(TAG, "Test: Write operation successful")
            
            // Verify the write
            val verifyDoc = testDocRef.get().await()
            val testValue = verifyDoc.get("testField") as? String
            Log.d(TAG, "Test: Verification successful. Test value: $testValue")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Test: Firestore connection failed: ${e.message}", e)
            false
        }
    }

    /**
     * Add a new reminder to current user's profile
     */
    suspend fun addReminder(reminder: SchedulingReminder): Boolean {
        return try {
            val currentUser = auth.currentUser
            Log.d(TAG, "Current user: ${currentUser?.uid}")
            Log.d(TAG, "User email: ${currentUser?.email}")
            Log.d(TAG, "User display name: ${currentUser?.displayName}")
            
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user")
                return false
            }
            
            val reminderWithId = reminder.copy(
                id = UUID.randomUUID().toString(),
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            
            Log.d(TAG, "Adding reminder for user: ${currentUser.uid}")
            Log.d(TAG, "Reminder data: $reminderWithId")
            
            // Use a simpler approach - always use set with merge
            val userDocRef = usersCollection.document(currentUser.uid)
            Log.d(TAG, "User document path: ${userDocRef.path}")
            
            // Get current reminders first
            Log.d(TAG, "Fetching current document...")
            val currentDoc = userDocRef.get().await()
            Log.d(TAG, "Current document exists: ${currentDoc.exists()}")
            
            val currentReminders = if (currentDoc.exists()) {
                Log.d(TAG, "Document data: ${currentDoc.data}")
                val userData = currentDoc.toObject(FirestoreUser::class.java)
                Log.d(TAG, "Parsed user data: $userData")
                userData?.reminders ?: emptyList()
            } else {
                Log.d(TAG, "Document doesn't exist, starting with empty list")
                emptyList()
            }
            
            Log.d(TAG, "Current reminders count: ${currentReminders.size}")
            
            // Add new reminder to the list
            val updatedReminders = currentReminders + reminderWithId
            Log.d(TAG, "Updated reminders count: ${updatedReminders.size}")
            
            // Save back to Firestore
            Log.d(TAG, "Saving to Firestore...")
            userDocRef.set(
                mapOf("reminders" to updatedReminders),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            
            Log.d(TAG, "Save operation completed")
            
            // Verify the save
            Log.d(TAG, "Verifying save...")
            val verifyDoc = userDocRef.get().await()
            Log.d(TAG, "Verify document exists: ${verifyDoc.exists()}")
            
            if (verifyDoc.exists()) {
                val verifyUser = verifyDoc.toObject(FirestoreUser::class.java)
                val verifyCount = verifyUser?.reminders?.size ?: 0
                Log.d(TAG, "Verification: Document has ${verifyCount} reminders")
                
                if (verifyCount > currentReminders.size) {
                    Log.d(TAG, "SUCCESS: Reminder was added successfully!")
                    return true
                } else {
                    Log.e(TAG, "ERROR: Reminder count didn't increase")
                    return false
                }
            } else {
                Log.e(TAG, "ERROR: Verification document doesn't exist")
                return false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in addReminder: ${e.javaClass.simpleName}: ${e.message}", e)
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            false
        }
    }
    
    /**
     * Get all reminders for current user
     */
    suspend fun getCurrentUserReminders(): List<SchedulingReminder> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user")
                return emptyList()
            }
            
            Log.d(TAG, "Getting reminders for user: ${currentUser.uid}")
            
            val document = usersCollection.document(currentUser.uid).get().await()
            Log.d(TAG, "Document exists: ${document.exists()}")
            
            if (document.exists()) {
                val user = document.toObject(FirestoreUser::class.java)
                val reminders = user?.reminders ?: emptyList()
                Log.d(TAG, "Found ${reminders.size} reminders")
                
                // Log each reminder for debugging
                reminders.forEachIndexed { index, reminder ->
                    Log.d(TAG, "Reminder $index: ${reminder.title} - ${reminder.id}")
                }
                
                reminders
            } else {
                Log.d(TAG, "User document does not exist")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reminders: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Delete a reminder from current user's profile
     */
    suspend fun deleteReminder(reminderId: String): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user")
                return false
            }
            
            val reminders = getCurrentUserReminders()
            val reminderToDelete = reminders.find { it.id == reminderId }
            
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
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reminder", e)
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
