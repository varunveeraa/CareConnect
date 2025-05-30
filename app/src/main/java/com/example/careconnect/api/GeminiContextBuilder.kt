package com.example.careconnect.api

import android.content.Context
import android.location.Location
import com.example.careconnect.firestore.ActualUser
import com.example.careconnect.health.HealthDataManager
import com.example.careconnect.health.HealthMetrics
import com.example.careconnect.location.LocationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class UserContext(
    val user: ActualUser?,
    val healthMetrics: HealthMetrics?,
    val location: Location?,
    val currentDate: String,
    val currentTime: String
)

class GeminiContextBuilder(
    private val context: Context,
    private val healthDataManager: HealthDataManager,
    private val locationManager: LocationManager
) {
    
    suspend fun buildUserContext(): UserContext = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        
        val user = if (userId != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(userId).get().await()
                doc.toObject(ActualUser::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
        
        val healthMetrics = try {
            healthDataManager.fetchHealthMetrics()
        } catch (e: Exception) {
            null
        }
        
        val location = suspendCoroutine<Location?> { continuation ->
            locationManager.getCurrentLocation { loc ->
                continuation.resume(loc)
            }
        }
        
        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        UserContext(
            user = user,
            healthMetrics = healthMetrics,
            location = location,
            currentDate = dateFormat.format(now),
            currentTime = timeFormat.format(now)
        )
    }
    
    fun formatContextMessage(userContext: UserContext): String {
        val contextBuilder = StringBuilder()
        
        contextBuilder.append("Context about the user you're chatting with:\n\n")
        
        // User basic information
        userContext.user?.let { user ->
            contextBuilder.append("User Information:\n")
            contextBuilder.append("- Name: ${user.fullName}\n")
            contextBuilder.append("- Age: ${calculateAge(user.dateOfBirth)}\n")
            contextBuilder.append("- Gender: ${user.gender}\n")
            
            if (user.healthConditions.isNotEmpty()) {
                contextBuilder.append("- Health Conditions: ${user.healthConditions.joinToString(", ")}\n")
            }
            
            if (user.focusAreas.isNotEmpty()) {
                contextBuilder.append("- Health Focus Areas: ${user.focusAreas.joinToString(", ")}\n")
            }
            
            contextBuilder.append("\n")
        }
        
        // Current date and time
        contextBuilder.append("Current Information:\n")
        contextBuilder.append("- Date: ${userContext.currentDate}\n")
        contextBuilder.append("- Time: ${userContext.currentTime}\n")
        
        // Location information (if available)
        userContext.location?.let { location ->
            contextBuilder.append("- GPS Coordinates: ${location.latitude}, ${location.longitude}\n")
            contextBuilder.append("- Location Accuracy: ${location.accuracy}m\n")
        }
        
        contextBuilder.append("\n")
        
        // Health metrics from today
        userContext.healthMetrics?.let { metrics ->
            contextBuilder.append("Today's Health Metrics:\n")
            contextBuilder.append("- Steps: ${metrics.stepCount}\n")
            contextBuilder.append("- Heart Rate: ${String.format("%.1f", metrics.heartRate)} bpm\n")
            contextBuilder.append("- Sleep: ${String.format("%.1f", metrics.sleepHours)} hours\n")
            contextBuilder.append("- Calories: ${String.format("%.0f", metrics.calories)}\n")
            
            val lastUpdatedTime = Date(metrics.lastUpdated)
            val updateTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            contextBuilder.append("- Last Updated: ${updateTimeFormat.format(lastUpdatedTime)}\n")
            contextBuilder.append("\n")
        }
        
        contextBuilder.append("Please use this context to provide personalized and relevant health advice and responses. ")
        contextBuilder.append("Take into account the user's health conditions, current metrics, and focus areas when responding. ")
        contextBuilder.append("Be supportive and provide actionable advice when appropriate.\n\n")
        contextBuilder.append("User's message: ")
        
        return contextBuilder.toString()
    }
    
    private fun calculateAge(dateOfBirth: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = dateFormat.parse(dateOfBirth)
            if (birthDate != null) {
                val birthCalendar = Calendar.getInstance().apply { time = birthDate }
                val todayCalendar = Calendar.getInstance()
                
                var age = todayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                
                if (todayCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                
                "$age years old"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
