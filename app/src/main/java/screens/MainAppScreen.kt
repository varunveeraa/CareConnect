package com.example.careconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.careconnect.database.User
import com.example.careconnect.navigation.AppNavigation
import com.example.careconnect.navigation.Screen
import com.example.careconnect.viewmodel.SocialViewModel
import com.example.careconnect.viewmodel.FirebaseAuthViewModel
import com.example.careconnect.viewmodel.DirectMessageViewModel

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    currentUser: User,
    socialViewModel: SocialViewModel,
    authViewModel: FirebaseAuthViewModel,
    onNavigateToUserChats: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val directMessageViewModel: DirectMessageViewModel = viewModel()
    val totalUnreadCount by directMessageViewModel.totalUnreadCount.collectAsState()
    
    // Ensure ViewModel is initialized
    LaunchedEffect(Unit) {
        directMessageViewModel.ensureInitialized()
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, Icons.Default.Home, "HOME"),
        BottomNavItem(Screen.Browse.route, Icons.Default.Build, "TOOLS"),
        BottomNavItem(Screen.Chat.route, Icons.AutoMirrored.Filled.Chat, "CHAT"),
        BottomNavItem(Screen.People.route, Icons.Default.People, "PEOPLE"),
        BottomNavItem(Screen.Settings.route, Icons.Default.Person, "ACCOUNT")
    )

    Scaffold(
        bottomBar = {
            // Only show bottom bar on main screens
            if (currentRoute in bottomNavItems.map { it.route }) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination to avoid building up a large stack
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Show floating chat button on all main screens except the chat screen
            if (currentRoute in bottomNavItems.map { it.route } && currentRoute != Screen.Chat.route) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.DirectMessages.route)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Messages",
                            tint = Color.White
                        )
                        
                        // Unread count badge
                        if (totalUnreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (totalUnreadCount > 99) "99+" else totalUnreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        AppNavigation(
            navController = navController,
            currentUser = currentUser,
            socialViewModel = socialViewModel,
            authViewModel = authViewModel,
            paddingValues = paddingValues
        )
    }
}
