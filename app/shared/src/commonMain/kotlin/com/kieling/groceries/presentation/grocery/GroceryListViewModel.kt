package com.kieling.groceries.presentation.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieling.groceries.AuthUser
import com.kieling.groceries.GroceryItem
import com.kieling.groceries.data.repository.AuthRepositoryImpl
import com.kieling.groceries.data.repository.GroceryRepositoryImpl
import com.kieling.groceries.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroceryListViewModel(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase = ObserveCurrentUserUseCase(AuthRepositoryImpl()),
    getGroceryItemsUseCase: GetGroceryItemsUseCase = GetGroceryItemsUseCase(GroceryRepositoryImpl()),
    private val addGroceryItemUseCase: AddGroceryItemUseCase = AddGroceryItemUseCase(GroceryRepositoryImpl()),
    private val toggleGroceryItemUseCase: ToggleGroceryItemUseCase = ToggleGroceryItemUseCase(GroceryRepositoryImpl()),
    private val deleteGroceryItemUseCase: DeleteGroceryItemUseCase = DeleteGroceryItemUseCase(GroceryRepositoryImpl()),
    private val signOutUseCase: SignOutUseCase = SignOutUseCase(AuthRepositoryImpl()),
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = SignInWithGoogleUseCase(AuthRepositoryImpl()),
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
