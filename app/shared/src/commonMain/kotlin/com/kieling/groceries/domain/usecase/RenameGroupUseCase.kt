package com.kieling.groceries.domain.usecase

import com.kieling.groceries.domain.repository.GroceryRepository

class RenameGroupUseCase(
    private val groceryRepository: GroceryRepository,
) {
    suspend operator fun invoke(oldName: String, newName: String) = groceryRepository.renameGroup(oldName, newName)
}
