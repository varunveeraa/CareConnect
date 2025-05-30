package com.example.careconnect.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.database.User
import com.example.careconnect.firestore.ActualUser
import com.example.careconnect.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualUserProfileScreen(
    actualUser: ActualUser,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onNavigateBack: () -> Unit,
    onStartChat: ((String, String) -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = actualUser.fullName.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name and basic info
            Text(
                text = actualUser.fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = actualUser.email,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Basic stats (could be enhanced with real follower data)
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                ActualUserStatCard(
                    count = 0, // Could be enhanced with real data
                    label = "Followers"
                )
                ActualUserStatCard(
                    count = 0, // Could be enhanced with real data
                    label = "Following"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            // Temporarily always show buttons for testing
            if (true) { // Changed from: if (actualUser.uid != currentUser.email) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Connect button
                    Button(
                        onClick = {
                            // In a real app, you'd implement the follow logic here
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Connect")
                    }
                    
                    // Message button
                    if (onStartChat != null) {
                        Button(
                            onClick = {
                                onStartChat(actualUser.uid, actualUser.fullName)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Message",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Focus Areas
            if (actualUser.focusAreas.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Focus Areas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = actualUser.focusAreas.joinToString(", "))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Health Conditions
            if (actualUser.healthConditions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Health Conditions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = actualUser.healthConditions.joinToString(", "))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Additional info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Additional Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Date of Birth: ${actualUser.dateOfBirth}")
                    Text("Gender: ${actualUser.gender}")
                    if (actualUser.onboardingCompleted) {
                        Text("✓ Onboarding completed", color = Color.Green)
                    }
                }
            }
        }
    }
}

@Composable
fun ActualUserStatCard(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
