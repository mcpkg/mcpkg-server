package org.mcpkg.server.tests

import junit.framework.TestCase
import kotlin.test.assertEquals

class MainTest : TestCase() {
    fun testAssert() : Unit {
        assertEquals("Hello, world!", "Hello, world!")
    }
}
