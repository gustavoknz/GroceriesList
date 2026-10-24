package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.model.AuthUser

class SignInWithEmailUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): Result<AuthUser> = authRepository.signInWithEmail(email, password)
}
