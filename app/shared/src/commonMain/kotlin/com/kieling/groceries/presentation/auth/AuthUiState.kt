package com.kieling.groceries.presentation.auth

import androidx.compose.runtime.Immutable

@Immutable
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
