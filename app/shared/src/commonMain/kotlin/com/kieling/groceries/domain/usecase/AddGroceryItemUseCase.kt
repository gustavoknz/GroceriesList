package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.GroceryRepository

class AddGroceryItemUseCase(private val groceryRepository: GroceryRepository) {
    suspend operator fun invoke(name: String) = groceryRepository.addItem(name)
}
