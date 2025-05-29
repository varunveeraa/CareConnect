package com.example.careconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.database.AppDatabase
import com.example.careconnect.screens.MainAppScreen
import com.example.careconnect.screens.AuthScreen
import com.example.careconnect.screens.OnboardingScreen
import com.example.careconnect.repository.SocialRepository
import com.example.careconnect.ui.theme.CareConnectTheme
import com.example.careconnect.viewmodel.FirebaseAuthViewModel
import com.example.careconnect.viewmodel.FirebaseAuthState
import com.example.careconnect.viewmodel.SocialViewModel
import com.example.careconnect.viewmodel.SocialViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareConnectTheme {
                CareConnectApp()
            }
        }
    }
}

@Composable
fun CareConnectApp() {
    val authViewModel: FirebaseAuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser

    Surface(modifier = Modifier.fillMaxSize()) {
        when (authState) {
            is FirebaseAuthState.Loading -> {
                // Show loading screen while checking authentication
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is FirebaseAuthState.Authenticated -> {
                // User is signed in and onboarded, show main app with navigation
                val database = AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current)
                
                val socialRepository = SocialRepository(
                    userDao = database.userDao(),
                    followRequestDao = database.followRequestDao(),
                    followingDao = database.followingDao()
                )
                val socialViewModel: SocialViewModel = viewModel(
                    factory = SocialViewModelFactory(socialRepository)
                )
                
                // For now, we'll use a mock current user. In a real app, you'd get this from your auth system
                val currentUser = remember { 
                    com.example.careconnect.database.User(
                        id = 1,
                        fullName = "Current User",
                        email = currentUser?.uid ?: "user@example.com", // This should be the actual Firebase UID in a real app
                        password = "",
                        dateOfBirth = "",
                        gender = "",
                        isLoggedIn = true
                    )
                }
                
                MainAppScreen(
                    currentUser = currentUser,
                    socialViewModel = socialViewModel,
                    authViewModel = authViewModel
                )
            }
            is FirebaseAuthState.NeedsOnboarding -> {
                // User is signed in but needs onboarding
                OnboardingScreen(
                    onOnboardingComplete = {
                        authViewModel.onboardingCompleted()
                    }
                )
            }
            is FirebaseAuthState.Unauthenticated, is FirebaseAuthState.Error -> {
                // User is not signed in, show auth screen
                AuthScreen(
                    authViewModel = authViewModel,
                    onAuthSuccess = {
                        // Navigation will be handled automatically by the ViewModel
                    }
                )
            }
        }
    }
}
