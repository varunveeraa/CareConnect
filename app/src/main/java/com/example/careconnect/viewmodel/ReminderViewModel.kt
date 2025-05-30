package com.example.careconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.firestore.SchedulingReminder
import com.example.careconnect.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderViewModel : ViewModel() {
    private val reminderRepository = ReminderRepository()
    
    private val _reminders = MutableStateFlow<List<SchedulingReminder>>(emptyList())
    val reminders: StateFlow<List<SchedulingReminder>> = _reminders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _followers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val followers: StateFlow<List<Map<String, Any>>> = _followers.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    init {
        loadReminders()
        loadFollowers()
    }
    
    fun addReminder(reminder: SchedulingReminder) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("ReminderViewModel", "Adding reminder: ${reminder.title}")
                val success = reminderRepository.addReminder(reminder)
                if (success) {
                    _message.value = "Reminder added successfully"
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
    
    private fun loadReminders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("ReminderViewModel", "Loading reminders...")
                val remindersList = reminderRepository.getCurrentUserReminders()
                android.util.Log.d("ReminderViewModel", "Loaded ${remindersList.size} reminders")
                _reminders.value = remindersList
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
