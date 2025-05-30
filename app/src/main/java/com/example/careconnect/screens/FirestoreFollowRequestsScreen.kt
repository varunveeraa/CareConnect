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
import com.example.careconnect.firestore.FirestoreFollowRequest
import com.example.careconnect.database.User
import com.example.careconnect.viewmodel.SocialViewModel
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirestoreFollowRequestsScreen(
    socialViewModel: SocialViewModel,
    currentUser: User,
    onNavigateBack: () -> Unit
) {
    // Use Firebase Auth UID instead of email
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUserUid = firebaseAuth.currentUser?.uid ?: currentUser.email
    
    val pendingRequests by socialViewModel.firestorePendingRequests.collectAsState()
    
    // Load pending requests when screen opens
    LaunchedEffect(currentUserUid) {
        android.util.Log.d("FirestoreFollowRequests", "Loading requests for UID: '$currentUserUid'")
        socialViewModel.loadFirestorePendingRequests(currentUserUid)
    }
    
    // Debug current state
    LaunchedEffect(pendingRequests) {
        Log.d("FirestoreFollowRequests", "=== FOLLOW REQUESTS DEBUG ===")
        Log.d("FirestoreFollowRequests", "Current user: ${currentUser.fullName} (${currentUser.email})")
        Log.d("FirestoreFollowRequests", "Using UID for query: '$currentUserUid'")
        Log.d("FirestoreFollowRequests", "Pending requests count: ${pendingRequests.size}")
        pendingRequests.forEach { request ->
            Log.d("FirestoreFollowRequests", "Request: ${request.fromUserName} -> ${request.toUserName} (${request.toUserUid})")
        }
        Log.d("FirestoreFollowRequests", "=== END DEBUG ===")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Follow Requests (Firestore)") },
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
                            text = "No pending follow requests in Firestore",
                            color = Color.Gray,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            } else {
                items(pendingRequests) { request ->
                    FirestoreFollowRequestItem(
                        request = request,
                        socialViewModel = socialViewModel,
                        onAccept = { 
                            socialViewModel.acceptFirestoreFollowRequest(request)
                        },
                        onReject = { 
                            socialViewModel.rejectFirestoreFollowRequest(request)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FirestoreFollowRequestItem(
    request: FirestoreFollowRequest,
    socialViewModel: SocialViewModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
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
                        text = request.fromUserName.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.fromUserName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = "Wants to follow you",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Status: ${request.status}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            // Action buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        android.util.Log.d("FirestoreFollowRequest", "Accepting request from ${request.fromUserName}")
                        onAccept()
                    },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Accept")
                }
                OutlinedButton(
                    onClick = {
                        android.util.Log.d("FirestoreFollowRequest", "Rejecting request from ${request.fromUserName}")
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
