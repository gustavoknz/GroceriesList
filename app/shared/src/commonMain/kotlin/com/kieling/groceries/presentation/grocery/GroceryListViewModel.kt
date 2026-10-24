package com.kieling.groceries.presentation.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieling.groceries.domain.usecase.AddGroceryItemUseCase
import com.kieling.groceries.domain.usecase.DeleteGroceryItemUseCase
import com.kieling.groceries.domain.usecase.GetGroceryItemsUseCase
import com.kieling.groceries.domain.usecase.ObserveCurrentUserUseCase
import com.kieling.groceries.domain.usecase.SignInWithGoogleUseCase
import com.kieling.groceries.domain.usecase.SignOutUseCase
import com.kieling.groceries.domain.usecase.ToggleGroceryItemUseCase
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
) : ViewModel() {
    private val newItemNameFlow = MutableStateFlow("")

    val uiState: StateFlow<GroceryListUiState> =
        combine(
            observeCurrentUserUseCase(),
            getGroceryItemsUseCase(),
            newItemNameFlow,
        ) { user, items, newItemName ->
            GroceryListUiState(
                currentUser = user,
                items = items.toImmutableList(),
                newItemName = newItemName,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GroceryListUiState(),
        )

    fun updateNewItemName(name: String) {
        newItemNameFlow.value = name
    }

    fun addItem() {
        val name = newItemNameFlow.value
        if (name.isNotBlank()) {
            viewModelScope.launch {
                addGroceryItemUseCase(name)
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
