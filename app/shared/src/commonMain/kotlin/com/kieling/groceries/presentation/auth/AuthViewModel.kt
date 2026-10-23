package com.kieling.groceries.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieling.groceries.AuthUser
import com.kieling.groceries.domain.usecase.ObserveCurrentUserUseCase
import com.kieling.groceries.domain.usecase.SignInWithEmailUseCase
import com.kieling.groceries.domain.usecase.SignInWithGoogleUseCase
import com.kieling.groceries.domain.usecase.SignUpWithEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class AuthViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
) : ViewModel() {
    val currentUser: StateFlow<AuthUser?> = observeCurrentUserUseCase()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun updatePassword(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun toggleIsRegistering() {
        _uiState.update { it.copy(isRegistering = !it.isRegistering, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun authenticate(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter email and password") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result =
                if (state.isRegistering) {
                    signUpWithEmailUseCase(state.email, state.password)
                } else {
                    signInWithEmailUseCase(state.email, state.password)
                }
            _uiState.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = err.message ?: "Authentication failed") }
                },
            )
        }
    }

    fun signInWithGoogle(onSuccess: () -> Unit = {}) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = signInWithGoogleUseCase()
            _uiState.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = err.message ?: "Google Sign-In failed") }
                },
            )
        }
    }
}
