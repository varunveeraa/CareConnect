package com.example.careconnect.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.careconnect.database.User
import com.example.careconnect.firestore.FirestoreUser
import com.example.careconnect.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirestoreUserProfileScreen(
    firestoreUser: FirestoreUser,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onNavigateBack: () -> Unit
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
                        text = firestoreUser.fullName.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name and basic info
            Text(
                text = firestoreUser.fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = firestoreUser.email,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Follower/Following stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                StatCard(
                    count = firestoreUser.followersCount,
                    label = "Followers"
                )
                StatCard(
                    count = firestoreUser.followingCount,
                    label = "Following"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Connect button (simplified for Firestore users)
            if (firestoreUser.uid != currentUser.email) {
                Button(
                    onClick = {
                        // In a real app, you'd implement the follow logic here
                        // This would require mapping between Firestore and local user systems
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connect")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bio
            if (!firestoreUser.bio.isNullOrBlank()) {
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
                        Text(text = firestoreUser.bio)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Focus Area
            if (!firestoreUser.focusArea.isNullOrBlank()) {
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
                        Text(text = firestoreUser.focusArea)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Health Conditions
            if (!firestoreUser.healthConditions.isNullOrBlank()) {
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
                        Text(text = firestoreUser.healthConditions)
                    }
                }
            }
        }
    }
}
