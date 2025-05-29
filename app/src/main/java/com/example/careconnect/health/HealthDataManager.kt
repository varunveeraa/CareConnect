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

enum class WearableType(val displayName: String) {
    GOOGLE_FIT("Google Fit"),
    MI_FITNESS("Mi Fitness"),
    APPLE_HEALTH("Apple Health"),
    SAMSUNG_HEALTH("Samsung Health")
}

enum class MetricsPeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

class HealthDataManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("health_data", Context.MODE_PRIVATE)
    private val db = Firebase.firestore
    
    companion object {
        private const val PREF_CONNECTED_WEARABLE = "connected_wearable"
        private const val PREF_CONNECTION_STATUS = "connection_status"
        private const val PREF_LAST_SYNC = "last_sync"
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
            
            // Store dummy health data when connecting
            storeDummyHealthData()
            
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
        
        // Simulate data fetching delay
        delay(1000)
        
        // Generate realistic mock data
        return HealthMetrics(
            stepCount = Random.nextInt(5000, 15000),
            heartRate = Random.nextInt(60, 100).toFloat(),
            sleepHours = Random.nextDouble(6.0, 9.0).toFloat(),
            calories = Random.nextInt(1800, 2500).toFloat(),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Store dummy health data in Firestore
     */
    suspend fun storeDummyHealthData() {
        // Generate 30 days of dummy data
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        
        for (i in 0 until 30) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val date = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
            
            val data = DailyHealthData(
                date = date,
                stepCount = Random.nextInt(5000, 15000),
                heartRate = Random.nextInt(60, 100).toFloat(),
                sleepHours = Random.nextDouble(6.0, 9.0).toFloat(),
                calories = Random.nextInt(1800, 2500).toFloat()
            )
            
            db.collection("users").document(getCurrentUserId())
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
        
        val query = db.collection("users").document(getCurrentUserId())
            .collection("healthData")
        
        try {
            val documents = query.get().await()
            for (document in documents) {
                val data = document.toObject(DailyHealthData::class.java)
                healthData.add(data)
            }
        } catch (e: Exception) {
            // Handle error
        }
        
        return healthData
    }
    
    // Get current user ID from Firebase Auth
    private fun getCurrentUserId(): String {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user"
    }
}
