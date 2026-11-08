package com.kieling.groceries

import com.kieling.groceries.data.repository.AuthRepositoryImpl
import com.kieling.groceries.domain.usecase.AddGroceryItemUseCase
import com.kieling.groceries.domain.usecase.DeleteGroceryItemUseCase
import com.kieling.groceries.domain.usecase.GetGroceryItemsUseCase
import com.kieling.groceries.domain.usecase.ObserveCurrentUserUseCase
import com.kieling.groceries.domain.usecase.RenameGroupUseCase
import com.kieling.groceries.domain.usecase.SignInWithGoogleUseCase
import com.kieling.groceries.domain.usecase.SignOutUseCase
import com.kieling.groceries.domain.usecase.ToggleGroceryItemUseCase
import com.kieling.groceries.presentation.grocery.GroceryListViewModel
import kotlinx.coroutines.Dispatchers
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
                renameGroupUseCase = RenameGroupUseCase(groceryRepo),
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
            val groups = viewModel.uiState.value.groups
            assertTrue(groups.isNotEmpty())
        }

    @Test
    fun testAddGroceryItem() =
        runTest {
            viewModel.updateNewItemName("Bananas")
            viewModel.updateNewItemGroup("Produce")
            viewModel.addItem()
            advanceUntilIdle()

            val items = viewModel.uiState.value.items
            assertTrue(items.any { it.name == "Bananas" && it.group == "Produce" })
            assertEquals("", viewModel.uiState.value.newItemName)

            val groups = viewModel.uiState.value.groups
            assertTrue(groups.any { it.name == "Produce" })
        }

    @Test
    fun testRenameGroup() =
        runTest {
            viewModel.updateNewItemName("Carrots")
            viewModel.updateNewItemGroup("Veg")
            viewModel.addItem()
            advanceUntilIdle()

            viewModel.renameGroup("Veg", "Vegetables")
            advanceUntilIdle()

            val groups = viewModel.uiState.value.groups
            assertTrue(groups.any { it.name == "Vegetables" })
            assertFalse(groups.any { it.name == "Veg" })

            val items = viewModel.uiState.value.items
            assertTrue(items.any { it.name == "Carrots" && it.group == "Vegetables" })
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
