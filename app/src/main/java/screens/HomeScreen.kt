package com.example.careconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Single row with all four items in one box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricCard(
                value = "1000",
                label = "Steps",
                icon = Icons.Default.DirectionsRun,
                iconColor = Color.Blue
            )
            MetricCard(
                value = "70",
                label = "Heart Rate",
                icon = Icons.Default.Favorite,
                iconColor = Color.Red
            )
            MetricCard(
                value = "7",
                label = "Sleep",
                icon = Icons.Default.Nightlight,
                iconColor = Color.Gray
            )
            MetricCard(
                value = "250",
                label = "Calories",
                icon = Icons.Default.LocalFireDepartment,
                iconColor = Color(0xFF9C27B0) // Purple color
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Reminders",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.LightGray,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "“Hi mom, did you take your iron tablet?”")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* Send logic */ }) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        // Icon at the top
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Label text
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A) // Same dark color as the value text
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Value text
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A) // Very dark for emphasis
        )
    }
}
