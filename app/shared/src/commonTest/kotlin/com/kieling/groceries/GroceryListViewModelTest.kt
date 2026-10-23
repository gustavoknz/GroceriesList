package com.kieling.groceries

import com.kieling.groceries.data.repository.AuthRepositoryImpl
import com.kieling.groceries.data.repository.GroceryRepositoryImpl
import com.kieling.groceries.domain.usecase.*
import com.kieling.groceries.presentation.grocery.GroceryListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class GroceryListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val groceryRepo = GroceryRepositoryImpl()
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
