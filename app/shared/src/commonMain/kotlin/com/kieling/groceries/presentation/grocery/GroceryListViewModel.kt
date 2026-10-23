package com.kieling.groceries.presentation.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieling.groceries.AuthUser
import com.kieling.groceries.GroceryItem
import com.kieling.groceries.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroceryListUiState(
    val currentUser: AuthUser? = null,
    val items: List<GroceryItem> = emptyList(),
    val newItemName: String = "",
)

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
                items = items,
                newItemName = newItemName,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
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
