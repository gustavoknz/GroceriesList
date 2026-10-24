package com.kieling.groceries.data.repository

import com.kieling.groceries.data.AuthManager
import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val authManager: AuthManager = AuthManager(),
) : AuthRepository {
    override val currentUser: StateFlow<AuthUser?> = authManager.currentUser

    override suspend fun signInWithGoogle(): Result<AuthUser> = authManager.signInWithGoogle()

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> = authManager.signInWithEmail(email, password)

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> = authManager.signUpWithEmail(email, password)

    override suspend fun signOut() {
        authManager.signOut()
    }
}
