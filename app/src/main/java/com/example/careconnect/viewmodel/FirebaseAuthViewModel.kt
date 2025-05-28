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

class FirebaseAuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _authState = MutableStateFlow<FirebaseAuthState>(FirebaseAuthState.Loading)
    val authState: StateFlow<FirebaseAuthState> = _authState.asStateFlow()
    
    private val _signUpState = MutableStateFlow<FirebaseSignUpState>(FirebaseSignUpState.Idle)
    val signUpState: StateFlow<FirebaseSignUpState> = _signUpState.asStateFlow()
    
    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Check if user has completed onboarding
                try {
                    val userDoc = firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()
                    
                    val onboardingCompleted = userDoc.getBoolean("onboardingCompleted") ?: false
                    
                    _authState.value = if (onboardingCompleted) {
                        FirebaseAuthState.Authenticated(currentUser.uid)
                    } else {
                        FirebaseAuthState.NeedsOnboarding(currentUser.uid)
                    }
                } catch (e: Exception) {
                    // If we can't check onboarding status, assume they need onboarding
                    _authState.value = FirebaseAuthState.NeedsOnboarding(currentUser.uid)
                }
            } else {
                _authState.value = FirebaseAuthState.Unauthenticated
            }
        }
    }
    
    fun signUp(
        fullName: String,
        email: String,
        password: String,
        dateOfBirth: String,
        gender: String
    ) {
        viewModelScope.launch {
            try {
                _signUpState.value = FirebaseSignUpState.Loading
                
                // Create user with Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                
                if (user != null) {
                    // Store additional user data in Firestore
                    val userData = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "dateOfBirth" to dateOfBirth,
                        "gender" to gender,
                        "uid" to user.uid,
                        "createdAt" to System.currentTimeMillis(),
                        "onboardingCompleted" to false
                    )
                    
                    firestore.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .await()
                    
                    _signUpState.value = FirebaseSignUpState.Success
                    _authState.value = FirebaseAuthState.NeedsOnboarding(user.uid)
                } else {
                    _signUpState.value = FirebaseSignUpState.Error("User creation failed")
                }
            } catch (e: Exception) {
                _signUpState.value = FirebaseSignUpState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                
                if (user != null) {
                    // Check onboarding status after login
                    try {
                        val userDoc = firestore.collection("users")
                            .document(user.uid)
                            .get()
                            .await()
                        
                        val onboardingCompleted = userDoc.getBoolean("onboardingCompleted") ?: false
                        
                        _authState.value = if (onboardingCompleted) {
                            FirebaseAuthState.Authenticated(user.uid)
                        } else {
                            FirebaseAuthState.NeedsOnboarding(user.uid)
                        }
                    } catch (e: Exception) {
                        // If we can't check onboarding status, assume they need onboarding
                        _authState.value = FirebaseAuthState.NeedsOnboarding(user.uid)
                    }
                } else {
                    _authState.value = FirebaseAuthState.Error("Login failed")
                }
            } catch (e: Exception) {
                _authState.value = FirebaseAuthState.Error(e.message ?: "Login failed")
            }
        }
    }
    
    fun logout() {
        auth.signOut()
        _authState.value = FirebaseAuthState.Unauthenticated
    }
    
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                _forgotPasswordState.value = ForgotPasswordState.Loading
                auth.sendPasswordResetEmail(email).await()
                _forgotPasswordState.value = ForgotPasswordState.Success
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }
    
    fun onboardingCompleted() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = FirebaseAuthState.Authenticated(currentUser.uid)
        }
    }
    
    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
    
    fun resetSignUpState() {
        _signUpState.value = FirebaseSignUpState.Idle
    }
    
    fun resetAuthError() {
        if (_authState.value is FirebaseAuthState.Error) {
            _authState.value = FirebaseAuthState.Unauthenticated
        }
    }
}

sealed class FirebaseAuthState {
    object Loading : FirebaseAuthState()
    data class Authenticated(val uid: String) : FirebaseAuthState()
    data class NeedsOnboarding(val uid: String) : FirebaseAuthState()
    object Unauthenticated : FirebaseAuthState()
    data class Error(val message: String) : FirebaseAuthState()
}

sealed class FirebaseSignUpState {
    object Idle : FirebaseSignUpState()
    object Loading : FirebaseSignUpState()
    object Success : FirebaseSignUpState()
    data class Error(val message: String) : FirebaseSignUpState()
}

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}
