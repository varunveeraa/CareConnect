package com.example.careconnect.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.health.HealthDataManager
import com.example.careconnect.health.HealthMetrics
import com.example.careconnect.health.WearableType
import com.example.careconnect.health.MetricsPeriod
import com.example.careconnect.health.HealthSummary
import com.example.careconnect.health.DailyHealthData
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
    
    private val _dailySummary = MutableStateFlow<HealthSummary?>(null)
    val dailySummary: StateFlow<HealthSummary?> = _dailySummary.asStateFlow()
    
    private val _weeklySummary = MutableStateFlow<HealthSummary?>(null)
    val weeklySummary: StateFlow<HealthSummary?> = _weeklySummary.asStateFlow()
    
    private val _monthlySummary = MutableStateFlow<HealthSummary?>(null)
    val monthlySummary: StateFlow<HealthSummary?> = _monthlySummary.asStateFlow()
    
    private val _detailedData = MutableStateFlow<List<DailyHealthData>>(emptyList())
    val detailedData: StateFlow<List<DailyHealthData>> = _detailedData.asStateFlow()
    
    init {
        checkConnectionStatus()
        loadHealthSummaries()
    }
    
    private fun checkConnectionStatus() {
        _isConnected.value = healthDataManager.isWearableConnected()
        _connectedWearable.value = healthDataManager.getConnectedWearable()
        
        if (_isConnected.value) {
            refreshHealthData()
        }
    }
    
    private fun loadHealthSummaries() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Load all summaries concurrently
                val dailyJob = launch { 
                    _dailySummary.value = healthDataManager.getHealthSummary(MetricsPeriod.DAILY)
                }
                val weeklyJob = launch { 
                    _weeklySummary.value = healthDataManager.getHealthSummary(MetricsPeriod.WEEKLY)
                }
                val monthlyJob = launch { 
                    _monthlySummary.value = healthDataManager.getHealthSummary(MetricsPeriod.MONTHLY)
                }
                
                // Wait for all to complete
                dailyJob.join()
                weeklyJob.join()
                monthlyJob.join()
                
            } catch (e: Exception) {
                _connectionStatus.value = "Failed to load health summaries"
            } finally {
                _isLoading.value = false
            }
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
                    loadHealthSummaries()
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
    
    fun loadDetailedData(period: MetricsPeriod) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val data = healthDataManager.fetchHealthMetricsForPeriod(period)
                _detailedData.value = data
            } catch (e: Exception) {
                _connectionStatus.value = "Failed to load detailed data"
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
    
    fun generateDummyDataForAllUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _connectionStatus.value = "Generating dummy data for all users..."
                healthDataManager.generateDummyDataForAllUsers()
                _connectionStatus.value = "Dummy data generated successfully"
                loadHealthSummaries()
            } catch (e: Exception) {
                _connectionStatus.value = "Failed to generate dummy data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
