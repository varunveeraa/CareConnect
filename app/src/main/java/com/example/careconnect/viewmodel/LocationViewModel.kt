package com.example.careconnect.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.careconnect.location.LocationManager
import com.example.careconnect.location.LocationTrackingManager
import com.example.careconnect.repository.LocationRepository
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val locationManager = LocationManager(application)
    private val locationTrackingManager = LocationTrackingManager(application)
    private val locationRepository = LocationRepository()
    
    private val _isTrackingEnabled = MutableLiveData<Boolean>(false)
    val isTrackingEnabled: LiveData<Boolean> = _isTrackingEnabled
    
    private val _lastLocationStatus = MutableLiveData<String>("")
    val lastLocationStatus: LiveData<String> = _lastLocationStatus
    
    private val _shouldRequestPermission = MutableLiveData<Boolean>(false)
    val shouldRequestPermission: LiveData<Boolean> = _shouldRequestPermission
    
    fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }
    
    fun requestLocationPermission() {
        _shouldRequestPermission.value = true
    }
    
    fun onPermissionRequestHandled() {
        _shouldRequestPermission.value = false
    }
    
    fun onPermissionGranted() {
        _lastLocationStatus.value = "Location permission granted! You can now start tracking."
        _shouldRequestPermission.value = false
    }
    
    fun onPermissionDenied() {
        _lastLocationStatus.value = "Location permission denied. Please enable it in settings to use location tracking."
        _shouldRequestPermission.value = false
    }
    
    fun startLocationTracking() {
        if (!hasLocationPermission()) {
            _lastLocationStatus.value = "Location permission not granted"
            return
        }
        
        try {
            locationTrackingManager.startPeriodicLocationTracking()
            _isTrackingEnabled.value = true
            _lastLocationStatus.value = "Location tracking started - getting initial location..."
            Log.d("LocationViewModel", "Location tracking started")
            
            // Get and save current location immediately when tracking is enabled
            getCurrentLocationAndSave()
            
        } catch (e: Exception) {
            _lastLocationStatus.value = "Failed to start location tracking: ${e.message}"
            Log.e("LocationViewModel", "Error starting location tracking", e)
        }
    }
    
    fun stopLocationTracking() {
        try {
            locationTrackingManager.stopPeriodicLocationTracking()
            _isTrackingEnabled.value = false
            _lastLocationStatus.value = "Location tracking stopped"
            Log.d("LocationViewModel", "Location tracking stopped")
        } catch (e: Exception) {
            _lastLocationStatus.value = "Failed to stop location tracking: ${e.message}"
            Log.e("LocationViewModel", "Error stopping location tracking", e)
        }
    }
    
    fun getCurrentLocationAndSave() {
        if (!hasLocationPermission()) {
            _lastLocationStatus.value = "Location permission not granted"
            return
        }
        
        locationManager.getCurrentLocation { location ->
            if (location != null) {
                viewModelScope.launch {
                    try {
                        val success = locationRepository.saveLocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy
                        )
                        
                        if (success) {
                            _lastLocationStatus.value = 
                                "Location saved: ${location.latitude}, ${location.longitude}"
                        } else {
                            _lastLocationStatus.value = "Failed to save location to Firestore"
                        }
                    } catch (e: Exception) {
                        _lastLocationStatus.value = "Error saving location: ${e.message}"
                        Log.e("LocationViewModel", "Error saving location", e)
                    }
                }
            } else {
                _lastLocationStatus.value = "Could not get current location"
            }
        }
    }
}
