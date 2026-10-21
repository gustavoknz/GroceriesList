package com.kieling.groceries

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onSignInClick: () -> Unit) {
    val authManager = remember { AuthManager() }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Groceries List",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRegistering) "Create a new account" else "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result =
                                if (isRegistering) {
                                    authManager.signUpWithEmail(email, password)
                                } else {
                                    authManager.signInWithEmail(email, password)
                                }
                            isLoading = false
                            result.onFailure { err ->
                                errorMessage = err.message ?: "Authentication failed"
                            }
                        }
                    } else {
                        errorMessage = "Please enter email and password"
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                enabled = !isLoading,
            ) {
                Text(if (isRegistering) "Register" else "Sign In with Email", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    isRegistering = !isRegistering
                    errorMessage = null
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
            ) {
                Text(if (isRegistering) "Already have an account? Sign In" else "Create Account", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSignInClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                enabled = !isLoading,
            ) {
                Text("Sign in with Google", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
