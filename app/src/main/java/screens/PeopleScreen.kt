package com.example.careconnect.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.careconnect.firestore.FirestoreUser
import com.example.careconnect.firestore.ActualUser
import com.example.careconnect.viewmodel.SocialViewModel.RawFirestoreUser
import com.example.careconnect.viewmodel.SocialViewModel
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RawUserItem(
    rawUser: com.example.careconnect.viewmodel.SocialViewModel.RawFirestoreUser,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onRawUserClick: (com.example.careconnect.viewmodel.SocialViewModel.RawFirestoreUser) -> Unit
) {
    var followRequestStatus by remember { mutableStateOf<String?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    
    // Use the same Firebase Auth UID logic as PeopleScreen
    val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUserUid = firebaseAuth.currentUser?.uid ?: currentUser.email
    
    // Check current follow status using Firestore methods
    LaunchedEffect(rawUser.uid) {
        isFollowing = socialViewModel.isFollowingInFirestore(currentUserUid, rawUser.uid)
        val request = socialViewModel.getFirestoreRequestBetweenUsers(currentUserUid, rawUser.uid)
        followRequestStatus = request?.status
        
        android.util.Log.d("RawUserItem", "Status check - Following: $isFollowing, Request: $followRequestStatus")
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onRawUserClick(rawUser) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rawUser.fullName.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rawUser.fullName,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = rawUser.email,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (rawUser.focusAreas.isNotEmpty()) {
                    Text(
                        text = "Focus: ${rawUser.focusAreas.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Follow button with Firestore functionality
            when {
                isFollowing -> {
                    Button(
                        onClick = {
                            android.util.Log.d("RawUserItem", "Unfollowing ${rawUser.fullName} in Firestore")
                            socialViewModel.unfollowInFirestore(currentUserUid, rawUser.uid)
                            isFollowing = false
                        },
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
                        enabled = false
                    ) {
                        Text("Requested")
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            android.util.Log.d("RawUserItem", "Sending Firestore follow request to ${rawUser.fullName}")
                            socialViewModel.sendFollowRequestToFirestoreUser(
                                fromUserUid = currentUserUid,
                                fromUserName = currentUser.fullName,
                                firestoreUser = rawUser
                            )
                            followRequestStatus = "pending"
                        }
                    ) {
                        Text("Follow")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    socialViewModel: SocialViewModel,
    currentUser: User,
    onUserClick: (User) -> Unit,
    onFirestoreUserClick: (FirestoreUser) -> Unit = {},
    onActualUserClick: (ActualUser) -> Unit = {},
    onRawUserClick: (SocialViewModel.RawFirestoreUser) -> Unit = { rawUser ->
        android.util.Log.d("PeopleScreen", "Raw user clicked: ${rawUser.fullName}")
    },
    onNavigateToRequests: () -> Unit,
    onNavigateToFirestoreRequests: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedListType by remember { mutableStateOf("following") } // Default to "following"
    
    val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUserUid = firebaseAuth.currentUser?.uid ?: currentUser.email
    
    android.util.Log.d("PeopleScreen", "=== USER UID DEBUG ===")
    android.util.Log.d("PeopleScreen", "Firebase Auth UID: ${firebaseAuth.currentUser?.uid}")
    android.util.Log.d("PeopleScreen", "Using UID: $currentUserUid")
    android.util.Log.d("PeopleScreen", "Current user email: ${currentUser.email}")
    
    val searchResults by socialViewModel.searchResults.collectAsState()
    val isLoading by socialViewModel.isLoading.collectAsState()
    
    // Use Firestore data for followers and pending requests
    val firestoreFollowers by socialViewModel.firestoreFollowers.collectAsState()
    val firestoreFollowing by socialViewModel.firestoreFollowingUsers.collectAsState()
    val firestorePendingRequests by socialViewModel.firestorePendingRequests.collectAsState()
    
    LaunchedEffect(currentUserUid, selectedListType) { // Also trigger on selectedListType change for fresh data
        android.util.Log.d("PeopleScreen", "Loading Firestore data for UID: $currentUserUid, ListType: $selectedListType")
        socialViewModel.loadFirestoreFollowers(currentUserUid)
        socialViewModel.loadFirestoreFollowing(currentUserUid)
        socialViewModel.loadFirestorePendingRequests(currentUserUid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with follow requests button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "People",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (firestorePendingRequests.isNotEmpty()) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    IconButton(onClick = onNavigateToFirestoreRequests) {
                        Icon(Icons.Default.Notifications, contentDescription = "Follow Requests")
                    }
                    Text("${firestorePendingRequests.size}")
                }
            } else {
                IconButton(onClick = onNavigateToFirestoreRequests) {
                    Icon(Icons.Default.Notifications, contentDescription = "Follow Requests")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Unified Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                socialViewModel.searchUsers(it)
            },
            label = { Text("Search users or view followers...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Reduced height for search bar
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Content Area (either search results or followers list)
        if (searchQuery.isNotEmpty()) {
            // Show search results if query is not empty
            FirestoreSearchContent(
                searchQuery = searchQuery,
                searchResults = searchResults,
                isLoading = isLoading,
                currentUser = currentUser,
                socialViewModel = socialViewModel,
                onFirestoreUserClick = onFirestoreUserClick,
                onActualUserClick = onActualUserClick,
                onRawUserClick = onRawUserClick
            )
        } else {
            // Display Followers/Following Tabs (Following first)
            Column {
                TabRow(selectedTabIndex = if (selectedListType == "following") 0 else 1) {
                    Tab(
                        selected = selectedListType == "following",
                        onClick = { selectedListType = "following" },
                        text = { Text("Following (${firestoreFollowing.size})") }
                    )
                    Tab(
                        selected = selectedListType == "followers",
                        onClick = { selectedListType = "followers" },
                        text = { Text("Followers (${firestoreFollowers.size})") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                when (selectedListType) {
                    "following" -> {
                        LocalizedFollowingContent(
                            followingUsers = firestoreFollowing,
                            onUserClick = { userData ->
                                android.util.Log.d("PeopleScreen", "Clicked on following: ${userData["fullName"]}")
                            }
                        )
                    }
                    "followers" -> {
                        LocalizedFollowersContent(
                            followerUsers = firestoreFollowers,
                            onUserClick = { userData ->
                                android.util.Log.d("PeopleScreen", "Clicked on follower: ${userData["fullName"]}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<Any>,
    isLoading: Boolean,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onUserClick: (User) -> Unit,
    onFirestoreUserClick: (FirestoreUser) -> Unit
) {
    Column {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search users...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search results
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            searchQuery.isBlank() -> {
                Text(
                    text = "Start typing to search for users...",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            searchResults.isEmpty() -> {
                Text(
                    text = "No users found",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn {
                    items(searchResults.filter {
                        when (it) {
                            is User -> it.id != currentUser.id
                            is FirestoreUser -> it.uid != currentUser.email
                            else -> false
                        }
                    }) { result ->
                        when (result) {
                            is User -> {
                                UserItem(
                                    user = result,
                                    currentUser = currentUser,
                                    socialViewModel = socialViewModel,
                                    onUserClick = onUserClick
                                )
                            }
                            is FirestoreUser -> {
                                FirestoreUserItem(
                                    firestoreUser = result,
                                    currentUser = currentUser,
                                    socialViewModel = socialViewModel,
                                    onFirestoreUserClick = onFirestoreUserClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirestoreSearchContent(
    searchQuery: String,
    searchResults: List<Any>,
    isLoading: Boolean,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onFirestoreUserClick: (FirestoreUser) -> Unit,
    onActualUserClick: (ActualUser) -> Unit,
    onRawUserClick: (SocialViewModel.RawFirestoreUser) -> Unit
) {
    Column {
        // Search results
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            searchQuery.isBlank() -> {
                Text(
                    text = "Start typing to search for users by name...",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            searchResults.isEmpty() -> {
                Text(
                    text = "No users found",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn {
                    items(searchResults.filter { result ->
                        when (result) {
                            is FirestoreUser -> result.uid != currentUser.email
                            is ActualUser -> result.uid != currentUser.email
                            is com.example.careconnect.viewmodel.SocialViewModel.RawFirestoreUser -> result.uid != currentUser.email
                            else -> {
                                android.util.Log.d("PeopleScreen", "Unknown result type: ${result::class.simpleName}")
                                false
                            }
                        }
                    }) { result ->
                        android.util.Log.d("PeopleScreen", "Displaying result: ${result::class.simpleName}")
                        when (result) {
                            is FirestoreUser -> {
                                android.util.Log.d("PeopleScreen", "Showing FirestoreUser: ${result.fullName}")
                                FirestoreUserItem(
                                    firestoreUser = result,
                                    currentUser = currentUser,
                                    socialViewModel = socialViewModel,
                                    onFirestoreUserClick = onFirestoreUserClick
                                )
                            }
                            is ActualUser -> {
                                android.util.Log.d("PeopleScreen", "Showing ActualUser: ${result.fullName}")
                                ActualUserItem(
                                    actualUser = result,
                                    currentUser = currentUser,
                                    socialViewModel = socialViewModel,
                                    onActualUserClick = onActualUserClick
                                )
                            }
                            is com.example.careconnect.viewmodel.SocialViewModel.RawFirestoreUser -> {
                                android.util.Log.d("PeopleScreen", "Showing RawFirestoreUser: ${result.fullName}")
                                RawUserItem(
                                    rawUser = result,
                                    currentUser = currentUser,
                                    socialViewModel = socialViewModel,
                                    onRawUserClick = onRawUserClick
                                )
                            }
                            else -> {
                                android.util.Log.e("PeopleScreen", "Unhandled result type in display: ${result::class.simpleName}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FollowingContent(
    following: List<User>,
    onUserClick: (User) -> Unit
) {
    if (following.isEmpty()) {
        Text(
            text = "You're not following anyone yet",
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyColumn {
            items(following) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onUserClick(user) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile picture placeholder
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user.fullName.firstOrNull()?.toString() ?: "?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.fullName,
                                fontWeight = FontWeight.Medium
                            )
                            if (!user.focusArea.isNullOrBlank()) {
                                Text(
                                    text = user.focusArea,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View profile"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FirestoreUserItem(
    firestoreUser: FirestoreUser,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onFirestoreUserClick: (FirestoreUser) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onFirestoreUserClick(firestoreUser) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = firestoreUser.fullName.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = firestoreUser.fullName,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = firestoreUser.email,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (!firestoreUser.focusArea.isNullOrBlank()) {
                    Text(
                        text = "Focus: ${firestoreUser.focusArea}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Follow button (simplified for Firestore users)
            Button(
                onClick = {
                    // For now, just show that it would send a follow request
                    // In a real app, you'd need to map Firestore UID to local user ID
                }
            ) {
                Text("Connect")
            }
        }
    }
}

@Composable
fun ActualUserItem(
    actualUser: ActualUser,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onActualUserClick: (ActualUser) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onActualUserClick(actualUser) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = actualUser.fullName.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = actualUser.fullName,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = actualUser.email,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (actualUser.focusAreas.isNotEmpty()) {
                    Text(
                        text = "Focus: ${actualUser.focusAreas.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Follow button (simplified for Firestore users)
            Button(
                onClick = {
                    // For now, just show that it would send a follow request
                    // In a real app, you'd need to map Firestore UID to local user ID
                }
            ) {
                Text("Connect")
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    currentUser: User,
    socialViewModel: SocialViewModel,
    onUserClick: (User) -> Unit
) {
    var followRequestStatus by remember { mutableStateOf<String?>(null) }
    var isFollowing by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) {
        isFollowing = socialViewModel.isFollowing(currentUser.id, user.id)
        val request = socialViewModel.getRequestBetweenUsers(currentUser.id, user.id)
        followRequestStatus = request?.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onUserClick(user) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.fullName.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (!user.focusArea.isNullOrBlank()) {
                    Text(
                        text = "Focus: ${user.focusArea}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Follow button
            when {
                isFollowing -> {
                    Button(
                        onClick = {
                            socialViewModel.unfollowUser(currentUser.id, user.id)
                            isFollowing = false
                        },
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
                        enabled = false
                    ) {
                        Text("Requested")
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            socialViewModel.sendFollowRequest(currentUser.id, user.id)
                            followRequestStatus = "pending"
                        }
                    ) {
                        Text("Follow")
                    }
                }
            }
        }
    }
}

@Composable
fun LocalizedFollowersContent(
    followerUsers: List<Map<String, Any>>,
    onUserClick: (Map<String, Any>) -> Unit
) {
    if (followerUsers.isEmpty()) {
        Text(
            text = "You don't have any followers yet",
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyColumn {
            items(followerUsers) { userData ->
                val fullName = userData["fullName"]?.toString() ?: "Unknown User"
                val focusAreas = userData["focusAreas"] as? List<String>

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onUserClick(userData) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile picture placeholder
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = fullName.firstOrNull()?.toString() ?: "?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fullName,
                                fontWeight = FontWeight.Medium
                            )
                            
                            // Show focus areas if available
                            if (!focusAreas.isNullOrEmpty()) {
                                Text(
                                    text = "Focus: ${focusAreas.joinToString(", ")}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View profile"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocalizedFollowingContent(
    followingUsers: List<Map<String, Any>>,
    onUserClick: (Map<String, Any>) -> Unit
) {
    if (followingUsers.isEmpty()) {
        Text(
            text = "You're not following anyone yet",
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyColumn {
            items(followingUsers) { userData ->
                val fullName = userData["fullName"]?.toString() ?: "Unknown User"
                val focusAreas = userData["focusAreas"] as? List<String>

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onUserClick(userData) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer // Different color for following
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = fullName.firstOrNull()?.toString() ?: "?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fullName,
                                fontWeight = FontWeight.Medium
                            )
                            
                            if (!focusAreas.isNullOrEmpty()) {
                                Text(
                                    text = "Focus: ${focusAreas.joinToString(", ")}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View profile"
                        )
                    }
                }
            }
        }
    }
}
