package com.example.careconnect.health

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

data class HealthMetrics(
    val stepCount: Int = 0,
    val heartRate: Float = 0f,
    val sleepHours: Float = 0f,
    val calories: Float = 0f,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class DailyHealthData(
    @PropertyName("date") val date: String = "", // Format: "yyyy-MM-dd"
    @PropertyName("stepCount") val stepCount: Int = 0,
    @PropertyName("heartRate") val heartRate: Float = 0f,
    @PropertyName("sleepHours") val sleepHours: Float = 0f,
    @PropertyName("calories") val calories: Float = 0f,
    @PropertyName("timestamp") val timestamp: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", 0, 0f, 0f, 0f, 0L)
}

data class HealthSummary(
    val avgStepCount: Int = 0,
    val avgHeartRate: Float = 0f,
    val avgSleepHours: Float = 0f,
    val avgCalories: Float = 0f,
    val totalDays: Int = 0,
    val period: String = ""
)

enum class WearableType(val displayName: String) {
    GOOGLE_FIT("Google Fit"),
    MI_FITNESS("Mi Fitness"),
    APPLE_HEALTH("Apple Health"),
    SAMSUNG_HEALTH("Samsung Health")
}

enum class MetricsPeriod(val displayName: String, val days: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30)
}

class HealthDataManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("health_data", Context.MODE_PRIVATE)
    private val db = Firebase.firestore
    
    companion object {
        private const val PREF_CONNECTED_WEARABLE = "connected_wearable"
        private const val PREF_CONNECTION_STATUS = "connection_status"
        private const val PREF_LAST_SYNC = "last_sync"
        private const val PREF_DUMMY_DATA_GENERATED = "dummy_data_generated"
    }
    
    /**
     * Check if any wearable is connected
     */
    fun isWearableConnected(): Boolean {
        return prefs.getBoolean(PREF_CONNECTION_STATUS, false)
    }
    
    /**
     * Get connected wearable type
     */
    fun getConnectedWearable(): WearableType? {
        val wearableName = prefs.getString(PREF_CONNECTED_WEARABLE, null)
        return WearableType.values().find { it.displayName == wearableName }
    }
    
    /**
     * Connect to a wearable device (simulated)
     */
    suspend fun connectWearable(wearableType: WearableType): Boolean {
        // Simulate connection process
        delay(2000) // Simulate connection time
        
        return try {
            // In a real implementation, this would handle actual API connections
            // For now, we'll simulate a successful connection
            prefs.edit()
                .putString(PREF_CONNECTED_WEARABLE, wearableType.displayName)
                .putBoolean(PREF_CONNECTION_STATUS, true)
                .putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                .apply()
            
            // Store dummy health data when connecting (only once per user)
            if (!prefs.getBoolean(PREF_DUMMY_DATA_GENERATED, false)) {
                storeDummyHealthData()
                prefs.edit().putBoolean(PREF_DUMMY_DATA_GENERATED, true).apply()
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Disconnect from wearable
     */
    fun disconnectWearable(): Boolean {
        return try {
            prefs.edit()
                .remove(PREF_CONNECTED_WEARABLE)
                .putBoolean(PREF_CONNECTION_STATUS, false)
                .remove(PREF_LAST_SYNC)
                .apply()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Fetch health metrics from Firestore
     */
    suspend fun fetchHealthMetrics(): HealthMetrics? {
        if (!isWearableConnected()) {
            return null
        }
        
        // Get today's data from Firestore
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        try {
            val document = db.collection("users").document(getCurrentUserId())
                .collection("healthData").document(today)
                .get().await()
            
            if (document.exists()) {
                val data = document.toObject(DailyHealthData::class.java)
                return data?.let {
                    HealthMetrics(
                        stepCount = it.stepCount,
                        heartRate = it.heartRate,
                        sleepHours = it.sleepHours,
                        calories = it.calories,
                        lastUpdated = it.timestamp
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback to generated data
        }
        
        // Fallback: Generate realistic mock data with user-specific seed
        val userSeed = getCurrentUserId().hashCode().toLong()
        val random = Random(userSeed + System.currentTimeMillis() / (24 * 60 * 60 * 1000)) // Change daily
        
        return HealthMetrics(
            stepCount = random.nextInt(5000, 15000),
            heartRate = random.nextInt(60, 100).toFloat(),
            sleepHours = random.nextDouble(6.0, 9.0).toFloat(),
            calories = random.nextInt(1800, 2500).toFloat(),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Store dummy health data in Firestore for current user
     */
    suspend fun storeDummyHealthData() {
        val userId = getCurrentUserId()
        val userSeed = userId.hashCode().toLong()
        val random = Random(userSeed)
        
        // Generate 90 days of dummy data
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -90)
        
        for (i in 0 until 90) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            // Use day-specific seed for consistent but varied data
            val daySeed = userSeed + i
            val dayRandom = Random(daySeed)
            
            val data = DailyHealthData(
                date = date,
                stepCount = dayRandom.nextInt(4000, 16000),
                heartRate = (dayRandom.nextInt(55, 105) + dayRandom.nextFloat()).toFloat(),
                sleepHours = (dayRandom.nextDouble(5.5, 9.5)).toFloat(),
                calories = (dayRandom.nextInt(1600, 2800) + dayRandom.nextFloat() * 100).toFloat()
            )
            
            db.collection("users").document(userId)
                .collection("healthData").document(date)
                .set(data)
                .await()
        }
    }
    
    /**
     * Generate dummy data for all existing users (admin function)
     */
    suspend fun generateDummyDataForAllUsers() {
        try {
            val usersSnapshot = db.collection("users").get().await()
            
            for (userDoc in usersSnapshot.documents) {
                val userId = userDoc.id
                generateDummyDataForUser(userId)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    /**
     * Generate dummy data for a specific user
     */
    private suspend fun generateDummyDataForUser(userId: String) {
        val userSeed = userId.hashCode().toLong()
            
        // Generate 90 days of dummy data
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -90)
        
        for (i in 0 until 90) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            // Use day-specific seed for consistent but varied data
            val daySeed = userSeed + i
            val dayRandom = Random(daySeed)
            
            val data = DailyHealthData(
                date = date,
                stepCount = dayRandom.nextInt(4000, 16000),
                heartRate = (dayRandom.nextInt(55, 105) + dayRandom.nextFloat()).toFloat(),
                sleepHours = (dayRandom.nextDouble(5.5, 9.5)).toFloat(),
                calories = (dayRandom.nextInt(1600, 2800) + dayRandom.nextFloat() * 100).toFloat()
            )
            
            db.collection("users").document(userId)
                .collection("healthData").document(date)
                .set(data)
                .await()
        }
    }
    
    /**
     * Get last sync time
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(PREF_LAST_SYNC, 0)
    }
    
    /**
     * Update last sync time
     */
    fun updateLastSyncTime() {
        prefs.edit()
            .putLong(PREF_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Get available wearable options
     */
    fun getAvailableWearables(): List<WearableType> {
        return listOf(
            WearableType.GOOGLE_FIT,
            WearableType.MI_FITNESS,
            WearableType.SAMSUNG_HEALTH
            // Note: Apple Health would only be available on iOS
        )
    }
    
    /**
     * Fetch health metrics for a specific period
     */
    suspend fun fetchHealthMetricsForPeriod(period: MetricsPeriod): List<DailyHealthData> {
        val healthData = mutableListOf<DailyHealthData>()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -period.days)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        try {
            val query = db.collection("users").document(getCurrentUserId())
                .collection("healthData")
                .whereGreaterThanOrEqualTo("date", startDate)
                .orderBy("date", Query.Direction.ASCENDING)
            
            val documents = query.get().await()
            for (document in documents) {
                val data = document.toObject(DailyHealthData::class.java)
                healthData.add(data)
            }
        } catch (e: Exception) {
            // Handle error
        }
        
        return healthData.takeLast(period.days)
    }
    
    /**
     * Get health summary for a specific period
     */
    suspend fun getHealthSummary(period: MetricsPeriod): HealthSummary {
        val healthData = fetchHealthMetricsForPeriod(period)
        
        if (healthData.isEmpty()) {
            return HealthSummary(period = period.displayName)
        }
        
        val avgStepCount = healthData.map { it.stepCount }.average().toInt()
        val avgHeartRate = healthData.map { it.heartRate }.average().toFloat()
        val avgSleepHours = healthData.map { it.sleepHours }.average().toFloat()
        val avgCalories = healthData.map { it.calories }.average().toFloat()
        
        return HealthSummary(
            avgStepCount = avgStepCount,
            avgHeartRate = avgHeartRate,
            avgSleepHours = avgSleepHours,
            avgCalories = avgCalories,
            totalDays = healthData.size,
            period = period.displayName
        )
    }
    
    // Get current user ID from Firebase Auth
    private fun getCurrentUserId(): String {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user"
    }
}
