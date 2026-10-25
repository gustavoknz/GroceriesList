package com.kieling.groceries.presentation.model

import androidx.compose.runtime.Immutable
import com.kieling.groceries.domain.model.GroceryItem

@Immutable
data class GroceryItemUi(
    val id: Int,
    val name: String,
    val isChecked: Boolean,
)

fun GroceryItem.toUi(): GroceryItemUi =
    GroceryItemUi(
        id = id,
        name = name,
        isChecked = isChecked,
    )
