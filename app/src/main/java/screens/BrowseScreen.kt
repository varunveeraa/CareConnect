package com.example.careconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careconnect.health.DailyHealthData
import com.example.careconnect.health.HealthDataManager
import com.example.careconnect.health.MetricsPeriod
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen() {
    val context = LocalContext.current
    val healthDataManager = remember { HealthDataManager(context) }
    
    var selectedPeriod by remember { mutableStateOf(MetricsPeriod.DAILY) }
    var healthData by remember { mutableStateOf<List<DailyHealthData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Load health data when screen opens or period changes
    LaunchedEffect(selectedPeriod) {
        isLoading = true
        try {
            healthData = healthDataManager.fetchHealthMetricsForPeriod(selectedPeriod)
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Header
        Text(
            "Health Metrics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Period Selection
        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { selectedPeriod = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (healthData.isEmpty()) {
            NoDataMessage()
        } else {
            // Metrics Summary Cards
            MetricsSummaryCards(healthData)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detailed List
            Text(
                "Detailed View",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn {
                items(healthData.sortedByDescending { it.timestamp }) { data ->
                    HealthDataCard(data)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: MetricsPeriod,
    onPeriodSelected: (MetricsPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricsPeriod.values().forEach { period ->
            FilterChip(
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName) },
                selected = selectedPeriod == period,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricsSummaryCards(healthData: List<DailyHealthData>) {
    val avgSteps = healthData.map { it.stepCount }.average().roundToInt()
    val avgHeartRate = healthData.map { it.heartRate }.average().roundToInt()
    val avgSleep = healthData.map { it.sleepHours }.average()
    val avgCalories = healthData.map { it.calories }.average().roundToInt()
    
    Column {
        Text(
            "Summary",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Avg Steps",
                value = avgSteps.toString(),
                icon = Icons.Default.DirectionsWalk,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Avg BPM",
                value = avgHeartRate.toString(),
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Avg Sleep",
                value = "${String.format("%.1f", avgSleep)}h",
                icon = Icons.Default.Bedtime,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Avg Calories",
                value = avgCalories.toString(),
                icon = Icons.Default.LocalFireDepartment,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun HealthDataCard(data: DailyHealthData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    data.date,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(data.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem("Steps", data.stepCount.toString(), Icons.Default.DirectionsWalk)
                MetricItem("BPM", data.heartRate.roundToInt().toString(), Icons.Default.Favorite)
                MetricItem("Sleep", "${String.format("%.1f", data.sleepHours)}h", Icons.Default.Bedtime)
                MetricItem("Cal", data.calories.roundToInt().toString(), Icons.Default.LocalFireDepartment)
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NoDataMessage() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.HealthAndSafety,
            contentDescription = "No Data",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No health data available",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Connect a wearable device in Account settings",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
