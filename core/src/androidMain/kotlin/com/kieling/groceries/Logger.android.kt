package com.kieling.groceries

import android.util.Log

actual object Logger {
    actual fun d(
        tag: String,
        message: String,
    ) {
        try {
            Log.d(tag, message)
        } catch (_: Exception) {
            println("[$tag] $message")
        }
    }

    actual fun i(
        tag: String,
        message: String,
    ) {
        try {
            Log.i(tag, message)
        } catch (_: Exception) {
            println("[$tag] $message")
        }
    }

    actual fun w(
        tag: String,
        message: String,
    ) {
        try {
            Log.w(tag, message)
        } catch (_: Exception) {
            println("[$tag] $message")
        }
    }

    actual fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        try {
            Log.e(tag, message, throwable)
        } catch (_: Exception) {
            println("[$tag] $message: $throwable")
        }
    }
}
