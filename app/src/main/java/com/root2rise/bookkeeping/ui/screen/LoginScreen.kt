package com.root2rise.bookkeeping.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.ui.components.GradientBackground
import com.root2rise.bookkeeping.ui.components.GradientButton
import com.root2rise.bookkeeping.ui.components.NeonTextField
import com.root2rise.bookkeeping.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Login to your account",
                fontSize = 16.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email Field
            NeonTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = "Email",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            NeonTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = "Password",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        performLogin(
                            email = email,
                            password = password,
                            onLoading = { isLoading = it },
                            onSuccess = onLoginSuccess,
                            onError = { errorMessage = it }
                        )
                    }
                }
            )

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Forgot Password
            Text(
                text = "Forgot Password?",
                fontSize = 14.sp,
                color = NeonBlue,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { /* TODO: Handle forgot password */ }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            GradientButton(
                text = "Login",
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        performLogin(
                            email = email,
                            password = password,
                            onLoading = { isLoading = it },
                            onSuccess = onLoginSuccess,
                            onError = { errorMessage = it }
                        )
                    } else {
                        errorMessage = "Please fill in all fields"
                    }
                },
                loading = isLoading,
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Register",
                    fontSize = 14.sp,
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun performLogin(
    email: String,
    password: String,
    onLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)

    // Simulate login - Replace with actual authentication
    // For now, accept any non-empty credentials
    CoroutineScope(Dispatchers.Main).launch {
        delay(1500)
        onLoading(false)

        if (email.contains("@") && password.length >= 6) {
            onSuccess()
        } else {
            onError("Invalid email or password")
        }
    }
}
