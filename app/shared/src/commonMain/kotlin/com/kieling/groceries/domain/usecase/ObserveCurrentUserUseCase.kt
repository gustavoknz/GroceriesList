package com.kieling.groceries.domain.usecase

import com.kieling.groceries.AuthUser
import com.kieling.groceries.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveCurrentUserUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): StateFlow<AuthUser?> = authRepository.currentUser
}
