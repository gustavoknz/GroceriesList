package com.kieling.groceries

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kieling.groceries.presentation.grocery.GroceryListScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        GroceryListScreen()
    }
}
