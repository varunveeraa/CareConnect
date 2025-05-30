package com.example.careconnect.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.careconnect.MainActivity
import com.example.careconnect.R

class ReminderNotificationReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "reminder_notifications"
        const val NOTIFICATION_ID_BASE = 1000
        const val EXTRA_REMINDER_TITLE = "reminder_title"
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: "Reminder"
        val type = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: "General"
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: ""
        
        createNotificationChannel(context)
        showNotification(context, title, type, reminderId)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Notifications"
            val descriptionText = "Notifications for health reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(context: Context, title: String, type: String, reminderId: String) {
        // Check if we have notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_reminders", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Default icon for now
            .setContentTitle("⏰ $title")
            .setContentText("Time for your $type reminder")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Time for your $type reminder: $title\n\nTap to open CareConnect and manage your reminders.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        val notificationId = NOTIFICATION_ID_BASE + reminderId.hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}