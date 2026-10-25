package com.kieling.groceries.presentation.grocery

import androidx.compose.runtime.Immutable
import com.kieling.groceries.presentation.model.AuthUserUi
import com.kieling.groceries.presentation.model.GroceryItemUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GroceryListUiState(
    val currentUser: AuthUserUi? = null,
    val items: ImmutableList<GroceryItemUi> = persistentListOf(),
    val newItemName: String = "",
)
