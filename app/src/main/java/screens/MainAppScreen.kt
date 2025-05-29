package com.example.careconnect.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.careconnect.database.User
import com.example.careconnect.navigation.AppNavigation
import com.example.careconnect.navigation.Screen
import com.example.careconnect.viewmodel.SocialViewModel
import com.example.careconnect.viewmodel.FirebaseAuthViewModel

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
    authViewModel: FirebaseAuthViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
