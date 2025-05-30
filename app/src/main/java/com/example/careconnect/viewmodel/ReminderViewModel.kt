package com.example.careconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.firestore.SchedulingReminder
import com.example.careconnect.notification.ReminderNotificationService
import com.example.careconnect.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val reminderRepository = ReminderRepository()
    private val notificationService = ReminderNotificationService(application.applicationContext)
    
    private val _reminders = MutableStateFlow<List<SchedulingReminder>>(emptyList())
    val reminders: StateFlow<List<SchedulingReminder>> = _reminders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _followers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val followers: StateFlow<List<Map<String, Any>>> = _followers.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()
    
    private val _hasExactAlarmPermission = MutableStateFlow(false)
    val hasExactAlarmPermission: StateFlow<Boolean> = _hasExactAlarmPermission.asStateFlow()
    
    init {
        loadReminders()
        loadFollowers()
        checkPermissions()
    }
    
    fun addReminder(reminder: SchedulingReminder) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("ReminderViewModel", "Adding reminder: ${reminder.title}")
                val success = reminderRepository.addReminder(reminder)
                if (success) {
                    // Schedule notification for the new reminder
                    if (_hasNotificationPermission.value) {
                        notificationService.scheduleNotification(reminder)
                        _message.value = "Reminder added with notifications enabled"
                    } else {
                        _message.value = "Reminder added (notifications require permission)"
                    }
                    android.util.Log.d("ReminderViewModel", "Reminder added, refreshing list...")
                    loadReminders() // Refresh the list
                } else {
                    _message.value = "Failed to add reminder"
                    android.util.Log.e("ReminderViewModel", "Failed to add reminder")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                android.util.Log.e("ReminderViewModel", "Error adding reminder", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cancel notification first
                notificationService.cancelNotification(reminderId)
                
                val success = reminderRepository.deleteReminder(reminderId)
                if (success) {
                    _message.value = "Reminder deleted successfully"
                    loadReminders() // Refresh the list
                } else {
                    _message.value = "Failed to delete reminder"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshReminders() {
        loadReminders()
    }
    
    fun rescheduleAllNotifications() {
        viewModelScope.launch {
            if (_hasNotificationPermission.value) {
                try {
                    _reminders.value.forEach { reminder ->
                        notificationService.scheduleNotification(reminder)
                    }
                    _message.value = "All reminder notifications rescheduled"
                } catch (e: Exception) {
                    _message.value = "Error rescheduling notifications: ${e.message}"
                }
            } else {
                _message.value = "Notification permission required"
            }
        }
    }
    
    fun checkPermissions() {
        _hasNotificationPermission.value = notificationService.requestNotificationPermission()
        _hasExactAlarmPermission.value = notificationService.hasExactAlarmPermission()
    }
    
    private fun loadReminders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("ReminderViewModel", "Loading reminders...")
                val remindersList = reminderRepository.getCurrentUserReminders()
                android.util.Log.d("ReminderViewModel", "Loaded ${remindersList.size} reminders")
                _reminders.value = remindersList
                
                // Schedule notifications for all loaded reminders if permission is granted
                if (_hasNotificationPermission.value) {
                    remindersList.forEach { reminder ->
                        notificationService.scheduleNotification(reminder)
                    }
                }
            } catch (e: Exception) {
                _message.value = "Error loading reminders: ${e.message}"
                android.util.Log.e("ReminderViewModel", "Error loading reminders", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadFollowers() {
        viewModelScope.launch {
            try {
                val followersList = reminderRepository.getCurrentUserFollowers()
                _followers.value = followersList
            } catch (e: Exception) {
                _message.value = "Error loading followers: ${e.message}"
            }
        }
    }
    
    fun clearMessage() {
        _message.value = ""
    }
}
