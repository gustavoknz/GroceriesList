package com.kieling.groceries

import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingUtilTest {

    @Test
    fun testSayHelloStandard() {
        assertEquals("Hello, Ktor!", sayHello("Ktor"))
    }

    @Test
    fun testSayHelloAndroid() {
        assertEquals("Hello, Android!", sayHello("Android"))
    }

    @Test
    fun testSayHelloEmpty() {
        assertEquals("Hello, !", sayHello(""))
    }
}
