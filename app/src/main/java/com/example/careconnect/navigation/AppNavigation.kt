package com.example.careconnect.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.careconnect.database.User
import com.example.careconnect.screens.*
import com.example.careconnect.viewmodel.FirebaseAuthViewModel
import com.example.careconnect.viewmodel.SocialViewModel
import com.example.careconnect.viewmodel.DirectMessageViewModel
import com.example.careconnect.firestore.DirectMessage

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Browse : Screen("browse")
    object HealthTools : Screen("health_tools")
    object HealthDetailedView : Screen("health_detailed_view/{period}") {
        fun createRoute(period: String) = "health_detailed_view/$period"
    }
    object ManageReminders : Screen("manage_reminders")
    object Patterns : Screen("patterns")
    object People : Screen("people")
    object Chat : Screen("chat")
    object DirectMessages : Screen("direct_messages")
    object DirectMessageChat : Screen("direct_message_chat/{conversationId}") {
        fun createRoute(conversationId: String) = "direct_message_chat/$conversationId"
    }
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
    // Create shared DirectMessageViewModel for navigation use
    val directMessageViewModel: DirectMessageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = paddingValues?.let { Modifier.padding(it) } ?: Modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        
        composable(Screen.Browse.route) {
            HealthToolsScreen(
                onNavigateToDetailedView = { period ->
                    navController.navigate(Screen.HealthDetailedView.createRoute(period.name))
                },
                onNavigateToManageReminders = {
                    navController.navigate(Screen.ManageReminders.route)
                }
            )
        }
        
        composable(Screen.HealthTools.route) {
            HealthToolsScreen(
                onNavigateToDetailedView = { period ->
                    navController.navigate(Screen.HealthDetailedView.createRoute(period.name))
                },
                onNavigateToManageReminders = {
                    navController.navigate(Screen.ManageReminders.route)
                }
            )
        }
        
        composable(Screen.HealthDetailedView.route) { backStackEntry ->
            val periodName = backStackEntry.arguments?.getString("period") ?: "DAILY"
            val period = com.example.careconnect.health.MetricsPeriod.values().find { it.name == periodName } ?: com.example.careconnect.health.MetricsPeriod.DAILY
            
            HealthDetailedViewScreen(
                period = period,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ManageReminders.route) {
            ManageRemindersScreen(
                onNavigateBack = {
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
                onRawUserClick = { rawUser ->
                    // Add null safety check before navigation
                    if (rawUser.uid.isNotEmpty() && rawUser.fullName.isNotEmpty()) {
                        android.util.Log.d("PeopleScreen", "Raw user clicked: ${rawUser.fullName}")
                        navController.navigate(Screen.FollowerProfile.createRoute(rawUser.uid))
                    } else {
                        android.util.Log.e("PeopleScreen", "Invalid raw user data - uid: ${rawUser.uid}, name: ${rawUser.fullName}")
                    }
                },
                onFollowerUserClick = { userData ->
                    val uid = userData["uid"]?.toString() ?: ""
                    val fullName = userData["fullName"]?.toString() ?: ""
                    if (uid.isNotEmpty() && fullName.isNotEmpty()) {
                        android.util.Log.d("PeopleScreen", "Follower user clicked: $fullName")
                        socialViewModel.setTemporaryFollowerUser(userData)
                        navController.navigate(Screen.FollowerProfile.createRoute(uid))
                    } else {
                        android.util.Log.e("PeopleScreen", "Invalid follower user data - uid: $uid, name: $fullName")
                    }
                },
                onNavigateToRequests = {
                    navController.navigate(Screen.FollowRequests.route)
                },
                onNavigateToFirestoreRequests = {
                    navController.navigate(Screen.FirestoreFollowRequests.route)
                },
                onStartChat = { otherUserUid, otherUserName ->
                    // Start conversation and navigate directly to chat
                    android.util.Log.d("AppNavigation", "Starting chat with $otherUserName")
                    directMessageViewModel.startConversationWith(
                        otherUserId = otherUserUid,
                        otherUserName = otherUserName,
                        onConversationReady = { conversation ->
                            navController.navigate(Screen.DirectMessageChat.createRoute(conversation.id))
                        }
                    )
                }
            )
        }
        
        composable(Screen.Chat.route) {
            ChatScreen(currentUser = currentUser)
        }
        
        composable(Screen.DirectMessages.route) {
            DirectMessageListScreen(
                onConversationClick = { conversation ->
                    navController.navigate(Screen.DirectMessageChat.createRoute(conversation.id))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = directMessageViewModel
            )
        }
        
        composable(Screen.DirectMessageChat.route) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            
            var conversation by remember { mutableStateOf<DirectMessage?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(conversationId) {
                if (conversationId.isNotEmpty()) {
                    try {
                        conversation = directMessageViewModel.getConversationByIdWithRefresh(conversationId)
                        if (conversation == null) {
                            error = "Conversation not found"
                        }
                    } catch (e: Exception) {
                        error = "Error loading conversation: ${e.message}"
                    }
                } else {
                    error = "Invalid conversation ID"
                }
                isLoading = false
            }
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading conversation...")
                        }
                    }
                }
                conversation != null -> {
                    DirectMessageChatScreen(
                        conversation = conversation!!,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        viewModel = directMessageViewModel
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error ?: "Conversation not found",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    navController.navigate(Screen.DirectMessages.route) {
                                        popUpTo(Screen.DirectMessageChat.route) { inclusive = true }
                                    }
                                }
                            ) {
                                Text("Back to Messages")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    // Retry loading
                                    isLoading = true
                                    error = null
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
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
                    },
                    onStartChat = { otherUserUid, otherUserName ->
                        android.util.Log.d("AppNavigation", "UserProfile - Starting chat with $otherUserName")
                        directMessageViewModel.startConversationWith(
                            otherUserId = otherUserUid,
                            otherUserName = otherUserName,
                            onConversationReady = { conversation ->
                                navController.navigate(Screen.DirectMessageChat.createRoute(conversation.id))
                            }
                        )
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
                        },
                        onStartChat = { otherUserUid, otherUserName ->
                            android.util.Log.d("AppNavigation", "ActualUserProfile - Starting chat with $otherUserName")
                            directMessageViewModel.startConversationWith(
                                otherUserId = otherUserUid,
                                otherUserName = otherUserName,
                                onConversationReady = { conversation ->
                                    navController.navigate(Screen.DirectMessageChat.createRoute(conversation.id))
                                }
                            )
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
                        },
                        onStartChat = { otherUserUid, otherUserName ->
                            android.util.Log.d("AppNavigation", "FirestoreUserProfile - Starting chat with $otherUserName")
                            directMessageViewModel.startConversationWith(
                                otherUserId = otherUserUid,
                                otherUserName = otherUserName,
                                onConversationReady = { conversation ->
                                    navController.navigate(Screen.DirectMessageChat.createRoute(conversation.id))
                                }
                            )
                        }
                    )
                }
            }
        }
        
        composable(Screen.FollowerProfile.route) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val followerUser by socialViewModel.temporaryFollowerUser.collectAsState()
            
            if (followerUser != null && uid.isNotEmpty()) {
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
                    if (uid.isNotEmpty()) {
                        socialViewModel.getActualUserByUid(uid)
                        socialViewModel.getFirestoreUserByUid(uid)
                    }
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
                            },
                            onStartChat = { otherUserUid, otherUserName ->
                                android.util.Log.d("AppNavigation", "ActualUserProfile - Starting chat with $otherUserName")
                                directMessageViewModel.startConversationWith(
                                    otherUserId = otherUserUid,
                                    otherUserName = otherUserName,
                                    onConversationReady = { conversation ->
                                        navController.navigate(Screen.DirectMessageChat.createRoute(conversation.id))
                                    }
                                )
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
                            },
                            onStartChat = { otherUserUid, otherUserName ->
                                android.util.Log.d("AppNavigation", "FirestoreUserProfile - Starting chat with $otherUserName")
                                directMessageViewModel.startConversationWith(
                                    otherUserId = otherUserUid,
                                    otherUserName = otherUserName,
                                    onConversationReady = { conversation ->
                                        navController.navigate(Screen.DirectMessageChat.createRoute(conversation.id))
                                    }
                                )
                            }
                        )
                    }
                    else -> {
                        // Show loading or error state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uid.isEmpty()) {
                                Column {
                                    Text("Invalid user ID")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { navController.popBackStack() }
                                    ) {
                                        Text("Go Back")
                                    }
                                }
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
