package com.example.careconnect.screens

import android.app.Application
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.viewmodel.HealthViewModel
import com.example.careconnect.viewmodel.LocationViewModel
import com.example.careconnect.viewmodel.ReminderViewModel
import com.example.careconnect.health.HealthSummary
import com.example.careconnect.health.MetricsPeriod
import com.example.careconnect.health.DailyHealthData
import com.example.careconnect.firestore.SchedulingReminder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthToolsScreen(
    onNavigateToDetailedView: (MetricsPeriod) -> Unit = {},
    onNavigateToManageReminders: () -> Unit = {}
) {
    val context = LocalContext.current
    val healthViewModel: HealthViewModel = remember { HealthViewModel(context) }
    val locationViewModel: LocationViewModel = viewModel()
    val reminderViewModel: ReminderViewModel = viewModel { ReminderViewModel(context.applicationContext as Application) }
    
    val dailySummary by healthViewModel.dailySummary.collectAsState()
    val weeklySummary by healthViewModel.weeklySummary.collectAsState()
    val monthlySummary by healthViewModel.monthlySummary.collectAsState()
    val isLoading by healthViewModel.isLoading.collectAsState()
    val connectionStatus by healthViewModel.connectionStatus.collectAsState()
    
    // Reminder states
    val reminders by reminderViewModel.reminders.collectAsState()
    val reminderMessage by reminderViewModel.message.collectAsState()
    
    // Location tracking states
    val isTrackingEnabled by locationViewModel.isTrackingEnabled.observeAsState(false)
    val lastLocationStatus by locationViewModel.lastLocationStatus.observeAsState("")
    val shouldRequestPermission by locationViewModel.shouldRequestPermission.observeAsState(false)
    
    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            locationViewModel.onPermissionGranted()
        } else {
            locationViewModel.onPermissionDenied()
        }
    }
    
    // Handle permission request
    LaunchedEffect(shouldRequestPermission) {
        if (shouldRequestPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            locationViewModel.onPermissionRequestHandled()
        }
    }
    
    // Show reminder message
    LaunchedEffect(reminderMessage) {
        if (reminderMessage.isNotEmpty()) {
            // Clear message after showing it
            reminderViewModel.clearMessage()
        }
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
                    RemindersSummaryCard(
                        reminders = reminders,
                        onNavigateToManage = onNavigateToManageReminders
                    )
                }
                
                item {
                    LocationTrackingCard(
                        isTrackingEnabled = isTrackingEnabled,
                        lastLocationStatus = lastLocationStatus,
                        hasPermission = locationViewModel.hasLocationPermission(),
                        onStartTracking = { locationViewModel.startLocationTracking() },
                        onStopTracking = { locationViewModel.stopLocationTracking() },
                        onGetCurrentLocation = { locationViewModel.getCurrentLocationAndSave() },
                        onRequestPermission = { locationViewModel.requestLocationPermission() }
                    )
                }
                
                item {
                    UnifiedHealthSummaryCard(
                        dailySummary = dailySummary,
                        weeklySummary = weeklySummary,
                        monthlySummary = monthlySummary,
                        onDetailedViewClick = onNavigateToDetailedView
                    )
                }
            }
        }
    }
}

@Composable
fun RemindersSummaryCard(
    reminders: List<SchedulingReminder>,
    onNavigateToManage: () -> Unit
) {
    val context = LocalContext.current
    val activeReminders = reminders.filter { reminder ->
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val endDate = dateFormat.parse(reminder.endDate)
            val today = Date()
            endDate?.after(today) ?: false || endDate?.equals(today) ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    // Check notification permission
    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (hasNotificationPermission) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = "Reminders",
                        tint = if (hasNotificationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "Reminders",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!hasNotificationPermission) {
                            Text(
                                text = "Notifications disabled",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = onNavigateToManage,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Manage",
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (reminders.isEmpty()) {
                Text(
                    text = "No reminders set. Use 'Manage' to create your first reminder.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = reminders.size.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Total",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = activeReminders.size.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Active",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = reminders.count { it.hasAccountability }.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "With Partners",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: SchedulingReminder,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = "${reminder.startDate} - ${reminder.endDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Type: ${reminder.type} • Time: ${reminder.reminderTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (reminder.hasAccountability) {
                    Text(
                        text = "${reminder.accountabilityPartners.size} accountability partner(s)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderForm(
    onSave: (SchedulingReminder) -> Unit,
    onCancel: () -> Unit,
    reminderViewModel: ReminderViewModel
) {
    var title by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var hasAccountability by remember { mutableStateOf(false) }
    var selectedPartners by remember { mutableStateOf(emptyList<String>()) }
    var showAccountabilityPartners by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    
    val followers by reminderViewModel.followers.collectAsState()
    
    // Type options
    val typeOptions = listOf(
        "Medication",
        "Exercise",
        "Appointment",
        "Check-up",
        "Therapy",
        "Diet",
        "Sleep",
        "Water Intake",
        "Other"
    )
    
    // Date validation function
    fun isEndDateValid(): Boolean {
        if (startDate.isEmpty() || endDate.isEmpty()) return true
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)
            end?.after(start) ?: true || end?.equals(start) ?: true
        } catch (e: Exception) {
            true
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Add New Reminder",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Start Date with Calendar Picker
            OutlinedTextField(
                value = startDate,
                onValueChange = { },
                label = { Text("Start Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStartDatePicker = true },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Start Date")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // End Date with Calendar Picker and Validation
            OutlinedTextField(
                value = endDate,
                onValueChange = { },
                label = { Text("End Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEndDatePicker = true },
                readOnly = true,
                isError = !isEndDateValid(),
                supportingText = {
                    if (!isEndDateValid()) {
                        Text(
                            text = "End date must be same as or after start date",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select End Date")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time Picker
            OutlinedTextField(
                value = reminderTime,
                onValueChange = { },
                label = { Text("Reminder Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Type Dropdown
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = { },
                    label = { Text("Type") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown)
                    }
                )
                
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    typeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                type = option
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasAccountability,
                    onCheckedChange = { hasAccountability = it }
                )
                Text(
                    text = "Accountability",
                    modifier = Modifier
                        .weight(1f)
                        .clickable { hasAccountability = !hasAccountability }
                )
                
                if (hasAccountability) {
                    TextButton(
                        onClick = { showAccountabilityPartners = !showAccountabilityPartners }
                    ) {
                        Text("Select Partners (${selectedPartners.size})")
                    }
                }
            }
            
            if (hasAccountability && showAccountabilityPartners) {
                Spacer(modifier = Modifier.height(12.dp))
                AccountabilityPartnersSection(
                    followers = followers,
                    selectedPartners = selectedPartners,
                    onSelectionChange = { selectedPartners = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        if (title.isNotBlank() && startDate.isNotBlank() && 
                            endDate.isNotBlank() && reminderTime.isNotBlank() && 
                            type.isNotBlank() && isEndDateValid()) {
                            
                            val reminder = SchedulingReminder(
                                title = title,
                                startDate = startDate,
                                endDate = endDate,
                                reminderTime = reminderTime,
                                type = type,
                                hasAccountability = hasAccountability,
                                accountabilityPartners = selectedPartners
                            )
                            onSave(reminder)
                        }
                    },
                    enabled = title.isNotBlank() && startDate.isNotBlank() && 
                            endDate.isNotBlank() && reminderTime.isNotBlank() && 
                            type.isNotBlank() && isEndDateValid()
                ) {
                    Text("Save")
                }
            }
        }
    }
    
    // Date Pickers
    if (showStartDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                // Reset end date if it becomes invalid
                if (endDate.isNotEmpty() && !isEndDateValid()) {
                    endDate = ""
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    if (showEndDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
    
    // Time Picker
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                reminderTime = time
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun AccountabilityPartnersSection(
    followers: List<Map<String, Any>>,
    selectedPartners: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Select Accountability Partners",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (followers.isEmpty()) {
                Text(
                    text = "No followers available. Connect with others to add accountability partners.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                followers.forEach { follower ->
                    val name = follower["fullName"] as? String ?: "Unknown"
                    val uid = follower["uid"] as? String ?: ""
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectionChange(
                                    if (selectedPartners.contains(uid)) {
                                        selectedPartners - uid
                                    } else {
                                        selectedPartners + uid
                                    }
                                )
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedPartners.contains(uid),
                            onCheckedChange = { isChecked ->
                                onSelectionChange(
                                    if (isChecked) {
                                        selectedPartners + uid
                                    } else {
                                        selectedPartners - uid
                                    }
                                )
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = name,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Date",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = millis
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val dateString = dateFormat.format(calendar.time)
                                onDateSelected(dateString)
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Time",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TimePicker(
                    state = timePickerState
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val hour = timePickerState.hour
                            val minute = timePickerState.minute
                            val timeString = String.format("%02d:%02d", hour, minute)
                            onTimeSelected(timeString)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun UnifiedHealthSummaryCard(
    dailySummary: HealthSummary?,
    weeklySummary: HealthSummary?,
    monthlySummary: HealthSummary?,
    onDetailedViewClick: (MetricsPeriod) -> Unit
) {
    var selectedPeriod by remember { mutableStateOf(MetricsPeriod.DAILY) }
    val currentSummary = when (selectedPeriod) {
        MetricsPeriod.DAILY -> dailySummary
        MetricsPeriod.WEEKLY -> weeklySummary
        MetricsPeriod.MONTHLY -> monthlySummary
    }
    
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
                    text = "Health Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedButton(
                    onClick = { onDetailedViewClick(selectedPeriod) },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Detailed View",
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PeriodButton(
                    period = MetricsPeriod.DAILY,
                    isSelected = selectedPeriod == MetricsPeriod.DAILY,
                    onClick = { selectedPeriod = MetricsPeriod.DAILY }
                )
                
                PeriodButton(
                    period = MetricsPeriod.WEEKLY,
                    isSelected = selectedPeriod == MetricsPeriod.WEEKLY,
                    onClick = { selectedPeriod = MetricsPeriod.WEEKLY }
                )
                
                PeriodButton(
                    period = MetricsPeriod.MONTHLY,
                    isSelected = selectedPeriod == MetricsPeriod.MONTHLY,
                    onClick = { selectedPeriod = MetricsPeriod.MONTHLY }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (currentSummary != null && currentSummary.totalDays > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "Steps",
                        value = "${currentSummary.avgStepCount}",
                        icon = Icons.Default.DirectionsWalk,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    MetricItem(
                        title = "Heart Rate",
                        value = "${currentSummary.avgHeartRate.roundToInt()}",
                        unit = "bpm",
                        icon = Icons.Default.Favorite,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "Sleep",
                        value = "${(currentSummary.avgSleepHours * 10).roundToInt() / 10.0}",
                        unit = "hrs",
                        icon = Icons.Default.Bedtime,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    MetricItem(
                        title = "Calories",
                        value = "${currentSummary.avgCalories.roundToInt()}",
                        unit = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Based on ${currentSummary.totalDays} day${if (currentSummary.totalDays != 1) "s" else ""} of data",
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
private fun PeriodButton(
    period: MetricsPeriod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.width(100.dp)
    ) {
        Text(period.displayName)
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
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
fun LocationTrackingCard(
    isTrackingEnabled: Boolean,
    lastLocationStatus: String,
    hasPermission: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onGetCurrentLocation: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "GPS Location Tracking",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Switch(
                    checked = isTrackingEnabled,
                    onCheckedChange = { 
                        if (isTrackingEnabled) {
                            onStopTracking()
                        } else {
                            onStartTracking()
                        }
                    },
                    enabled = hasPermission
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (!hasPermission) {
                Column {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Location permission required. Please grant location access in app settings.",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.LocationSearching,
                            contentDescription = "Request Permission",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Request Location Permission")
                    }
                }
            } else {
                Column {
                    Text(
                        text = if (isTrackingEnabled) 
                            "📍 Automatic location updates every hour" 
                        else "⏸️ Location tracking is disabled",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    if (lastLocationStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (lastLocationStatus.contains("saved") || lastLocationStatus.contains("started"))
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Status: $lastLocationStatus",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 12.sp,
                                color = if (lastLocationStatus.contains("saved") || lastLocationStatus.contains("started"))
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = onGetCurrentLocation,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = hasPermission
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Get Location",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Get Current Location Now")
                    }
                }
            }
        }
    }
}
