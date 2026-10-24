package com.kieling.groceries

import androidx.compose.ui.window.ComposeViewport
import com.kieling.groceries.di.appModule
import org.koin.core.context.startKoin

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
