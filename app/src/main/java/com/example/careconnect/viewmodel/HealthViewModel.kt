package com.example.careconnect.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.health.HealthDataManager
import com.example.careconnect.health.HealthMetrics
import com.example.careconnect.health.WearableType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthViewModel(context: Context) : ViewModel() {
    
    private val healthDataManager = HealthDataManager(context)
    
    private val _healthMetrics = MutableStateFlow<HealthMetrics?>(null)
    val healthMetrics: StateFlow<HealthMetrics?> = _healthMetrics.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectedWearable = MutableStateFlow<WearableType?>(null)
    val connectedWearable: StateFlow<WearableType?> = _connectedWearable.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow("")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()
    
    init {
        checkConnectionStatus()
    }
    
    private fun checkConnectionStatus() {
        _isConnected.value = healthDataManager.isWearableConnected()
        _connectedWearable.value = healthDataManager.getConnectedWearable()
        
        if (_isConnected.value) {
            refreshHealthData()
        }
    }
    
    fun connectWearable(wearableType: WearableType) {
        viewModelScope.launch {
            _isLoading.value = true
            _connectionStatus.value = "Connecting to ${wearableType.displayName}..."
            
            try {
                val success = healthDataManager.connectWearable(wearableType)
                if (success) {
                    _isConnected.value = true
                    _connectedWearable.value = wearableType
                    _connectionStatus.value = "Connected to ${wearableType.displayName}"
                    refreshHealthData()
                } else {
                    _connectionStatus.value = "Failed to connect to ${wearableType.displayName}"
                }
            } catch (e: Exception) {
                _connectionStatus.value = "Connection error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun disconnectWearable() {
        val success = healthDataManager.disconnectWearable()
        if (success) {
            _isConnected.value = false
            _connectedWearable.value = null
            _healthMetrics.value = null
            _connectionStatus.value = "Disconnected from wearable"
        } else {
            _connectionStatus.value = "Failed to disconnect"
        }
    }
    
    fun refreshHealthData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val metrics = healthDataManager.fetchHealthMetrics()
                _healthMetrics.value = metrics
                healthDataManager.updateLastSyncTime()
                _connectionStatus.value = "Health data synced successfully"
            } catch (e: Exception) {
                _connectionStatus.value = "Failed to sync health data"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getAvailableWearables(): List<WearableType> {
        return healthDataManager.getAvailableWearables()
    }
    
    fun getLastSyncTime(): Long {
        return healthDataManager.getLastSyncTime()
    }
    
    fun clearConnectionStatus() {
        _connectionStatus.value = ""
    }
}