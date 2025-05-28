package com.example.careconnect.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.database.FollowRequest
import com.example.careconnect.database.User
import com.example.careconnect.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowRequestsScreen(
    socialViewModel: SocialViewModel,
    currentUser: User,
    onNavigateBack: () -> Unit
) {
    val pendingRequests by socialViewModel.getPendingRequests(currentUser.id).collectAsState(initial = emptyList())
    val userCache = remember { mutableMapOf<Int, User>() }

    // Load user details for each request
    LaunchedEffect(pendingRequests) {
        pendingRequests.forEach { request ->
            if (!userCache.containsKey(request.fromUserId)) {
                socialViewModel.getUserById(request.fromUserId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Follow Requests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (pendingRequests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending follow requests",
                            color = Color.Gray,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            } else {
                items(pendingRequests) { request ->
                    FollowRequestItem(
                        request = request,
                        socialViewModel = socialViewModel,
                        onAccept = { socialViewModel.acceptFollowRequest(request) },
                        onReject = { socialViewModel.rejectFollowRequest(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FollowRequestItem(
    request: FollowRequest,
    socialViewModel: SocialViewModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(request.fromUserId) {
        // Load user data using the ViewModel method
        socialViewModel.getUserById(request.fromUserId)
        android.util.Log.d("FollowRequestItem", "Loading user for request ID: ${request.fromUserId}")
    }
    
    // Listen to the selected user state to get the loaded user
    val selectedUser by socialViewModel.selectedUser.collectAsState()
    
    // Update user when selectedUser changes and matches our request
    LaunchedEffect(selectedUser) {
        if (selectedUser?.id == request.fromUserId) {
            user = selectedUser
            android.util.Log.d("FollowRequestItem", "User loaded: ${selectedUser?.fullName}")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user?.fullName?.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.fullName ?: "Loading...",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                user?.let { userData ->
                    Text(
                        text = userData.email,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    if (!userData.focusArea.isNullOrBlank()) {
                        Text(
                            text = "Focus: ${userData.focusArea}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    if (!userData.healthConditions.isNullOrBlank()) {
                        Text(
                            text = "Health: ${userData.healthConditions}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Text(
                    text = "Wants to follow you",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Action buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        android.util.Log.d("FollowRequestItem", "Accepting follow request from ${user?.fullName}")
                        onAccept()
                    },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Accept")
                }
                OutlinedButton(
                    onClick = {
                        android.util.Log.d("FollowRequestItem", "Rejecting follow request from ${user?.fullName}")
                        onReject()
                    },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Decline")
                }
            }
        }
    }
}
