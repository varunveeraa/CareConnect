package screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.screens.ForgotPasswordDialog
import com.example.careconnect.viewmodel.FirebaseAuthViewModel
import com.example.careconnect.viewmodel.FirebaseAuthState


@Composable
fun LoginScreen(
    authViewModel: FirebaseAuthViewModel = viewModel(),
    onSignUpClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()
    val isLoading = authState is FirebaseAuthState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Log In",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Email", modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                authViewModel.resetAuthError()
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Password", modifier = Modifier.align(Alignment.Start))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                authViewModel.resetAuthError()
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                val desc = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = desc, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                }
            }
        )


        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    enabled = !isLoading
                )
                Text(text = "Remember me")
            }

            Text(
                text = "Forgot password?",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable(enabled = !isLoading) { 
                    showForgotPassword = true 
                }
            )
        }

        // Show auth error with improved messaging
        val currentAuthState = authState
        if (currentAuthState is FirebaseAuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = when {
                        currentAuthState.message.contains("password", ignoreCase = true) ->
                            "❌ Incorrect password. Please try again."

                        currentAuthState.message.contains("user-not-found", ignoreCase = true) ->
                            "❌ No account found with this email address."

                        currentAuthState.message.contains("invalid-email", ignoreCase = true) ->
                            "❌ Please enter a valid email address."

                        currentAuthState.message.contains("too-many-requests", ignoreCase = true) ->
                            "⏳ Too many failed attempts. Please try again later."

                        currentAuthState.message.contains("network", ignoreCase = true) ->
                            "🌐 Network error. Please check your connection."

                        else -> "❌ ${currentAuthState.message}"
                    },
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.login(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Log In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Or",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign Up",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.clickable(enabled = !isLoading) { onSignUpClick() }
        )
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
