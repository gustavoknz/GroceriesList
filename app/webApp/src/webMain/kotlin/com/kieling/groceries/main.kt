package com.kieling.groceries

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.kieling.groceries.di.appModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        startKoin {
            modules(appModule)
        }
    } catch (e: Exception) {
        // Already started
    }

    ComposeViewport {
        App()
    }
}
