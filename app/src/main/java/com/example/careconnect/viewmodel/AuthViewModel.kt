package com.example.careconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.database.AppDatabase
import com.example.careconnect.database.User
import com.example.careconnect.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(database.userDao())
    }
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            val isLoggedIn = repository.isUserLoggedIn()
            _authState.value = if (isLoggedIn) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
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
            _signUpState.value = SignUpState.Loading
            
            val user = User(
                fullName = fullName,
                email = email,
                password = password, // In production, hash this
                dateOfBirth = dateOfBirth,
                gender = gender
            )
            
            val success = repository.registerUser(user)
            if (success) {
                // Auto login after successful registration
                val loginSuccess = repository.loginUser(email, password)
                if (loginSuccess) {
                    _signUpState.value = SignUpState.Success
                    _authState.value = AuthState.Authenticated
                } else {
                    _signUpState.value = SignUpState.Error("Registration successful but auto-login failed")
                }
            } else {
                _signUpState.value = SignUpState.Error("Registration failed")
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val success = repository.loginUser(email, password)
                if (success) {
                    _authState.value = AuthState.Authenticated
                } else {
                    // Login failed, but keep current state as unauthenticated
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            // Get current user and logout
            repository.getLoggedInUser().collect { user ->
                user?.let {
                    repository.logoutUser(it.id)
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
}
