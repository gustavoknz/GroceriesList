package com.kieling.groceries.data.model

import com.kieling.groceries.domain.model.GroceryItem
import kotlinx.serialization.Serializable

@Serializable
data class GroceryItemDto(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false,
)

fun GroceryItemDto.toDomain(): GroceryItem =
    GroceryItem(
        id = id,
        name = name,
        isChecked = isChecked,
    )

fun GroceryItem.toDto(): GroceryItemDto =
    GroceryItemDto(
        id = id,
        name = name,
        isChecked = isChecked,
    )
