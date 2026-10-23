package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.signOut()
}
