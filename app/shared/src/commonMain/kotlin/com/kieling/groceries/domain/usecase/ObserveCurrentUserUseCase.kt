package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

class ObserveCurrentUserUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): StateFlow<AuthUser?> = authRepository.currentUser
}
