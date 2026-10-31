package com.kieling.groceries.data.model

import com.kieling.groceries.domain.model.GroceryGroup
import com.kieling.groceries.domain.model.GroceryItem
import kotlinx.serialization.Serializable

@Serializable
data class GroceryItemDto(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false,
    val group: String = "General",
)

fun GroceryItemDto.toDomain(): GroceryItem =
    GroceryItem(
        id = id,
        name = name,
        isChecked = isChecked,
        group = group,
    )

fun GroceryItem.toDto(): GroceryItemDto =
    GroceryItemDto(
        id = id,
        name = name,
        isChecked = isChecked,
        group = group,
    )

fun List<GroceryItem>.toGroups(): List<GroceryGroup> =
    groupBy { it.group }
        .map { (groupName, items) -> GroceryGroup(name = groupName, items = items) }
