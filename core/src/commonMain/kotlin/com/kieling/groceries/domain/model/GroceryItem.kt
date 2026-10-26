package com.kieling.groceries.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GroceryItem(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false,
)
