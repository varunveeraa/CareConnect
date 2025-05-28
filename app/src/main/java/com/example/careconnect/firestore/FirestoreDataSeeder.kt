package com.example.careconnect.firestore

import android.util.Log
import com.example.careconnect.repository.FirestoreUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreDataSeeder {
    private val firestoreUserRepository = FirestoreUserRepository()
    
    companion object {
        private const val TAG = "FirestoreDataSeeder"
    }
    
    fun seedSampleUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if data already exists
                val existingUsers = firestoreUserRepository.getAllUsers(5)
                if (existingUsers.isNotEmpty()) {
                    Log.d(TAG, "Sample users already exist in Firestore")
                    return@launch
                }
                
                Log.d(TAG, "Seeding sample users to Firestore...")
                
                val sampleUsers = listOf(
                    FirestoreUser(
                        uid = "dr_sarah_johnson",
                        fullName = "Dr. Sarah Johnson",
                        email = "sarah.johnson@healthcare.com",
                        bio = "Cardiologist specializing in preventive care. Passionate about helping patients maintain healthy lifestyles.",
                        healthConditions = "None",
                        focusArea = "Cardiovascular Health",
                        followersCount = 156,
                        followingCount = 43,
                        dateOfBirth = "1985-04-15",
                        gender = "Female"
                    ),
                    FirestoreUser(
                        uid = "mike_chen_fitness",
                        fullName = "Mike Chen",
                        email = "mike.chen@email.com",
                        bio = "Fitness enthusiast managing Type 1 diabetes. Love sharing tips for staying active with chronic conditions.",
                        healthConditions = "Type 1 Diabetes",
                        focusArea = "Fitness & Exercise",
                        followersCount = 89,
                        followingCount = 127,
                        dateOfBirth = "1992-08-22",
                        gender = "Male"
                    ),
                    FirestoreUser(
                        uid = "emma_rodriguez_wellness",
                        fullName = "Emma Rodriguez",
                        email = "emma.rodriguez@email.com",
                        bio = "Mental health advocate and yoga instructor. Focused on holistic wellness approaches.",
                        healthConditions = "Anxiety, Depression",
                        focusArea = "Mental Health",
                        followersCount = 234,
                        followingCount = 78,
                        dateOfBirth = "1990-12-03",
                        gender = "Female"
                    ),
                    FirestoreUser(
                        uid = "james_wilson_nutrition",
                        fullName = "James Wilson",
                        email = "james.wilson@email.com",
                        bio = "Nutritionist helping people with autoimmune conditions through dietary changes.",
                        healthConditions = "Rheumatoid Arthritis",
                        focusArea = "Nutrition & Diet",
                        followersCount = 145,
                        followingCount = 92,
                        dateOfBirth = "1978-06-10",
                        gender = "Male"
                    ),
                    FirestoreUser(
                        uid = "lisa_park_sleep",
                        fullName = "Lisa Park",
                        email = "lisa.park@email.com",
                        bio = "Sleep researcher and wellness coach. Helping others improve their sleep quality and overall health.",
                        healthConditions = "Sleep Apnea",
                        focusArea = "Sleep & Recovery",
                        followersCount = 67,
                        followingCount = 134,
                        dateOfBirth = "1995-02-28",
                        gender = "Female"
                    ),
                    FirestoreUser(
                        uid = "alex_thompson_physical_therapy",
                        fullName = "Alex Thompson",
                        email = "alex.thompson@email.com",
                        bio = "Physical therapist specializing in chronic pain management and rehabilitation.",
                        healthConditions = "Lower Back Pain",
                        focusArea = "Physical Therapy",
                        followersCount = 198,
                        followingCount = 55,
                        dateOfBirth = "1987-09-14",
                        gender = "Non-binary"
                    ),
                    FirestoreUser(
                        uid = "maria_garcia_diabetes",
                        fullName = "Maria Garcia",
                        email = "maria.garcia@email.com",
                        bio = "Diabetes educator and advocate. Living with Type 2 diabetes for 15 years and helping others navigate their journey.",
                        healthConditions = "Type 2 Diabetes, Hypertension",
                        focusArea = "Diabetes Management",
                        followersCount = 312,
                        followingCount = 89,
                        dateOfBirth = "1975-03-20",
                        gender = "Female"
                    )
                )
                
                sampleUsers.forEach { user ->
                    val success = firestoreUserRepository.saveUser(user)
                    if (success) {
                        Log.d(TAG, "Successfully saved user: ${user.fullName}")
                    } else {
                        Log.e(TAG, "Failed to save user: ${user.fullName}")
                    }
                }
                
                Log.d(TAG, "Finished seeding ${sampleUsers.size} sample users")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding sample users", e)
            }
        }
    }
}