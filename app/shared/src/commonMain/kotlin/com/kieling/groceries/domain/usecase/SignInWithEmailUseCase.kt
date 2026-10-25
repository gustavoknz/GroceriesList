package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.model.AuthUser
import com.kieling.groceries.domain.repository.AuthRepository

class SignInWithEmailUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): Result<AuthUser> = authRepository.signInWithEmail(email, password)
}
