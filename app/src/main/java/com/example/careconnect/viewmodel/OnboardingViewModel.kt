package com.example.careconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _onboardingState = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()
    
    fun completeOnboarding(
        healthConditions: List<String>,
        focusAreas: List<String>
    ) {
        viewModelScope.launch {
            try {
                _onboardingState.value = OnboardingState.Loading
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val onboardingData = hashMapOf(
                        "healthConditions" to healthConditions,
                        "focusAreas" to focusAreas,
                        "onboardingCompleted" to true,
                        "onboardingCompletedAt" to System.currentTimeMillis()
                    )
                    
                    // Update user document with onboarding data
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update(onboardingData as Map<String, Any>)
                        .await()
                    
                    _onboardingState.value = OnboardingState.Success
                } else {
                    _onboardingState.value = OnboardingState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _onboardingState.value = OnboardingState.Error(e.message ?: "Failed to complete onboarding")
            }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            try {
                _onboardingState.value = OnboardingState.Loading

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val onboardingData = hashMapOf(
                        "healthConditions" to emptyList<String>(),
                        "focusAreas" to emptyList<String>(),
                        "onboardingCompleted" to false, // Mark as incomplete so it shows again
                        "onboardingSkipped" to true,
                        "onboardingSkippedAt" to System.currentTimeMillis()
                    )

                    // Update user document with skipped onboarding data
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update(onboardingData as Map<String, Any>)
                        .await()

                    _onboardingState.value = OnboardingState.Success
                } else {
                    _onboardingState.value = OnboardingState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _onboardingState.value =
                    OnboardingState.Error(e.message ?: "Failed to skip onboarding")
            }
        }
    }

    fun resetOnboardingState() {
        _onboardingState.value = OnboardingState.Idle
    }
}

sealed class OnboardingState {
    object Idle : OnboardingState()
    object Loading : OnboardingState()
    object Success : OnboardingState()
    data class Error(val message: String) : OnboardingState()
}
