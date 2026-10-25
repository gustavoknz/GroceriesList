package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.repository.GroceryRepository
import kotlinx.coroutines.flow.StateFlow

class GetGroceryItemsUseCase(
    private val groceryRepository: GroceryRepository,
) {
    operator fun invoke(): StateFlow<List<GroceryItem>> = groceryRepository.items
}
