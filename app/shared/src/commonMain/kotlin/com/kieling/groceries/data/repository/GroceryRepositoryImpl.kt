package com.kieling.groceries.data.repository

import com.kieling.groceries.GroceryItem
import com.kieling.groceries.domain.repository.GroceryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroceryRepositoryImpl : GroceryRepository {
    private val _items =
        MutableStateFlow(
            listOf(
                GroceryItem(1, "Apples"),
                GroceryItem(2, "Milk"),
                GroceryItem(3, "Bread"),
                GroceryItem(4, "Eggs"),
                GroceryItem(5, "Cheese"),
                GroceryItem(6, "Coffee"),
            ),
        )
    override val items: StateFlow<List<GroceryItem>> = _items.asStateFlow()

    override suspend fun getItems(): List<GroceryItem> = _items.value

    override suspend fun addItem(name: String) {
        val currentList = _items.value
        val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        _items.value = currentList + GroceryItem(id = newId, name = name.trim())
    }

    override suspend fun toggleItem(id: Int) {
        _items.value =
            _items.value.map { item ->
                if (item.id == id) item.copy(isChecked = !item.isChecked) else item
            }
    }

    override suspend fun deleteItem(id: Int) {
        _items.value = _items.value.filter { it.id != id }
    }
}
