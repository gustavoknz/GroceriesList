package com.kieling.groceries.domain.repository

import com.kieling.groceries.domain.model.GroceryItem
import kotlinx.coroutines.flow.StateFlow

interface GroceryRepository {
    val items: StateFlow<List<GroceryItem>>

    suspend fun getItems(): List<GroceryItem>

    suspend fun addItem(name: String, group: String = "General")

    suspend fun toggleItem(id: Int)

    suspend fun deleteItem(id: Int)

    suspend fun renameGroup(oldName: String, newName: String)
}
