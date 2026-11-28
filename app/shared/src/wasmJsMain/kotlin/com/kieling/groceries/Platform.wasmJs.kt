package com.kieling.groceries

import kotlin.js.ExperimentalWasmJsInterop

class WasmJsPlatform : Platform {
    override val name: String = "WasmJS"
}

actual fun getPlatform(): Platform = WasmJsPlatform()

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getCurrentTimestamp(): Long = js("Date.now()")
