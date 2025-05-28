package com.example.careconnect.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataSeeder(private val database: AppDatabase) {
    
    fun seedSampleUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = database.userDao()
            
            // Check if sample data already exists
            val existingUser = userDao.getUserById(2)
            if (existingUser != null) return@launch
            
            // Sample users
            val sampleUsers = listOf(
                User(
                    id = 2,
                    fullName = "Dr. Sarah Johnson",
                    email = "sarah.johnson@healthcare.com",
                    password = "password123",
                    dateOfBirth = "1985-04-15",
                    gender = "Female",
                    bio = "Cardiologist specializing in preventive care. Passionate about helping patients maintain healthy lifestyles.",
                    healthConditions = "None",
                    focusArea = "Cardiovascular Health",
                    followersCount = 156,
                    followingCount = 43
                ),
                User(
                    id = 3,
                    fullName = "Mike Chen",
                    email = "mike.chen@email.com",
                    password = "password123",
                    dateOfBirth = "1992-08-22",
                    gender = "Male",
                    bio = "Fitness enthusiast managing Type 1 diabetes. Love sharing tips for staying active with chronic conditions.",
                    healthConditions = "Type 1 Diabetes",
                    focusArea = "Fitness & Exercise",
                    followersCount = 89,
                    followingCount = 127
                ),
                User(
                    id = 4,
                    fullName = "Emma Rodriguez",
                    email = "emma.rodriguez@email.com",
                    password = "password123",
                    dateOfBirth = "1990-12-03",
                    gender = "Female",
                    bio = "Mental health advocate and yoga instructor. Focused on holistic wellness approaches.",
                    healthConditions = "Anxiety, Depression",
                    focusArea = "Mental Health",
                    followersCount = 234,
                    followingCount = 78
                ),
                User(
                    id = 5,
                    fullName = "James Wilson",
                    email = "james.wilson@email.com",
                    password = "password123",
                    dateOfBirth = "1978-06-10",
                    gender = "Male",
                    bio = "Nutritionist helping people with autoimmune conditions through dietary changes.",
                    healthConditions = "Rheumatoid Arthritis",
                    focusArea = "Nutrition & Diet",
                    followersCount = 145,
                    followingCount = 92
                ),
                User(
                    id = 6,
                    fullName = "Lisa Park",
                    email = "lisa.park@email.com",
                    password = "password123",
                    dateOfBirth = "1995-02-28",
                    gender = "Female",
                    bio = "Sleep researcher and wellness coach. Helping others improve their sleep quality and overall health.",
                    healthConditions = "Sleep Apnea",
                    focusArea = "Sleep & Recovery",
                    followersCount = 67,
                    followingCount = 134
                )
            )
            
            sampleUsers.forEach { user ->
                try {
                    userDao.insertUser(user)
                } catch (e: Exception) {
                    // User might already exist, that's okay
                }
            }
        }
    }
}