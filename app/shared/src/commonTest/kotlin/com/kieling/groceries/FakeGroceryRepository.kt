package com.kieling.groceries

import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.repository.GroceryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeGroceryRepository : GroceryRepository {
    private val _items =
        MutableStateFlow(
            listOf(
                GroceryItem(1, "Apples"),
                GroceryItem(2, "Milk"),
                GroceryItem(3, "Bread"),
            ),
        )
    override val items: StateFlow<List<GroceryItem>> = _items.asStateFlow()

    private val _categoryOrder =
        MutableStateFlow(listOf("Fruits", "Dairy", "Bakery", "Beverages", "General"))
    override val categoryOrder: StateFlow<List<String>> = _categoryOrder.asStateFlow()

    override suspend fun getItems(): List<GroceryItem> = _items.value

    override suspend fun getCategories(): List<String> = _categoryOrder.value

    override suspend fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && !_categoryOrder.value.contains(trimmed)) {
            _categoryOrder.value += trimmed
        }
    }

    override suspend fun reorderCategories(newOrder: List<String>) {
        _categoryOrder.value = newOrder
    }

    override suspend fun addItem(
        name: String,
        group: String,
    ) {
        val trimmedName = name.trim()
        val normalizedName = trimmedName.lowercase()
        val targetGroup = group.trim().ifBlank { "General" }
        if (!_categoryOrder.value.contains(targetGroup)) {
            _categoryOrder.value += targetGroup
        }
        val current = _items.value
        val existingIndex =
            current.indexOfFirst {
                it.name.trim().lowercase() == normalizedName && it.group == targetGroup
            }
        _items.value =
            if (existingIndex != -1) {
                current.mapIndexed { index, item ->
                    if (index == existingIndex) {
                        item.copy(name = trimmedName, isChecked = false)
                    } else {
                        item
                    }
                }
            } else {
                val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
                val newItem = GroceryItem(newId, trimmedName, group = targetGroup)
                current + newItem
            }
    }

    override suspend fun toggleItem(id: Int) {
        _items.value = _items.value.map { if (it.id == id) it.copy(isChecked = !it.isChecked) else it }
    }

    override suspend fun deleteItem(id: Int) {
        _items.value = _items.value.filter { it.id != id }
    }

    override suspend fun renameGroup(
        oldName: String,
        newName: String,
    ) {
        val trimmed = newName.trim()
        val categories = _categoryOrder.value.toMutableList()
        val index = categories.indexOf(oldName)
        if (index != -1) {
            categories[index] = trimmed
        } else if (!categories.contains(trimmed)) {
            categories.add(trimmed)
        }
        _categoryOrder.value = categories

        _items.value =
            _items.value.map { if (it.group == oldName) it.copy(group = trimmed) else it }
    }

    override suspend fun renameItem(
        id: Int,
        newName: String,
    ) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            _items.value =
                _items.value.map {
                    if (it.id == id) it.copy(name = trimmed) else it
                }
        }
    }
}
