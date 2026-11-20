package com.kieling.groceries.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import grocerieslist.app.shared.generated.resources.Res
import grocerieslist.app.shared.generated.resources.already_have_account
import grocerieslist.app.shared.generated.resources.app_name
import grocerieslist.app.shared.generated.resources.create_account
import grocerieslist.app.shared.generated.resources.create_new_account
import grocerieslist.app.shared.generated.resources.email
import grocerieslist.app.shared.generated.resources.password
import grocerieslist.app.shared.generated.resources.register
import grocerieslist.app.shared.generated.resources.sign_in_to_continue
import grocerieslist.app.shared.generated.resources.sign_in_with_email
import grocerieslist.app.shared.generated.resources.sign_in_with_google
import org.jetbrains.compose.resources.stringResource
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
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (uiState.isRegistering) stringResource(Res.string.create_new_account) else stringResource(Res.string.sign_in_to_continue),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { authViewModel.updateEmail(it) },
                label = { Text(stringResource(Res.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { authViewModel.updatePassword(it) },
                label = { Text(stringResource(Res.string.password)) },
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
                Text(
                    if (uiState.isRegistering) {
                        stringResource(Res.string.register)
                    } else {
                        stringResource(
                            Res.string.sign_in_with_email,
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { authViewModel.toggleIsRegistering() },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
            ) {
                Text(
                    if (uiState.isRegistering) {
                        stringResource(Res.string.already_have_account)
                    } else {
                        stringResource(
                            Res.string.create_account,
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
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
                Text(stringResource(Res.string.sign_in_with_google), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
