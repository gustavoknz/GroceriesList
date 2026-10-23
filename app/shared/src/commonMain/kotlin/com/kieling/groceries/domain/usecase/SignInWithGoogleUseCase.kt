package com.kieling.groceries.domain.usecase

import com.kieling.groceries.AuthUser
import com.kieling.groceries.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<AuthUser> = authRepository.signInWithGoogle()
}
