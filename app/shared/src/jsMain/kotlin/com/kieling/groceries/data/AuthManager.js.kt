package com.kieling.groceries.data

import com.kieling.groceries.domain.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class AuthManager actual constructor() {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    actual val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    actual suspend fun signInWithGoogle(): Result<AuthUser> = Result.failure(Exception("Configure Firebase Web SDK for Google Sign-In"))

    actual suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> = Result.failure(Exception("Configure Firebase Web SDK for Email Sign-In"))

    actual suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> = Result.failure(Exception("Configure Firebase Web SDK for Email Sign-Up"))

    actual suspend fun signOut() {
        _currentUser.value = null
    }
}
