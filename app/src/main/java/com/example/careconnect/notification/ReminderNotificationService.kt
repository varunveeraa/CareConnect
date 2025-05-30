package com.example.careconnect.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.careconnect.firestore.SchedulingReminder
import java.text.SimpleDateFormat
import java.util.*

class ReminderNotificationService(private val context: Context) {
    
    companion object {
        private const val TAG = "ReminderNotificationService"
    }
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleNotification(reminder: SchedulingReminder) {
        try {
            // Parse the reminder time and dates
            val dates = generateDateRange(reminder.startDate, reminder.endDate)
            val timeComponents = parseTime(reminder.reminderTime)
            
            dates.forEach { date ->
                val calendar = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, timeComponents.first)
                    set(Calendar.MINUTE, timeComponents.second)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                // Only schedule if the time is in the future
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    scheduleAlarm(reminder, calendar.timeInMillis)
                    Log.d(TAG, "Scheduled notification for ${reminder.title} at ${calendar.time}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling notification for ${reminder.title}", e)
        }
    }
    
    fun cancelNotification(reminderId: String) {
        try {
            val intent = Intent(context, ReminderNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled notification for reminder ID: $reminderId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification for reminder ID: $reminderId", e)
        }
    }
    
    private fun scheduleAlarm(reminder: SchedulingReminder, triggerTime: Long) {
        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_TYPE, reminder.type)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_ID, reminder.id)
        }
        
        val requestCode = (reminder.id + triggerTime.toString()).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to inexact alarm if exact alarm permission is not granted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.w(TAG, "Using inexact alarm due to permission restrictions")
        }
    }
    
    private fun generateDateRange(startDateStr: String, endDateStr: String): List<Date> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.parse(startDateStr) ?: return emptyList()
        val endDate = dateFormat.parse(endDateStr) ?: return emptyList()
        
        val dates = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        while (calendar.time <= endDate) {
            dates.add(Date(calendar.timeInMillis))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return dates
    }
    
    private fun parseTime(timeStr: String): Pair<Int, Int> {
        val parts = timeStr.split(":")
        return if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: 9
            val minute = parts[1].toIntOrNull() ?: 0
            Pair(hour, minute)
        } else {
            Pair(9, 0) // Default to 9:00 AM
        }
    }
    
    fun requestNotificationPermission(): Boolean {
        // This method can be used to check if notification permissions are granted
        // The actual permission request should be handled in the UI layer
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Notifications are automatically granted on older versions
        }
    }
    
    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Exact alarms are automatically granted on older versions
        }
    }
}