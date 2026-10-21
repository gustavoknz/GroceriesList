package com.kieling.groceries

actual object Logger {
    actual fun d(
        tag: String,
        message: String,
    ) {
        println("[$tag] DEBUG: $message")
    }

    actual fun i(
        tag: String,
        message: String,
    ) {
        println("[$tag] INFO: $message")
    }

    actual fun w(
        tag: String,
        message: String,
    ) {
        println("[$tag] WARN: $message")
    }

    actual fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        println("[$tag] ERROR: $message")
        throwable?.printStackTrace()
    }
}
