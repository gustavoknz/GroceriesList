package com.kieling.groceries.presentation.grocery

import androidx.compose.runtime.Immutable
import com.kieling.groceries.AuthUser
import com.kieling.groceries.GroceryItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GroceryListUiState(
    val currentUser: AuthUser? = null,
    val items: ImmutableList<GroceryItem> = persistentListOf(),
    val newItemName: String = "",
)
