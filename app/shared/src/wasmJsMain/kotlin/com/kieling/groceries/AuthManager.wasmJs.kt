package com.kieling.groceries

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class AuthManager actual constructor() {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    actual val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    actual suspend fun signInWithGoogle(): Result<AuthUser> {
        return Result.failure(Exception("Configure Firebase Web SDK for Google Sign-In"))
    }

    actual suspend fun signOut() {
        _currentUser.value = null
    }
}
