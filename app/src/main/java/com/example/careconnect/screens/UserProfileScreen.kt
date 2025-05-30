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
import com.example.careconnect.database.User
import com.example.careconnect.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: User,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onNavigateBack: () -> Unit,
    onStartChat: ((String, String) -> Unit)? = null
) {
    var isFollowing by remember { mutableStateOf(false) }
    var followRequestStatus by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(user.id) {
        isFollowing = socialViewModel.isFollowing(currentUser.id, user.id)
        val request = socialViewModel.getRequestBetweenUsers(currentUser.id, user.id)
        followRequestStatus = request?.status
    }

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
                        text = user.fullName.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name and basic info
            Text(
                text = user.fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Follower/Following stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                StatCard(
                    count = user.followersCount,
                    label = "Followers"
                )
                StatCard(
                    count = user.followingCount,
                    label = "Following"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons for other users
            if (user.id != currentUser.id) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Follow button
                    when {
                        isFollowing -> {
                            Button(
                                onClick = {
                                    socialViewModel.unfollowUser(currentUser.id, user.id)
                                    isFollowing = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                )
                            ) {
                                Text("Following")
                            }
                        }
                        followRequestStatus == "pending" -> {
                            Button(
                                onClick = { /* Already sent */ },
                                enabled = false,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Request Sent")
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    socialViewModel.sendFollowRequest(currentUser.id, user.id)
                                    followRequestStatus = "pending"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Follow")
                            }
                        }
                    }
                    
                    // Message button
                    if (onStartChat != null) {
                        Button(
                            onClick = {
                                onStartChat(user.email, user.fullName)
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

            // Bio
            if (!user.bio.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "About",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = user.bio)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Focus Area
            if (!user.focusArea.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Focus Area",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = user.focusArea)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Health Conditions
            if (!user.healthConditions.isNullOrBlank()) {
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
                        Text(text = user.healthConditions)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(count: Int, label: String) {
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
