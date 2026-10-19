package com.kieling.groceries

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform