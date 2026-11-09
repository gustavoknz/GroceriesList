package com.kieling.groceries

import kotlinx.browser.window

actual fun getApiBaseUrl(): String =
    try {
        window.location.origin.takeIf { (it.isNotBlank()) && (it != "null") }
            ?: "https://grocerieslist-27j6.onrender.com"
    } catch (_: Throwable) {
        "https://grocerieslist-27j6.onrender.com"
    }
