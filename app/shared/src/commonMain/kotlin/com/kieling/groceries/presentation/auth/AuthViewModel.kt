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
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
) : ViewModel() {
    val currentUser: StateFlow<AuthUser?> = observeCurrentUserUseCase()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun toggleIsRegistering() {
        _isRegistering.value = !_isRegistering.value
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun authenticate(onSuccess: () -> Unit = {}) {
        val emailVal = _email.value
        val passwordVal = _password.value
        if (emailVal.isBlank() || passwordVal.isBlank()) {
            _errorMessage.value = "Please enter email and password"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result =
                if (_isRegistering.value) {
                    signUpWithEmailUseCase(emailVal, passwordVal)
                } else {
                    signInWithEmailUseCase(emailVal, passwordVal)
                }
            _isLoading.value = false
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { err ->
                    _errorMessage.value = err.message ?: "Authentication failed"
                },
            )
        }
    }

    fun signInWithGoogle(onSuccess: () -> Unit = {}) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = signInWithGoogleUseCase()
            _isLoading.value = false
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { err ->
                    _errorMessage.value = err.message ?: "Google Sign-In failed"
                },
            )
        }
    }
}
