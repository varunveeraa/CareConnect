package com.example.careconnect.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.viewmodel.FirebaseAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: FirebaseAuthViewModel = viewModel()
) {
    var accessibilityEnabled by remember { mutableStateOf(true) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Settings",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider()

        SettingsRow("Account") { /* TODO: Navigate to Account */ }
        SettingsRow("Notifications") { /* TODO: Navigate to Notifications */ }
        SettingsRow("Reminders") { /* TODO: Navigate to Reminders */ }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Accessibility", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Switch(
                checked = accessibilityEnabled,
                onCheckedChange = { accessibilityEnabled = it }
            )
        }

        HorizontalDivider()

        SettingsRow("Help") { /* TODO: Navigate to Help */ }
        
        HorizontalDivider()
        
        // Sign Out Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSignOutDialog = true }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sign Out", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp, 
                contentDescription = "Sign Out",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
    
    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.logout()
                        showSignOutDialog = false
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Go", modifier = Modifier.size(18.dp))
    }
}
