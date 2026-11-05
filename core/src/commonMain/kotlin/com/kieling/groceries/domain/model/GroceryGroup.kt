package com.kieling.groceries.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GroceryGroup(
    val name: String,
    val items: List<GroceryItem> = emptyList(),
)
