package com.example.careconnect.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen() {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var dob by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf("") }

    var healthExpanded by remember { mutableStateOf(false) }
    var healthCondition by remember { mutableStateOf("") }

    var focusAreas by remember { mutableStateOf("") }

    var communicationOption by remember { mutableStateOf("Ask") }

    var selectedContact by remember { mutableStateOf("") }

    val genders = listOf("Male", "Female", "Other")
    val healthConditions = listOf("Diabetes", "Memory Loss", "Fall Risk")

    val datePicker = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            dob = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 48.dp,
                start = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile Set Up", style = MaterialTheme.typography.headlineMedium)
        Text(
            "To personalise your experience, please enter details of your care receiver.",
            style = MaterialTheme.typography.bodyMedium
        )

        // Date of Birth
        Text("*Date of Birth", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = dob,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePicker.show() }
        )

        // Gender Dropdown
        Text("*Gender", style = MaterialTheme.typography.bodyMedium)
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { genderExpanded = true }
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genders.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            gender = it
                            genderExpanded = false
                        }
                    )
                }
            }
        }

        // Health Conditions Dropdown
        Text("Health Conditions", style = MaterialTheme.typography.bodyMedium)
        ExposedDropdownMenuBox(
            expanded = healthExpanded,
            onExpandedChange = { healthExpanded = !healthExpanded }
        ) {
            OutlinedTextField(
                value = healthCondition,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { healthExpanded = true }
            )
            ExposedDropdownMenu(
                expanded = healthExpanded,
                onDismissRequest = { healthExpanded = false }
            ) {
                healthConditions.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            healthCondition = it
                            healthExpanded = false
                        }
                    )
                }
            }
        }

        // Focus Areas
        Text("Focus Areas (e.g. memory loss, fall risk)", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = focusAreas,
            onValueChange = { focusAreas = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Communication Method
        Text("* Choose Method of Communication", style = MaterialTheme.typography.bodyMedium)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = communicationOption == "Share",
                    onClick = { communicationOption = "Share" }
                )
                Text("Share with a family member")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = communicationOption == "Ask",
                    onClick = { communicationOption = "Ask" }
                )
                Text("Ask a family member to share")
            }
        }

        // Search Contacts
        Text("*Search contacts for sharing", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = selectedContact,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Done Button
        Button(
            onClick = { /* Submit logic */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}
