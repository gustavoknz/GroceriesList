package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.GroceryRepository

class AddGroceryItemUseCase(
    private val groceryRepository: GroceryRepository,
) {
    suspend operator fun invoke(
        name: String,
        group: String = "General",
        addedBy: String = "",
    ) = groceryRepository.addItem(name, group, addedBy)
}
