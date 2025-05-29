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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.viewmodel.HealthViewModel
import com.example.careconnect.health.HealthSummary
import com.example.careconnect.health.MetricsPeriod
import com.example.careconnect.health.DailyHealthData
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthToolsScreen(
    onNavigateToDetailedView: (MetricsPeriod) -> Unit = {}
) {
    val context = LocalContext.current
    val healthViewModel: HealthViewModel = remember { HealthViewModel(context) }
    
    val dailySummary by healthViewModel.dailySummary.collectAsState()
    val weeklySummary by healthViewModel.weeklySummary.collectAsState()
    val monthlySummary by healthViewModel.monthlySummary.collectAsState()
    val isLoading by healthViewModel.isLoading.collectAsState()
    val connectionStatus by healthViewModel.connectionStatus.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Health Tools",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { healthViewModel.generateDummyDataForAllUsers() }
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh Data",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (connectionStatus.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (connectionStatus.contains("success")) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = connectionStatus,
                    modifier = Modifier.padding(12.dp),
                    color = if (connectionStatus.contains("success")) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HealthSummaryCard(
                        title = "Daily Summary",
                        summary = dailySummary,
                        period = MetricsPeriod.DAILY,
                        onDetailedViewClick = onNavigateToDetailedView
                    )
                }
                
                item {
                    HealthSummaryCard(
                        title = "Weekly Summary",
                        summary = weeklySummary,
                        period = MetricsPeriod.WEEKLY,
                        onDetailedViewClick = onNavigateToDetailedView
                    )
                }
                
                item {
                    HealthSummaryCard(
                        title = "Monthly Summary",
                        summary = monthlySummary,
                        period = MetricsPeriod.MONTHLY,
                        onDetailedViewClick = onNavigateToDetailedView
                    )
                }
            }
        }
    }
}

@Composable
fun HealthSummaryCard(
    title: String,
    summary: HealthSummary?,
    period: MetricsPeriod,
    onDetailedViewClick: (MetricsPeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedButton(
                    onClick = { onDetailedViewClick(period) },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Detailed View",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (summary != null && summary.totalDays > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "Steps",
                        value = "${summary.avgStepCount}",
                        icon = Icons.Default.DirectionsWalk,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    MetricItem(
                        title = "Heart Rate",
                        value = "${summary.avgHeartRate.roundToInt()}",
                        unit = "bpm",
                        icon = Icons.Default.Favorite,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "Sleep",
                        value = "${(summary.avgSleepHours * 10).roundToInt() / 10.0}",
                        unit = "hrs",
                        icon = Icons.Default.Bedtime,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    MetricItem(
                        title = "Calories",
                        value = "${summary.avgCalories.roundToInt()}",
                        unit = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Based on ${summary.totalDays} day${if (summary.totalDays != 1) "s" else ""} of data",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    text = "No data available",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    title: String,
    value: String,
    unit: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "$value${if (unit.isNotEmpty()) " $unit" else ""}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDetailedViewScreen(
    period: MetricsPeriod,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val healthViewModel: HealthViewModel = remember { HealthViewModel(context) }
    
    val detailedData by healthViewModel.detailedData.collectAsState()
    val isLoading by healthViewModel.isLoading.collectAsState()
    
    LaunchedEffect(period) {
        healthViewModel.loadDetailedData(period)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "${period.displayName} Detailed View",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (detailedData.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Simple graph visualization (bar chart style)
                item {
                    HealthDataChart(
                        data = detailedData,
                        period = period
                    )
                }
                
                // Data list
                items(detailedData.reversed()) { data ->
                    DailyHealthDataItem(data)
                }
            }
        } else {
            Text(
                text = "No data available for ${period.displayName.lowercase()} view",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HealthDataChart(
    data: List<DailyHealthData>,
    period: MetricsPeriod
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Steps Chart",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Simple bar chart visualization
            val maxSteps = data.maxOfOrNull { it.stepCount } ?: 1
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.takeLast(10).forEach { dayData ->
                    val barHeight = (dayData.stepCount.toFloat() / maxSteps * 80).dp
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barHeight)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        
                        Text(
                            text = dayData.date.split("-").last(),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Steps over time (last ${minOf(data.size, 10)} days)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DailyHealthDataItem(data: DailyHealthData) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = data.date,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SmallMetricItem("Steps", "${data.stepCount}", Icons.Default.DirectionsWalk)
                SmallMetricItem("HR", "${data.heartRate.roundToInt()}", Icons.Default.Favorite)
                SmallMetricItem("Sleep", "${(data.sleepHours * 10).roundToInt() / 10.0}h", Icons.Default.Bedtime)
                SmallMetricItem("Cal", "${data.calories.roundToInt()}", Icons.Default.LocalFireDepartment)
            }
        }
    }
}

@Composable
fun SmallMetricItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
