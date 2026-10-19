package com.kieling.groceries

data class GroceryItem(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false
)
