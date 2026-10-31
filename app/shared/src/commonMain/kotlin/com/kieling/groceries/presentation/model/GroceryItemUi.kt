package com.kieling.groceries.presentation.model

import androidx.compose.runtime.Immutable
import com.kieling.groceries.domain.model.GroceryGroup
import com.kieling.groceries.domain.model.GroceryItem

@Immutable
data class GroceryItemUi(
    val id: Int,
    val name: String,
    val isChecked: Boolean,
    val group: String,
)

@Immutable
data class GroceryGroupUi(
    val name: String,
    val items: List<GroceryItemUi>,
)

fun GroceryItem.toUi(): GroceryItemUi =
    GroceryItemUi(
        id = id,
        name = name,
        isChecked = isChecked,
        group = group,
    )

fun GroceryGroup.toUi(): GroceryGroupUi =
    GroceryGroupUi(
        name = name,
        items = items.map { it.toUi() },
    )
