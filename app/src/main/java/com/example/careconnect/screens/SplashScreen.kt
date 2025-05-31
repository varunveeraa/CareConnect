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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit
) {
    var logoVisible by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "logoAlpha"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = if (taglineVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "taglineAlpha"
    )

    LaunchedEffect(Unit) {
        delay(500) // Initial delay
        logoVisible = true
        delay(600) // Stagger the tagline
        taglineVisible = true
        delay(2000) // Show for remaining time
        onNavigateToAuth()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image with blur effect
        Image(
            painter = painterResource(id = com.example.careconnect.R.drawable.app_background_img),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 10.dp),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // Center content with animations
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon with fade-in animation
            Image(
                painter = painterResource(id = com.example.careconnect.R.drawable.app_icon),
                contentDescription = "CareConnect App Icon",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer(
                        alpha = logoAlpha,
                        scaleX = 0.8f + (logoAlpha * 0.2f),
                        scaleY = 0.8f + (logoAlpha * 0.2f)
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name with elegant typography
            Text(
                text = "CareConnect",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(
                    alpha = logoAlpha,
                    translationY = 20f * (1f - logoAlpha)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline with soft, elegant typography
            Text(
                text = "Connecting Care with Compassion",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
                modifier = Modifier.graphicsLayer(
                    alpha = taglineAlpha,
                    translationY = 15f * (1f - taglineAlpha)
                )
            )
        }
    }
}
