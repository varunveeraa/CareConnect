package com.example.careconnect.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen() {
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var roleExpanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("") }

    val genderOptions = listOf("Male", "Female", "Other")
    val roleOptions = listOf("Caregiver", "Receiver", "Doctor")

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

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
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Full Name", modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Date of Birth", modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = dob,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select date")
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePicker.show() }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Gender", modifier = Modifier.align(Alignment.Start))
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select gender")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { genderExpanded = true }
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genderOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        gender = it
                        genderExpanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Email", modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Password", modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Confirm Password", modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Choose your Role", modifier = Modifier.align(Alignment.Start))
        ExposedDropdownMenuBox(
            expanded = roleExpanded,
            onExpandedChange = { roleExpanded = !roleExpanded }
        ) {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select role")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { roleExpanded = true }
            )
            ExposedDropdownMenu(
                expanded = roleExpanded,
                onDismissRequest = { roleExpanded = false }
            ) {
                roleOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        selectedRole = it
                        roleExpanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /* TODO: Handle sign-up submission */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
    }
}
