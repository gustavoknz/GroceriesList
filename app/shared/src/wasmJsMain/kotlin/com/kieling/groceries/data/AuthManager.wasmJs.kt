package com.kieling.groceries.data

import com.kieling.groceries.domain.model.AuthUser
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class AuthManager actual constructor() {
    private val storageKey = "groceries_current_user_email"

    private val _currentUser =
        MutableStateFlow(
            runCatching {
                localStorage.getItem(storageKey)?.takeIf { it.isNotBlank() }?.let { email ->
                    AuthUser(
                        uid = email,
                        email = email,
                        displayName = email.substringBefore("@"),
                        photoUrl = null,
                    )
                }
            }.getOrNull(),
        )
    actual val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    actual suspend fun signInWithGoogle(): Result<AuthUser> {
        val user =
            AuthUser(
                uid = "google_user",
                email = "user@gmail.com",
                displayName = "Google User",
                photoUrl = null,
            )
        runCatching { localStorage.setItem(storageKey, user.email ?: user.uid) }
        _currentUser.value = user
        return Result.success(user)
    }

    actual suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> {
        val trimmed = email.trim()
        val user =
            AuthUser(
                uid = trimmed,
                email = trimmed,
                displayName = trimmed.substringBefore("@"),
                photoUrl = null,
            )
        runCatching { localStorage.setItem(storageKey, trimmed) }
        _currentUser.value = user
        return Result.success(user)
    }

    actual suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> = signInWithEmail(email, password)

    actual suspend fun signOut() {
        runCatching { localStorage.removeItem(storageKey) }
        _currentUser.value = null
    }
}
