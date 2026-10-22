package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.GroceryRepository

class ToggleGroceryItemUseCase(private val groceryRepository: GroceryRepository) {
    suspend operator fun invoke(id: Int) = groceryRepository.toggleItem(id)
}
