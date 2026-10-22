package com.kieling.groceries.domain.repository

import com.kieling.groceries.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
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
