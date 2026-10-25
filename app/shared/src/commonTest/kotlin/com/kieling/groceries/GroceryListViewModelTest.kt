package com.kieling.groceries

import com.kieling.groceries.data.repository.AuthRepositoryImpl
import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.repository.GroceryRepository
import com.kieling.groceries.domain.usecase.AddGroceryItemUseCase
import com.kieling.groceries.domain.usecase.DeleteGroceryItemUseCase
import com.kieling.groceries.domain.usecase.GetGroceryItemsUseCase
import com.kieling.groceries.domain.usecase.ObserveCurrentUserUseCase
import com.kieling.groceries.domain.usecase.SignInWithGoogleUseCase
import com.kieling.groceries.domain.usecase.SignOutUseCase
import com.kieling.groceries.domain.usecase.ToggleGroceryItemUseCase
import com.kieling.groceries.presentation.grocery.GroceryListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeGroceryRepository : GroceryRepository {
    private val _items =
        MutableStateFlow(
            listOf(
                GroceryItem(1, "Apples"),
                GroceryItem(2, "Milk"),
                GroceryItem(3, "Bread"),
            ),
        )
    override val items: StateFlow<List<GroceryItem>> = _items.asStateFlow()

    override suspend fun getItems(): List<GroceryItem> = _items.value

    override suspend fun addItem(name: String) {
        val current = _items.value
        val newItem = GroceryItem(current.size + 1, name)
        _items.value = current + newItem
    }

    override suspend fun toggleItem(id: Int) {
        _items.value = _items.value.map { if (it.id == id) it.copy(isChecked = !it.isChecked) else it }
    }

    override suspend fun deleteItem(id: Int) {
        _items.value = _items.value.filter { it.id != id }
    }
}

class GroceryListViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val groceryRepo = FakeGroceryRepository()
    private val authRepo = AuthRepositoryImpl()
    private lateinit var viewModel: GroceryListViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel =
            GroceryListViewModel(
                observeCurrentUserUseCase = ObserveCurrentUserUseCase(authRepo),
                getGroceryItemsUseCase = GetGroceryItemsUseCase(groceryRepo),
                addGroceryItemUseCase = AddGroceryItemUseCase(groceryRepo),
                toggleGroceryItemUseCase = ToggleGroceryItemUseCase(groceryRepo),
                deleteGroceryItemUseCase = DeleteGroceryItemUseCase(groceryRepo),
                signOutUseCase = SignOutUseCase(authRepo),
                signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepo),
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialItemsLoaded() =
        runTest {
            val items = viewModel.uiState.value.items
            assertTrue(items.isNotEmpty())
        }

    @Test
    fun testAddGroceryItem() =
        runTest {
            viewModel.updateNewItemName("Bananas")
            viewModel.addItem()
            advanceUntilIdle()

            val items = viewModel.uiState.value.items
            assertTrue(items.any { it.name == "Bananas" })
            assertEquals("", viewModel.uiState.value.newItemName)
        }

    @Test
    fun testToggleGroceryItem() =
        runTest {
            val initialItem =
                viewModel.uiState.value.items
                    .first()
            val initialChecked = initialItem.isChecked

            viewModel.toggleItem(initialItem.id)
            advanceUntilIdle()

            val updatedItem =
                viewModel.uiState.value.items
                    .first { it.id == initialItem.id }
            assertEquals(!initialChecked, updatedItem.isChecked)
        }

    @Test
    fun testDeleteGroceryItem() =
        runTest {
            val targetItem =
                viewModel.uiState.value.items
                    .first()
            viewModel.deleteItem(targetItem.id)
            advanceUntilIdle()

            val items = viewModel.uiState.value.items
            assertFalse(items.any { it.id == targetItem.id })
        }
}
