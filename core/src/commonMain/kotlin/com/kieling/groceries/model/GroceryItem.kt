package com.kieling.groceries.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class GroceryItem(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false,
)
