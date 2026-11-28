package com.kieling.groceries

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getCurrentTimestamp(): Long = System.currentTimeMillis()
