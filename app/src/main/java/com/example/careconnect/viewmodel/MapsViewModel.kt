package com.example.careconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.api.OverpassPlace
import com.example.careconnect.repository.OverpassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {
    private val overpassRepository = OverpassRepository()

    private val _pharmacies = MutableStateFlow<List<OverpassPlace>>(emptyList())
    val pharmacies: StateFlow<List<OverpassPlace>> = _pharmacies.asStateFlow()

    private val _clinics = MutableStateFlow<List<OverpassPlace>>(emptyList())
    val clinics: StateFlow<List<OverpassPlace>> = _clinics.asStateFlow()

    private val _allPlaces = MutableStateFlow<List<OverpassPlace>>(emptyList())
    val allPlaces: StateFlow<List<OverpassPlace>> = _allPlaces.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedPlaceType = MutableStateFlow("all")
    val selectedPlaceType: StateFlow<String> = _selectedPlaceType.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Melbourne-specific coordinates
    private val _selectedMelbourneArea = MutableStateFlow<String?>(null)
    val selectedMelbourneArea: StateFlow<String?> = _selectedMelbourneArea.asStateFlow()

    private val _mapCenter = MutableStateFlow(Pair(-37.8136, 144.9631)) // Melbourne CBD default
    val mapCenter: StateFlow<Pair<Double, Double>> = _mapCenter.asStateFlow()

    private val _isGeocoding = MutableStateFlow(false)
    val isGeocoding: StateFlow<Boolean> = _isGeocoding.asStateFlow()

    fun searchSuburb(suburbName: String) {
        viewModelScope.launch {
            _isGeocoding.value = true
            _errorMessage.value = null

            try {
                val coordinates = overpassRepository.geocodeSuburb(suburbName)
                if (coordinates != null) {
                    _mapCenter.value = coordinates
                    _selectedMelbourneArea.value = suburbName
                    searchNearbyPlaces(coordinates.first, coordinates.second)
                } else {
                    _errorMessage.value = "Could not find suburb: $suburbName"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error searching for suburb: ${e.message}"
            } finally {
                _isGeocoding.value = false
            }
        }
    }

    fun searchNearbyPlaces(latitude: Double, longitude: Double, radius: Int = 5000) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val allHealthcare =
                    overpassRepository.getAllNearbyHealthcare(latitude, longitude, radius)
                _allPlaces.value = allHealthcare

                // Separate by type - include Australian-specific terms
                _pharmacies.value = allHealthcare.filter {
                    it.amenity in listOf("pharmacy") || it.tags?.get("shop") == "chemist"
                }
                _clinics.value = allHealthcare.filter {
                    it.amenity in listOf("clinic", "hospital", "doctors", "dentist") ||
                            it.tags?.get("healthcare") in listOf(
                        "clinic",
                        "hospital",
                        "doctor",
                        "centre",
                        "dentist"
                    )
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load nearby places: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Melbourne-specific area search functions
    fun searchMelbourneCBD() {
        _selectedMelbourneArea.value = "Melbourne CBD"
        _mapCenter.value = Pair(-37.8136, 144.9631)
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val places = overpassRepository.getMelbourneCBDHealthcare()
                updatePlacesData(places)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load Melbourne CBD healthcare: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchSouthYarra() {
        _selectedMelbourneArea.value = "South Yarra"
        _mapCenter.value = Pair(-37.8394, 144.9926)
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val places = overpassRepository.getSouthYarraHealthcare()
                updatePlacesData(places)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load South Yarra healthcare: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchStKilda() {
        _selectedMelbourneArea.value = "St Kilda"
        _mapCenter.value = Pair(-37.8676, 144.9803)
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val places = overpassRepository.getStKildaHealthcare()
                updatePlacesData(places)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load St Kilda healthcare: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchRichmond() {
        _selectedMelbourneArea.value = "Richmond"
        _mapCenter.value = Pair(-37.8197, 144.9917)
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val places = overpassRepository.getRichmondHealthcare()
                updatePlacesData(places)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load Richmond healthcare: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updatePlacesData(places: List<OverpassPlace>) {
        _allPlaces.value = places

        _pharmacies.value = places.filter {
            it.amenity == "pharmacy" || it.tags?.get("shop") == "chemist"
        }
        _clinics.value = places.filter {
            it.amenity in listOf("clinic", "hospital", "doctors", "dentist") ||
                    it.tags?.get("healthcare") in listOf(
                "clinic",
                "hospital",
                "doctor",
                "centre",
                "dentist"
            )
        }
    }

    fun setSelectedPlaceType(type: String) {
        _selectedPlaceType.value = type
    }

    fun getCurrentPlaces(): List<OverpassPlace> {
        return when (_selectedPlaceType.value) {
            "pharmacy" -> _pharmacies.value
            "clinic" -> _clinics.value
            else -> _allPlaces.value
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearMelbourneArea() {
        _selectedMelbourneArea.value = null
    }

    // Get Melbourne default coordinates
    fun getMelbourneCoordinates(): Pair<Double, Double> {
        return Pair(-37.8136, 144.9631) // Melbourne CBD
    }
}
