package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.GroceryRepository

class DeleteGroceryItemUseCase(private val groceryRepository: GroceryRepository) {
    suspend operator fun invoke(id: Int) = groceryRepository.deleteItem(id)
}
