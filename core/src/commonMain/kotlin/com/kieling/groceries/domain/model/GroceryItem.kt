package com.kieling.groceries.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GroceryItem(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false,
    val group: String = "General",
)

@Serializable
data class GroceryGroup(
    val name: String,
    val items: List<GroceryItem> = emptyList(),
)
