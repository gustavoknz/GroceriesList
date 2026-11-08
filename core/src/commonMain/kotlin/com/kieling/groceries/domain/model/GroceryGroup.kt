package com.kieling.groceries.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GroceryGroup(
    val name: String,
    val items: List<GroceryItem> = emptyList(),
)

fun List<GroceryItem>.toGroups(): List<GroceryGroup> =
    groupBy { it.group.ifBlank { "General" } }
        .map { (groupName, items) -> GroceryGroup(name = groupName, items = items) }
