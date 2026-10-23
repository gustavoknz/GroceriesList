package com.kieling.groceries

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kieling.groceries.di.appModule
import org.koin.core.context.startKoin

@Composable
@Preview
fun App() {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            startKoin {
                modules(appModule)
            }
        } catch (e: Exception) {
            // Already started
        }
    }

    MaterialTheme {
        GroceryListScreen()
    }
}
