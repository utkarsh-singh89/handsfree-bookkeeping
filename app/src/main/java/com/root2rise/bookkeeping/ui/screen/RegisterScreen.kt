package com.root2rise.bookkeeping.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.ui.components.GradientBackground
import com.root2rise.bookkeeping.ui.components.GradientButton
import com.root2rise.bookkeeping.ui.components.NeonTextField
import com.root2rise.bookkeeping.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Sign up to get started",
                fontSize = 16.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name Field
            NeonTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = "Full Name",
                leadingIcon = Icons.Filled.Person,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // Phone Field
            NeonTextField(
                value = phone,
                onValueChange = {
                    if (it.length <= 10) phone = it
                    errorMessage = null
                },
                label = "Phone Number",
                leadingIcon = Icons.Filled.Phone,
                keyboardType = KeyboardType.Phone,
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
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            NeonTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = "Confirm Password",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = {
                    if (validateFields(name, email, phone, password, confirmPassword)) {
                        performRegister(
                            name = name,
                            email = email,
                            phone = phone,
                            password = password,
                            onLoading = { isLoading = it },
                            onSuccess = onRegisterSuccess,
                            onError = { errorMessage = it }
                        )
                    } else {
                        errorMessage = "Please check all fields"
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

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            GradientButton(
                text = "Create Account",
                onClick = {
                    when {
                        name.isBlank() -> errorMessage = "Please enter your name"
                        email.isBlank() -> errorMessage = "Please enter your email"
                        !email.contains("@") -> errorMessage = "Please enter a valid email"
                        phone.isBlank() -> errorMessage = "Please enter your phone number"
                        phone.length < 10 -> errorMessage = "Please enter a valid phone number"
                        password.isBlank() -> errorMessage = "Please enter a password"
                        password.length < 6 -> errorMessage =
                            "Password must be at least 6 characters"

                        confirmPassword.isBlank() -> errorMessage = "Please confirm your password"
                        password != confirmPassword -> errorMessage = "Passwords do not match"
                        else -> {
                            performRegister(
                                name = name,
                                email = email,
                                phone = phone,
                                password = password,
                                onLoading = { isLoading = it },
                                onSuccess = onRegisterSuccess,
                                onError = { errorMessage = it }
                            )
                        }
                    }
                },
                loading = isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Login",
                    fontSize = 14.sp,
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun validateFields(
    name: String,
    email: String,
    phone: String,
    password: String,
    confirmPassword: String
): Boolean {
    return name.isNotBlank() &&
            email.contains("@") &&
            phone.length == 10 &&
            password.length >= 6 &&
            password == confirmPassword
}

private fun performRegister(
    name: String,
    email: String,
    phone: String,
    password: String,
    onLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)

    // Simulate registration - Replace with actual API call
    kotlinx.coroutines.GlobalScope.launch {
        kotlinx.coroutines.delay(1500)
        onLoading(false)
        onSuccess()
    }
}
