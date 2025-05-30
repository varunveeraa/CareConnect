package com.example.careconnect.location

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.example.careconnect.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationTrackingManager(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun startPeriodicLocationTracking() {
        val intent = Intent(context, LocationUpdateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule to run every hour with more aggressive wake-up
        val intervalMillis = 60 * 60 * 1000L // 1 hour
        
        // Use setRepeating for more reliable delivery (though still limited on modern Android)
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
        
        Log.d("LocationTrackingManager", "Started periodic location tracking every hour")
    }
    
    fun stopPeriodicLocationTracking() {
        val intent = Intent(context, LocationUpdateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d("LocationTrackingManager", "Stopped periodic location tracking")
    }
}

class LocationUpdateReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("LocationUpdateReceiver", "🚀 Background location update triggered!")
        
        val locationManager = LocationManager(context)
        val locationRepository = LocationRepository()
        
        if (!locationManager.hasLocationPermission()) {
            Log.w("LocationUpdateReceiver", "❌ Location permission not granted")
            return
        }
        
        locationManager.getCurrentLocation { location ->
            if (location != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val success = locationRepository.saveLocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy
                        )
                        
                        if (success) {
                            Log.d("LocationUpdateReceiver", 
                                "✅ Background location saved: ${location.latitude}, ${location.longitude}")
                        } else {
                            Log.e("LocationUpdateReceiver", "❌ Failed to save background location")
                        }
                    } catch (e: Exception) {
                        Log.e("LocationUpdateReceiver", "❌ Error saving background location", e)
                    }
                }
            } else {
                Log.w("LocationUpdateReceiver", "⚠️ Could not get background location")
            }
        }
    }
}
