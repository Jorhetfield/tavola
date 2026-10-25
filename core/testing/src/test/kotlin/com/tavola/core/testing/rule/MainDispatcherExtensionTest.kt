package com.tavola.core.testing.rule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MainDispatcherExtensionTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @Test
    fun `Dispatchers Main queda sustituido durante el test`() = runTest {
        var ran = false

        launch(Dispatchers.Main) { ran = true }

        assertTrue(ran)
    }
}
