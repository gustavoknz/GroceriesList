package com.kieling.groceries

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kieling.groceries.presentation.auth.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onSignInClick: () -> Unit,
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

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
                text = if (uiState.isRegistering) "Create a new account" else "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { authViewModel.updateEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { authViewModel.updatePassword(it) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { authViewModel.authenticate() },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                enabled = !uiState.isLoading,
            ) {
                Text(if (uiState.isRegistering) "Register" else "Sign In with Email", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { authViewModel.toggleIsRegistering() },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
            ) {
                Text(if (uiState.isRegistering) "Already have an account? Sign In" else "Create Account", style = MaterialTheme.typography.bodyLarge)
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
                enabled = !uiState.isLoading,
            ) {
                Text("Sign in with Google", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
