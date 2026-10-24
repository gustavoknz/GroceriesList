package com.kieling.groceries.data

import com.kieling.groceries.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

expect class AuthManager() {
    val currentUser: StateFlow<AuthUser?>

    suspend fun signInWithGoogle(): Result<AuthUser>

    suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser>

    suspend fun signOut()
}
