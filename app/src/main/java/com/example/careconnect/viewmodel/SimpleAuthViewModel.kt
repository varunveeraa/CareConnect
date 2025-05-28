package com.example.careconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SimpleAuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _authState = MutableStateFlow<SimpleAuthState>(SimpleAuthState.Unauthenticated)
    val authState: StateFlow<SimpleAuthState> = _authState.asStateFlow()
    
    private val _signUpState = MutableStateFlow<SimpleSignUpState>(SimpleSignUpState.Idle)
    val signUpState: StateFlow<SimpleSignUpState> = _signUpState.asStateFlow()
    
    private val _forgotPasswordState = MutableStateFlow<SimpleForgotPasswordState>(SimpleForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<SimpleForgotPasswordState> = _forgotPasswordState.asStateFlow()
    
    fun signUp(
        fullName: String,
        email: String,
        password: String,
        dateOfBirth: String,
        gender: String
    ) {
        viewModelScope.launch {
            try {
                _signUpState.value = SimpleSignUpState.Loading
                
                // Simulate network delay
                delay(1000)
                
                // Simple validation
                if (email.isNotBlank() && password.length >= 6) {
                    _signUpState.value = SimpleSignUpState.Success
                    _authState.value = SimpleAuthState.Authenticated("test_uid")
                } else {
                    _signUpState.value = SimpleSignUpState.Error("Invalid email or password too short")
                }
            } catch (e: Exception) {
                _signUpState.value = SimpleSignUpState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Simulate network delay
                delay(1000)
                
                if (email.isNotBlank() && password.isNotBlank()) {
                    _authState.value = SimpleAuthState.Authenticated("test_uid")
                } else {
                    _authState.value = SimpleAuthState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _authState.value = SimpleAuthState.Error(e.message ?: "Login failed")
            }
        }
    }
    
    fun logout() {
        _authState.value = SimpleAuthState.Unauthenticated
    }
    
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                _forgotPasswordState.value = SimpleForgotPasswordState.Loading
                delay(1000) // Simulate network delay
                _forgotPasswordState.value = SimpleForgotPasswordState.Success
            } catch (e: Exception) {
                _forgotPasswordState.value = SimpleForgotPasswordState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }
    
    fun resetForgotPasswordState() {
        _forgotPasswordState.value = SimpleForgotPasswordState.Idle
    }
    
    fun resetSignUpState() {
        _signUpState.value = SimpleSignUpState.Idle
    }
    
    fun resetAuthError() {
        if (_authState.value is SimpleAuthState.Error) {
            _authState.value = SimpleAuthState.Unauthenticated
        }
    }
}

sealed class SimpleAuthState {
    object Loading : SimpleAuthState()
    data class Authenticated(val uid: String) : SimpleAuthState()
    object Unauthenticated : SimpleAuthState()
    data class Error(val message: String) : SimpleAuthState()
}

sealed class SimpleSignUpState {
    object Idle : SimpleSignUpState()
    object Loading : SimpleSignUpState()
    object Success : SimpleSignUpState()
    data class Error(val message: String) : SimpleSignUpState()
}

sealed class SimpleForgotPasswordState {
    object Idle : SimpleForgotPasswordState()
    object Loading : SimpleForgotPasswordState()
    object Success : SimpleForgotPasswordState()
    data class Error(val message: String) : SimpleForgotPasswordState()
}