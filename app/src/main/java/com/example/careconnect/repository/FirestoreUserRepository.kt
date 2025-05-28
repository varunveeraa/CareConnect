package com.example.careconnect.repository

import android.util.Log
import com.example.careconnect.firestore.FirestoreUser
import com.example.careconnect.firestore.ActualUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FirestoreUserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    companion object {
        private const val TAG = "FirestoreUserRepository"
    }
    
    /**
     * Search for real users by fullName - works with existing database structure
     */
    suspend fun searchRealUsers(searchQuery: String): List<ActualUser> {
        if (searchQuery.isBlank()) return emptyList()
        
        return try {
            Log.d(TAG, "Searching real users for: '$searchQuery'")
            
            // Get all users from the collection
            val result = usersCollection.get().await()
            
            // Convert to ActualUser objects
            val users = result.toObjects(ActualUser::class.java)
            Log.d(TAG, "Found ${users.size} total users in collection")
            
            // Log all users for debugging
            users.forEach { user ->
                Log.d(TAG, "User: '${user.fullName}' (${user.uid})")
            }
            
            // Filter users that match the search query in fullName
            val matchingUsers = users.filter { user ->
                user.fullName.contains(searchQuery, ignoreCase = true)
            }
            
            Log.d(TAG, "Found ${matchingUsers.size} matching users for '$searchQuery'")
            matchingUsers.forEach { user ->
                Log.d(TAG, "Match: '${user.fullName}'")
            }
            
            matchingUsers
            
        } catch (e: Exception) {
            Log.e(TAG, "Error searching real users", e)
            emptyList()
        }
    }
    
    /**
     * Search users by name using Firestore queries
     * Uses array-contains-any for efficient searching with search terms
     */
    suspend fun searchUsersByName(searchQuery: String): List<FirestoreUser> {
        if (searchQuery.isBlank()) return emptyList()
        
        return try {
            // Create search terms for better matching
            val searchTerms = createSearchTerms(searchQuery.lowercase())
            
            Log.d(TAG, "Searching for users with terms: $searchTerms")
            
            val result = usersCollection
                .whereArrayContainsAny("searchTerms", searchTerms)
                .limit(20) // Limit results for performance
                .get()
                .await()
            
            val users = result.toObjects(FirestoreUser::class.java)
            Log.d(TAG, "Found ${users.size} users")
            
            // Additional client-side filtering for more precise matching
            users.filter { user ->
                user.fullName.contains(searchQuery, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users", e)
            emptyList()
        }
    }
    
    /**
     * Alternative search method using prefix matching
     * This is more limited but works well for autocomplete-style search
     */
    suspend fun searchUsersByNamePrefix(namePrefix: String): List<FirestoreUser> {
        if (namePrefix.isBlank()) return emptyList()
        
        return try {
            val lowercasePrefix = namePrefix.lowercase()
            val endPrefix = lowercasePrefix.substring(0, lowercasePrefix.length - 1) + 
                            (lowercasePrefix.last() + 1).toString()
            
            Log.d(TAG, "Searching for users with name prefix: $lowercasePrefix")
            
            val result = usersCollection
                .whereGreaterThanOrEqualTo("fullName", lowercasePrefix)
                .whereLessThan("fullName", endPrefix)
                .limit(20)
                .get()
                .await()
            
            val users = result.toObjects(FirestoreUser::class.java)
            Log.d(TAG, "Found ${users.size} users with prefix search")
            
            users
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users by prefix", e)
            emptyList()
        }
    }
    
    /**
     * Get a specific user by their UID
     */
    suspend fun getUserByUid(uid: String): FirestoreUser? {
        return try {
            val document = usersCollection.document(uid).get().await()
            document.toObject(FirestoreUser::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by UID", e)
            null
        }
    }
    
    /**
     * Save or update a user in Firestore
     */
    suspend fun saveUser(user: FirestoreUser): Boolean {
        return try {
            val userWithSearchTerms = user.copy(
                searchTerms = createSearchTerms(user.fullName.lowercase())
            )
            
            usersCollection.document(user.uid).set(userWithSearchTerms).await()
            Log.d(TAG, "User saved successfully: ${user.uid}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
            false
        }
    }
    
    /**
     * Creates search terms for efficient searching
     * Breaks down the full name into searchable terms
     */
    private fun createSearchTerms(fullName: String): List<String> {
        val terms = mutableSetOf<String>()
        val cleanName = fullName.lowercase().trim()
        
        // Add the full name
        terms.add(cleanName)
        
        // Add individual words
        val words = cleanName.split("\\s+".toRegex())
        terms.addAll(words)
        
        // Add prefixes for each word (for autocomplete)
        words.forEach { word ->
            for (i in 1..word.length) {
                terms.add(word.substring(0, i))
            }
        }
        
        return terms.toList()
    }
    
    /**
     * Get all users (for testing purposes, with pagination)
     */
    suspend fun getAllUsers(limit: Int = 50): List<FirestoreUser> {
        return try {
            val result = usersCollection
                .limit(limit.toLong())
                .get()
                .await()
            
            result.toObjects(FirestoreUser::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all users", e)
            emptyList()
        }
    }
    
    /**
     * Search for actual users (from sign-up) by getting all and filtering client-side
     * This works with the real user structure that doesn't have searchTerms
     */
    suspend fun searchActualUsers(searchQuery: String): List<Map<String, Any>> {
        if (searchQuery.isBlank()) return emptyList()
        
        return try {
            Log.d(TAG, "Searching actual users for: $searchQuery")
            
            val result = usersCollection
                .get()
                .await()
            
            val allDocs = result.documents
            Log.d(TAG, "Found ${allDocs.size} total documents in users collection")
            
            // Filter documents that contain the search query in fullName
            val matchingDocs = allDocs.mapNotNull { doc ->
                val data = doc.data
                val fullName = data?.get("fullName") as? String
                
                Log.d(TAG, "Checking user: $fullName")
                
                if (fullName?.contains(searchQuery, ignoreCase = true) == true) {
                    data
                } else {
                    null
                }
            }
            
            Log.d(TAG, "Found ${matchingDocs.size} matching users")
            matchingDocs
            
        } catch (e: Exception) {
            Log.e(TAG, "Error searching actual users", e)
            emptyList()
        }
    }
    
    /**
     * Simple search using only fullName field - works with any user structure
     */
    suspend fun searchByFullNameOnly(searchQuery: String): List<Map<String, Any>> {
        if (searchQuery.isBlank()) return emptyList()
        
        return try {
            Log.d(TAG, "Searching users by fullName for: '$searchQuery'")
            
            // Get all documents from users collection
            val result = usersCollection.get().await()
            val allDocuments = result.documents
            
            Log.d(TAG, "Found ${allDocuments.size} total documents")
            
            // Filter documents that have fullName containing the search query
            val matchingDocs = allDocuments.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    val fullName = data["fullName"] as? String
                    Log.d(TAG, "Checking document: fullName='$fullName'")
                    
                    if (fullName != null && fullName.contains(searchQuery, ignoreCase = true)) {
                        Log.d(TAG, "MATCH FOUND: '$fullName'")
                        data
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            
            Log.d(TAG, "Found ${matchingDocs.size} matching documents")
            matchingDocs
            
        } catch (e: Exception) {
            Log.e(TAG, "Error searching by fullName", e)
            emptyList()
        }
    }
}
