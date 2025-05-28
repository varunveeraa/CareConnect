package com.example.careconnect.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.example.careconnect.database.User
import com.example.careconnect.viewmodel.SocialViewModel
import com.example.careconnect.viewmodel.FirebaseAuthViewModel
import com.example.careconnect.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Browse : Screen("browse")
    object Patterns : Screen("patterns")
    object People : Screen("people")
    object Chat : Screen("chat")
    object Settings : Screen("settings")
    object FollowRequests : Screen("follow_requests")
    object FirestoreFollowRequests : Screen("firestore_follow_requests")
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: Int) = "user_profile/$userId"
    }
    object FirestoreUserProfile : Screen("firestore_user_profile/{uid}") {
        fun createRoute(uid: String) = "firestore_user_profile/$uid"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    currentUser: User,
    socialViewModel: SocialViewModel,
    authViewModel: FirebaseAuthViewModel,
    startDestination: String = Screen.Home.route,
    paddingValues: PaddingValues? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = paddingValues?.let { Modifier.padding(it) } ?: Modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        
        composable(Screen.Browse.route) {
            BrowseScreen()
        }
        
        composable(Screen.Patterns.route) {
            PatternsScreen()
        }
        
        composable(Screen.People.route) {
            PeopleScreen(
                socialViewModel = socialViewModel,
                currentUser = currentUser,
                onUserClick = { user ->
                    navController.navigate(Screen.UserProfile.createRoute(user.id))
                },
                onFirestoreUserClick = { firestoreUser ->
                    navController.navigate(Screen.FirestoreUserProfile.createRoute(firestoreUser.uid))
                },
                onActualUserClick = { actualUser ->
                    navController.navigate(Screen.FirestoreUserProfile.createRoute(actualUser.uid))
                },
                onNavigateToRequests = {
                    navController.navigate(Screen.FollowRequests.route)
                },
                onNavigateToFirestoreRequests = {
                    navController.navigate(Screen.FirestoreFollowRequests.route)
                }
            )
        }
        
        composable(Screen.Chat.route) {
            ChatScreen(currentUser = currentUser)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(authViewModel = authViewModel)
        }
        
        composable(Screen.FollowRequests.route) {
            FollowRequestsScreen(
                socialViewModel = socialViewModel,
                currentUser = currentUser,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.FirestoreFollowRequests.route) {
            FirestoreFollowRequestsScreen(
                socialViewModel = socialViewModel,
                currentUser = currentUser,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.UserProfile.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            LaunchedEffect(userId) {
                socialViewModel.getUserById(userId)
            }
            val user by socialViewModel.selectedUser.collectAsState()
            
            user?.let {
                UserProfileScreen(
                    user = it,
                    currentUser = currentUser,
                    socialViewModel = socialViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable(Screen.FirestoreUserProfile.route) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            LaunchedEffect(uid) {
                // Try to get as ActualUser first, fallback to FirestoreUser
                socialViewModel.getActualUserByUid(uid)
                socialViewModel.getFirestoreUserByUid(uid)
            }
            val actualUser by socialViewModel.selectedActualUser.collectAsState()
            val firestoreUser by socialViewModel.selectedFirestoreUser.collectAsState()
            
            when {
                actualUser != null -> {
                    ActualUserProfileScreen(
                        actualUser = actualUser!!,
                        currentUser = currentUser,
                        socialViewModel = socialViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                firestoreUser != null -> {
                    FirestoreUserProfileScreen(
                        firestoreUser = firestoreUser!!,
                        currentUser = currentUser,
                        socialViewModel = socialViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
