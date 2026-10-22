package com.kieling.groceries.domain.usecase

import com.kieling.groceries.AuthUser
import com.kieling.groceries.domain.repository.AuthRepository

class SignUpWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): Result<AuthUser> =
        authRepository.signUpWithEmail(email, password)
}
