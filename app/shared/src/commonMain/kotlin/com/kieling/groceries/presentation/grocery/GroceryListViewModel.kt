package com.kieling.groceries.presentation.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieling.groceries.AuthUser
import com.kieling.groceries.GroceryItem
import com.kieling.groceries.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val currentUser: StateFlow<AuthUser?> = observeCurrentUserUseCase()
    val items: StateFlow<List<GroceryItem>> = getGroceryItemsUseCase()

    private val _newItemName = MutableStateFlow("")
    val newItemName: StateFlow<String> = _newItemName.asStateFlow()

    fun updateNewItemName(name: String) {
        _newItemName.value = name
    }

    fun addItem() {
        val name = _newItemName.value
        if (name.isNotBlank()) {
            viewModelScope.launch {
                addGroceryItemUseCase(name)
                _newItemName.value = ""
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
