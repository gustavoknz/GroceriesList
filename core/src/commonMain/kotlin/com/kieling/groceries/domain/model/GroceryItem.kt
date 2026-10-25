package com.kieling.groceries.domain.model

data class GroceryItem(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false,
)
