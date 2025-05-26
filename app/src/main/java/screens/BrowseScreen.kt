package com.example.careconnect.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen() {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf("") }
    val datePicker = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                selectedDate = "$day/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    var reminderExpanded by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("") }
    val types = listOf("Appointment", "Check-up", "Vaccination")

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Browse") },
                    label = { Text("BROWSE") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.InsertChart, contentDescription = "Patterns") },
                    label = { Text("PATTERNS") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("HOME") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("CHAT") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("SETTINGS") },
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text("Caring Tools", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))
            Text("1. Set Reminders", fontSize = 18.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.LightGray,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Appointment Reminder", fontSize = 16.sp)
                        IconButton(onClick = { reminderExpanded = !reminderExpanded }) {
                            Icon(
                                imageVector = if (reminderExpanded) Icons.Default.Remove else Icons.Default.Add,
                                contentDescription = "Toggle Reminder"
                            )
                        }
                    }

                    if (reminderExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Title")
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Date")
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePicker.show() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Type")
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = !typeExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedType,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select type")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .clickable { typeExpanded = true }
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                types.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            selectedType = it
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color.LightGray,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Add new reminder type */ }
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Medicine Reminder")
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("2. Articles", fontSize = 18.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
