package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.model.AuthUser

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<AuthUser> = authRepository.signInWithGoogle()
}
