package com.example.careconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.api.OverpassPlace
import com.example.careconnect.viewmodel.HealthViewModel
import com.example.careconnect.viewmodel.MapsViewModel
import com.example.careconnect.health.HealthSummary
import com.example.careconnect.health.MetricsPeriod
import com.example.careconnect.health.DailyHealthData
import com.example.careconnect.ui.maps.EmbeddedMapView
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

    var expandedSection by remember { mutableStateOf<String?>(null) }
    var selectedPeriod by remember { mutableStateOf<MetricsPeriod>(MetricsPeriod.DAILY) }

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

        // Accordion sections
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Summary Section
            AccordionSection(
                title = "Summary",
                isExpanded = expandedSection == "summary",
                onExpandChange = {
                    expandedSection = if (expandedSection == "summary") null else "summary"
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { selectedPeriod = MetricsPeriod.DAILY },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == MetricsPeriod.DAILY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedPeriod == MetricsPeriod.DAILY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(text = "Daily")
                    }

                    Button(
                        onClick = { selectedPeriod = MetricsPeriod.WEEKLY },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == MetricsPeriod.WEEKLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedPeriod == MetricsPeriod.WEEKLY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(text = "Weekly")
                    }

                    Button(
                        onClick = { selectedPeriod = MetricsPeriod.MONTHLY },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == MetricsPeriod.MONTHLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedPeriod == MetricsPeriod.MONTHLY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(text = "Monthly")
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
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        when (selectedPeriod) {
                            MetricsPeriod.DAILY -> HealthSummaryCard(
                                title = "Daily Summary",
                                summary = dailySummary,
                                period = MetricsPeriod.DAILY
                            )

                            MetricsPeriod.WEEKLY -> HealthSummaryCard(
                                title = "Weekly Summary",
                                summary = weeklySummary,
                                period = MetricsPeriod.WEEKLY
                            )

                            MetricsPeriod.MONTHLY -> HealthSummaryCard(
                                title = "Monthly Summary",
                                summary = monthlySummary,
                                period = MetricsPeriod.MONTHLY
                            )
                        }

                        // Move the detailed view button to the bottom
                        OutlinedButton(
                            onClick = { onNavigateToDetailedView(selectedPeriod) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(text = "Detailed View")
                        }
                    }
                }
            }

            // Reminders Section
            AccordionSection(
                title = "Reminders",
                isExpanded = expandedSection == "reminders",
                onExpandChange = {
                    expandedSection = if (expandedSection == "reminders") null else "reminders"
                }
            ) {
                Text(
                    text = "Reminders content coming soon",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Maps Section
            AccordionSection(
                title = "Maps",
                isExpanded = expandedSection == "maps",
                onExpandChange = {
                    expandedSection = if (expandedSection == "maps") null else "maps"
                }
            ) {
                MapsContent()
            }
        }
    }
}

@Composable
fun AccordionSection(
    title: String,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            if (isExpanded) {
                content()
            }
        }
    }
}

@Composable
fun HealthSummaryCard(
    title: String,
    summary: HealthSummary?,
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
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

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

@Composable
fun MapsContent() {
    val context = LocalContext.current
    val mapsViewModel: MapsViewModel = remember { MapsViewModel() }

    val allPlaces by mapsViewModel.allPlaces.collectAsState()
    val pharmacies by mapsViewModel.pharmacies.collectAsState()
    val clinics by mapsViewModel.clinics.collectAsState()
    val isLoading by mapsViewModel.isLoading.collectAsState()
    val selectedPlaceType by mapsViewModel.selectedPlaceType.collectAsState()
    val errorMessage by mapsViewModel.errorMessage.collectAsState()
    val selectedMelbourneArea by mapsViewModel.selectedMelbourneArea.collectAsState()
    val mapCenter by mapsViewModel.mapCenter.collectAsState()
    val isGeocoding by mapsViewModel.isGeocoding.collectAsState()

    // Melbourne default coordinates
    val melbourneCoords = mapsViewModel.getMelbourneCoordinates()
    var userLatitude by remember { mutableStateOf(melbourneCoords.first) }
    var userLongitude by remember { mutableStateOf(melbourneCoords.second) }
    var suburbSearchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .padding(bottom = 100.dp) // Extra padding to avoid bottom nav collision
    ) {
        // Location input section
        Text(
            text = "Find Healthcare in Melbourne",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp) // Reduced from 16dp
        )

        // Suburb search bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp) // Reduced from 12dp
            ) {
                Text(
                    text = "Search Melbourne Suburbs",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8dp

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = suburbSearchText,
                        onValueChange = { suburbSearchText = it },
                        label = { Text("search suburb") },
                        modifier = Modifier.weight(1f),
                        enabled = !isGeocoding && !isLoading
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (suburbSearchText.isNotBlank()) {
                                mapsViewModel.searchSuburb(suburbSearchText.trim())
                            }
                        },
                        enabled = !isGeocoding && !isLoading && suburbSearchText.isNotBlank()
                    ) {
                        if (isGeocoding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Search")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16dp

        // Melbourne area quick selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp) // Reduced from 12dp
            ) {
                Text(
                    text = "Quick Melbourne Areas",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8dp

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedButton(
                            onClick = {
                                mapsViewModel.searchMelbourneCBD()
                                userLatitude = -37.8136
                                userLongitude = 144.9631
                            },
                            enabled = !isLoading && !isGeocoding
                        ) {
                            Text("CBD", fontSize = 12.sp)
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                mapsViewModel.searchSouthYarra()
                                userLatitude = -37.8394
                                userLongitude = 144.9926
                            },
                            enabled = !isLoading && !isGeocoding
                        ) {
                            Text("South Yarra", fontSize = 12.sp)
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                mapsViewModel.searchStKilda()
                                userLatitude = -37.8676
                                userLongitude = 144.9803
                            },
                            enabled = !isLoading && !isGeocoding
                        ) {
                            Text("St Kilda", fontSize = 12.sp)
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                mapsViewModel.searchRichmond()
                                userLatitude = -37.8197
                                userLongitude = 144.9917
                            },
                            enabled = !isLoading && !isGeocoding
                        ) {
                            Text("Richmond", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16dp

        // Embedded Map View (keeping full size)
        EmbeddedMapView(
            modifier = Modifier.fillMaxWidth(),
            center = mapCenter,
            places = allPlaces,
            selectedPlaceType = selectedPlaceType,
            onFullScreenClick = {
                // TODO: Navigate to full screen map
            }
        )

        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16dp

        // Show selected area
        selectedMelbourneArea?.let { area ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp), // Reduced from 12dp
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 Searching in: $area",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { mapsViewModel.clearMelbourneArea() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear area",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8dp
        }

        // Manual coordinate input
        Text(
            text = "Or enter custom coordinates:",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = String.format("%.4f", userLatitude),
                onValueChange = {
                    userLatitude = it.toDoubleOrNull() ?: userLatitude
                    mapsViewModel.clearMelbourneArea()
                },
                label = { Text("Latitude") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                enabled = !isLoading && !isGeocoding
            )

            OutlinedTextField(
                value = String.format("%.4f", userLongitude),
                onValueChange = {
                    userLongitude = it.toDoubleOrNull() ?: userLongitude
                    mapsViewModel.clearMelbourneArea()
                },
                label = { Text("Longitude") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                enabled = !isLoading && !isGeocoding
            )
        }

        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16dp

        // Search button
        Button(
            onClick = { mapsViewModel.searchNearbyPlaces(userLatitude, userLongitude) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && !isGeocoding
        ) {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching...")
                }
            } else {
                Text("Search Healthcare Near Location")
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16dp

        // Place type selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                onClick = { mapsViewModel.setSelectedPlaceType("all") },
                label = { Text("All (${allPlaces.size})") },
                selected = selectedPlaceType == "all"
            )

            FilterChip(
                onClick = { mapsViewModel.setSelectedPlaceType("pharmacy") },
                label = { Text("Pharmacies (${pharmacies.size})") },
                selected = selectedPlaceType == "pharmacy"
            )

            FilterChip(
                onClick = { mapsViewModel.setSelectedPlaceType("clinic") },
                label = { Text("Clinics (${clinics.size})") },
                selected = selectedPlaceType == "clinic"
            )
        }

        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16dp

        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp), // Reduced from 12dp
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { mapsViewModel.clearError() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8dp
        }

        // Results
        val currentPlaces = mapsViewModel.getCurrentPlaces()

        if (currentPlaces.isNotEmpty()) {
            // Summary info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Found ${currentPlaces.size} ${if (selectedPlaceType == "all") "healthcare places" else selectedPlaceType} nearby" +
                            (selectedMelbourneArea?.let { " in $it" } ?: ""),
                    modifier = Modifier.padding(8.dp), // Reduced from 12dp
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8dp

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced from 8dp
            ) {
                items(currentPlaces) { place ->
                    OverpassPlaceItem(place = place)
                }
            }
        } else if (!isLoading && !isGeocoding && errorMessage == null) {
            Text(
                text = "No ${selectedPlaceType} found nearby.\nTry selecting a different Melbourne area or adjusting coordinates.",
                modifier = Modifier.padding(8.dp), // Reduced from 16dp
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OverpassPlaceItem(place: OverpassPlace) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = place.amenity.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Distance could be calculated here
                Text(
                    text = "${String.format("%.4f", place.lat)}, ${
                        String.format(
                            "%.4f",
                            place.lon
                        )
                    }",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            place.address?.let { address ->
                Text(
                    text = address,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            place.phone?.let { phone ->
                Text(
                    text = "📞 $phone",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            place.openingHours?.let { hours ->
                Text(
                    text = "🕒 $hours",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
