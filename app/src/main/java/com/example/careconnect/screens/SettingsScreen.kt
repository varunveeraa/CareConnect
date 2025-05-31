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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.ui.components.AppBackground
import com.example.careconnect.viewmodel.FirebaseAuthViewModel
import com.example.careconnect.viewmodel.HealthViewModel
import com.example.careconnect.health.WearableType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: FirebaseAuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val healthViewModel: HealthViewModel = remember { HealthViewModel(context) }
    
    var accessibilityEnabled by remember { mutableStateOf(true) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showWearableDialog by remember { mutableStateOf(false) }

    val isConnected by healthViewModel.isConnected.collectAsState()
    val connectedWearable by healthViewModel.connectedWearable.collectAsState()
    val healthMetrics by healthViewModel.healthMetrics.collectAsState()
    val connectionStatus by healthViewModel.connectionStatus.collectAsState()

    AppBackground {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider()

            SettingsRow("Profile") { /* TODO: Navigate to Profile */ }
            SettingsRow("Notifications") { /* TODO: Navigate to Notifications */ }
            SettingsRow("Reminders") { /* TODO: Navigate to Reminders */ }

            // Wearable Connection Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWearableDialog = true }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Connect Wearable", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = if (isConnected) "Connected to ${connectedWearable?.displayName}" else "Not connected",
                        fontSize = 12.sp,
                        color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (isConnected) Icons.Default.Watch else Icons.Default.WatchOff,
                    contentDescription = "Wearable Status",
                    modifier = Modifier.size(18.dp),
                    tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Health Metrics Section (only show if connected)
            if (isConnected && healthMetrics != null) {
                HorizontalDivider()
                Text(
                    "Health data syncing in background...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

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
    }
    
    // Wearable Connection Dialog
    if (showWearableDialog) {
        WearableConnectionDialog(
            healthViewModel = healthViewModel,
            onDismiss = { 
                showWearableDialog = false
                healthViewModel.clearConnectionStatus()
            }
        )
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
    
    // Show connection status
    if (connectionStatus.isNotEmpty()) {
        LaunchedEffect(connectionStatus) {
            kotlinx.coroutines.delay(3000)
            healthViewModel.clearConnectionStatus()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearableConnectionDialog(
    healthViewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val isConnected by healthViewModel.isConnected.collectAsState()
    val connectedWearable by healthViewModel.connectedWearable.collectAsState()
    val isLoading by healthViewModel.isLoading.collectAsState()
    val connectionStatus by healthViewModel.connectionStatus.collectAsState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wearable Connection") },
        text = {
            Column {
                if (isConnected) {
                    Text("Currently connected to: ${connectedWearable?.displayName}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { healthViewModel.disconnectWearable() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Disconnect")
                    }
                } else {
                    Text("Select a wearable device to connect:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    healthViewModel.getAvailableWearables().forEach { wearable ->
                        Button(
                            onClick = { healthViewModel.connectWearable(wearable) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(wearable.displayName)
                        }
                    }
                }
                
                if (connectionStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        connectionStatus,
                        color = if (connectionStatus.contains("Connected") || connectionStatus.contains("synced")) 
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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
