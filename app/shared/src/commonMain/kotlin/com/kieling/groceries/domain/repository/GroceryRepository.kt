package com.kieling.groceries.domain.repository

import com.kieling.groceries.model.GroceryItem
import kotlinx.coroutines.flow.StateFlow

interface GroceryRepository {
    val items: StateFlow<List<GroceryItem>>

    suspend fun getItems(): List<GroceryItem>

    suspend fun addItem(name: String)

    suspend fun toggleItem(id: Int)

    suspend fun deleteItem(id: Int)
}
