package com.example.careconnect.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.careconnect.health.HealthDataManager

object HealthDataInitializer {
    private const val PREF_GLOBAL_DUMMY_DATA_INITIALIZED = "global_dummy_data_initialized"
    
    fun initializeHealthDataIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences("health_data_global", Context.MODE_PRIVATE)
        val isInitialized = prefs.getBoolean(PREF_GLOBAL_DUMMY_DATA_INITIALIZED, false)
        
        if (!isInitialized) {
            val healthDataManager = HealthDataManager(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Generate dummy data for all users
                    healthDataManager.generateDummyDataForAllUsers()
                    
                    // Mark as initialized
                    prefs.edit()
                        .putBoolean(PREF_GLOBAL_DUMMY_DATA_INITIALIZED, true)
                        .apply()
                } catch (e: Exception) {
                    // Handle error silently or log it
                }
            }
        }
    }
    
    fun resetInitialization(context: Context) {
        val prefs = context.getSharedPreferences("health_data_global", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(PREF_GLOBAL_DUMMY_DATA_INITIALIZED, false)
            .apply()
    }
}