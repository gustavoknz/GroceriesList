package com.kieling.groceries.presentation.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieling.groceries.domain.model.AuthUser
import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.repository.GroceryRepository
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
    private val groceryRepository: GroceryRepository,
) : ViewModel() {
    private val deletedItemFlow = MutableStateFlow<GroceryItem?>(null)

    private val baseUiState: StateFlow<GroceryListUiState> =
        combine(
            observeCurrentUserUseCase(),
            getGroceryItemsUseCase(),
            groceryRepository.categoryOrder,
        ) { user: AuthUser?, items: List<GroceryItem>, categoryOrder: List<String> ->
            val uiItems = items.map { it.toUi() }
            val groupedItems = items.groupBy { it.group.ifBlank { "General" } }
            val allGroupNames = (categoryOrder + groupedItems.keys).distinct()
            val groups =
                allGroupNames
                    .map { groupName ->
                        val groupItems = groupedItems[groupName] ?: emptyList()
                        GroceryGroupUi(
                            name = groupName,
                            items = groupItems.map { it.toUi() }.toImmutableList(),
                        )
                    }.filter { it.items.isNotEmpty() || categoryOrder.contains(it.name) }
                    .toImmutableList()

            GroceryListUiState(
                currentUser = user?.toUi(),
                items = uiItems.toImmutableList(),
                groups = groups,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GroceryListUiState(),
        )

    val uiState: StateFlow<GroceryListUiState> =
        combine(baseUiState, deletedItemFlow) { base, deleted ->
            base.copy(lastDeletedItemName = deleted?.name)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GroceryListUiState(),
        )

    fun toggleItem(id: Int) {
        viewModelScope.launch {
            toggleGroceryItemUseCase(id)
        }
    }

    fun deleteItem(id: Int) {
        val currentItems = groceryRepository.items.value
        val itemToDelete = currentItems.find { it.id == id }
        if (itemToDelete != null) {
            deletedItemFlow.value = itemToDelete
            viewModelScope.launch {
                deleteGroceryItemUseCase(id)
            }
        }
    }

    fun undoDelete() {
        val itemToRestore = deletedItemFlow.value
        if (itemToRestore != null) {
            deletedItemFlow.value = null
            viewModelScope.launch {
                addGroceryItemUseCase(itemToRestore.name, itemToRestore.group)
            }
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

    fun renameItem(
        id: Int,
        newName: String,
    ) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                groceryRepository.renameItem(id, trimmed)
            }
        }
    }

    fun addItemToGroup(
        groupName: String,
        itemName: String,
    ) {
        val trimmed = itemName.trim()
        val targetGroup = groupName.trim().ifBlank { "General" }
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                addGroceryItemUseCase(trimmed, targetGroup)
            }
        }
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                groceryRepository.addCategory(trimmed)
            }
        }
    }

    fun reorderCategories(newOrder: List<String>) {
        viewModelScope.launch {
            groceryRepository.reorderCategories(newOrder)
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
