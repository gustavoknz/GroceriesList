package com.kieling.groceries.domain.repository

import com.kieling.groceries.domain.model.GroceryItem
import kotlinx.coroutines.flow.StateFlow

interface GroceryRepository {
    val items: StateFlow<List<GroceryItem>>
    val categoryOrder: StateFlow<List<String>>

    suspend fun getItems(): List<GroceryItem>

    suspend fun getCategories(): List<String>

    suspend fun addItem(
        name: String,
        group: String = "General",
    )

    suspend fun addCategory(name: String)

    suspend fun reorderCategories(newOrder: List<String>)

    suspend fun toggleItem(id: Int)

    suspend fun deleteItem(id: Int)

    suspend fun renameGroup(
        oldName: String,
        newName: String,
    )

    suspend fun renameItem(
        id: Int,
        newName: String,
    )
}
