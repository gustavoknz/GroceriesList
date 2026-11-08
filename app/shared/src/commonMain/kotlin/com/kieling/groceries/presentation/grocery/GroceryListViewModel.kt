package com.kieling.groceries.presentation.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieling.groceries.domain.model.AuthUser
import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.usecase.AddGroceryItemUseCase
import com.kieling.groceries.domain.usecase.DeleteGroceryItemUseCase
import com.kieling.groceries.domain.usecase.GetGroceryItemsUseCase
import com.kieling.groceries.domain.usecase.ObserveCurrentUserUseCase
import com.kieling.groceries.domain.usecase.RenameGroupUseCase
import com.kieling.groceries.domain.usecase.SignInWithGoogleUseCase
import com.kieling.groceries.domain.usecase.SignOutUseCase
import com.kieling.groceries.domain.usecase.ToggleGroceryItemUseCase
import com.kieling.groceries.presentation.model.GroceryGroupUi
import com.kieling.groceries.presentation.model.toUi
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroceryListViewModel(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    getGroceryItemsUseCase: GetGroceryItemsUseCase,
    private val addGroceryItemUseCase: AddGroceryItemUseCase,
    private val toggleGroceryItemUseCase: ToggleGroceryItemUseCase,
    private val deleteGroceryItemUseCase: DeleteGroceryItemUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val renameGroupUseCase: RenameGroupUseCase,
) : ViewModel() {
    private val newItemNameFlow = MutableStateFlow("")
    private val newItemGroupFlow = MutableStateFlow("")

    val uiState: StateFlow<GroceryListUiState> =
        combine(
            observeCurrentUserUseCase(),
            getGroceryItemsUseCase(),
            newItemNameFlow,
            newItemGroupFlow,
        ) { user: AuthUser?, items: List<GroceryItem>, newItemName: String, newItemGroup: String ->
            val uiItems = items.map { it.toUi() }
            val groups =
                items
                    .groupBy { it.group.ifBlank { "General" } }
                    .map { (groupName, groupItems) ->
                        GroceryGroupUi(
                            name = groupName,
                            items = groupItems.map { it.toUi() }.toImmutableList(),
                        )
                    }.toImmutableList()

            GroceryListUiState(
                currentUser = user?.toUi(),
                items = uiItems.toImmutableList(),
                groups = groups,
                newItemName = newItemName,
                newItemGroup = newItemGroup,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GroceryListUiState(),
        )

    fun updateNewItemName(name: String) {
        newItemNameFlow.value = name
    }

    fun updateNewItemGroup(group: String) {
        newItemGroupFlow.value = group
    }

    fun addItem(group: String? = null) {
        val name = newItemNameFlow.value.trim()
        val targetGroup = (group ?: newItemGroupFlow.value).trim().ifBlank { "General" }
        if (name.isNotBlank()) {
            viewModelScope.launch {
                addGroceryItemUseCase(name, targetGroup)
                newItemNameFlow.value = ""
            }
        }
    }

    fun toggleItem(id: Int) {
        viewModelScope.launch {
            toggleGroceryItemUseCase(id)
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            deleteGroceryItemUseCase(id)
        }
    }

    fun renameGroup(
        oldName: String,
        newName: String,
    ) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank() && (trimmed != oldName)) {
            viewModelScope.launch {
                renameGroupUseCase(oldName, trimmed)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            signInWithGoogleUseCase()
        }
    }
}
