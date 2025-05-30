package com.example.careconnect.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.viewmodel.FirebaseAuthViewModel
import com.example.careconnect.viewmodel.FirebaseAuthState
import com.example.careconnect.viewmodel.FirebaseSignUpState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: FirebaseAuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(1) } // Start with signup tab
    val tabs = listOf("Login", "Sign Up")
    
    val authState by authViewModel.authState.collectAsState()

    // Handle authentication success
    LaunchedEffect(authState) {
        if (authState is FirebaseAuthState.Authenticated) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> LoginContent(authViewModel = authViewModel)
            1 -> SignUpContent(authViewModel = authViewModel)
        }
    }
}

@Composable
fun LoginContent(authViewModel: FirebaseAuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPassword by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                authViewModel.resetAuthError()
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                authViewModel.resetAuthError()
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { showForgotPassword = true }
            ) {
                Text("Forgot password?")
            }
        }

        // Show auth error
        val currentAuthState = authState
        if (currentAuthState is FirebaseAuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentAuthState.message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.login(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }

    // Forgot Password Dialog
    if (showForgotPassword) {
        ForgotPasswordDialog(
            onDismiss = { 
                showForgotPassword = false
                authViewModel.resetForgotPasswordState()
            },
            onSendEmail = { emailAddress ->
                authViewModel.forgotPassword(emailAddress)
            },
            forgotPasswordState = forgotPasswordState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(authViewModel: FirebaseAuthViewModel) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val signUpState by authViewModel.signUpState.collectAsState()
    val genderOptions = listOf("Male", "Female", "Other")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = signUpState !is FirebaseSignUpState.Loading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = signUpState !is FirebaseSignUpState.Loading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Date of Birth field
        OutlinedTextField(
            value = selectedDate?.let { 
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
            } ?: "",
            onValueChange = { },
            label = { Text("Date of Birth") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Select")
                }
            },
            enabled = signUpState !is FirebaseSignUpState.Loading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gender dropdown
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = it && signUpState !is FirebaseSignUpState.Loading }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = { },
                readOnly = true,
                label = { Text("Gender") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = signUpState !is FirebaseSignUpState.Loading,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) }
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            genderExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = signUpState !is FirebaseSignUpState.Loading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = signUpState !is FirebaseSignUpState.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Error message
        val currentSignUpState = signUpState
        if (currentSignUpState is FirebaseSignUpState.Error) {
            Text(
                text = currentSignUpState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Button(
            onClick = {
                when {
                    fullName.isBlank() -> return@Button
                    email.isBlank() -> return@Button
                    password.isBlank() -> return@Button
                    confirmPassword.isBlank() -> return@Button
                    selectedDate == null -> return@Button
                    gender.isBlank() -> return@Button
                    password != confirmPassword -> return@Button
                    else -> {
                        val dobString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(selectedDate!!))

                        authViewModel.signUp(
                            fullName = fullName,
                            email = email,
                            password = password,
                            dateOfBirth = dobString,
                            gender = gender
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = signUpState !is FirebaseSignUpState.Loading
        ) {
            if (signUpState is FirebaseSignUpState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Sign Up")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { dateInMillis ->
                selectedDate = dateInMillis
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendEmail: (String) -> Unit,
    forgotPasswordState: com.example.careconnect.viewmodel.ForgotPasswordState
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                Text("Enter your email address to receive a password reset link.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                when (forgotPasswordState) {
                    is com.example.careconnect.viewmodel.ForgotPasswordState.Success -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Password reset email sent! Check your inbox.",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is com.example.careconnect.viewmodel.ForgotPasswordState.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            forgotPasswordState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onSendEmail(email)
                    }
                },
                enabled = email.isNotBlank() && forgotPasswordState !is com.example.careconnect.viewmodel.ForgotPasswordState.Loading
            ) {
                if (forgotPasswordState is com.example.careconnect.viewmodel.ForgotPasswordState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let(onDateSelected)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
