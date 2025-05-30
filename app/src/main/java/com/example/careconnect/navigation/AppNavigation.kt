package com.example.careconnect.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.careconnect.database.User
import com.example.careconnect.screens.*
import com.example.careconnect.viewmodel.FirebaseAuthViewModel
import com.example.careconnect.viewmodel.NewsViewModel
import com.example.careconnect.viewmodel.SocialViewModel
import com.example.careconnect.health.MetricsPeriod

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Browse : Screen("browse")
    object HealthTools : Screen("health_tools")
    object HealthDetailedView : Screen("health_detailed_view/{period}") {
        fun createRoute(period: String) = "health_detailed_view/$period"
    }
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
    object FollowerProfile : Screen("follower_profile/{uid}") {
        fun createRoute(uid: String) = "follower_profile/$uid"
    }
    object ArticleDetail : Screen("article_detail")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    currentUser: User,
    socialViewModel: SocialViewModel,
    authViewModel: FirebaseAuthViewModel,
    newsViewModel: NewsViewModel,
    startDestination: String = Screen.Home.route,
    paddingValues: PaddingValues? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = paddingValues?.let { Modifier.padding(it) } ?: Modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                currentUser = currentUser,
                onArticleClick = { article ->
                    newsViewModel.selectArticle(article)
                    navController.navigate(Screen.ArticleDetail.route)
                }
            )
        }
        
        composable(Screen.Browse.route) {
            HealthToolsScreen(
                onNavigateToDetailedView = { period ->
                    navController.navigate(Screen.HealthDetailedView.createRoute(period.name))
                }
            )
        }
        
        composable(Screen.HealthTools.route) {
            HealthToolsScreen(
                onNavigateToDetailedView = { period ->
                    navController.navigate(Screen.HealthDetailedView.createRoute(period.name))
                }
            )
        }
        
        composable(Screen.HealthDetailedView.route) { backStackEntry ->
            val periodName = backStackEntry.arguments?.getString("period") ?: "DAILY"
            val period = MetricsPeriod.values().find { it.name == periodName } ?: MetricsPeriod.DAILY
            
            HealthDetailedViewScreen(
                period = period,
                onBackClick = {
                    navController.popBackStack()
                }
            )
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
                onFollowerUserClick = { userData ->
                    val uid = userData["uid"]?.toString() ?: ""
                    if (uid.isNotEmpty()) {
                        socialViewModel.setTemporaryFollowerUser(userData)
                        navController.navigate(Screen.FollowerProfile.createRoute(uid))
                    }
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
        
        composable(Screen.FollowerProfile.route) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val followerUser by socialViewModel.temporaryFollowerUser.collectAsState()
            
            if (followerUser != null) {
                FollowerUserProfileScreen(
                    userData = followerUser!!,
                    currentUser = currentUser,
                    socialViewModel = socialViewModel,
                    onNavigateBack = {
                        socialViewModel.clearTemporaryFollowerUser()
                        navController.popBackStack()
                    }
                )
            } else {
                // Fallback to existing logic if user data is not available
                LaunchedEffect(uid) {
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
                    else -> {
                        // Show loading or error state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        composable(Screen.ArticleDetail.route) {
            ArticleDetailScreen(
                newsViewModel = newsViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
