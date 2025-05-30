package com.example.careconnect.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        isVisible = true
        delay(3000) // Show splash for 3 seconds
        onNavigateToAuth()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image with blur
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("android.resource://com.example.careconnect/drawable/app_background_img")
                .crossfade(true)
                .build(),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 8.dp),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay for better contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Center content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            Image(
                painter = painterResource(id = com.example.careconnect.R.drawable.ic_launcher_foreground),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer(alpha = alpha),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Name
            Text(
                text = "CareConnect",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            Text(
                text = "Connecting Care with Compassion",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
        }
    }
}
