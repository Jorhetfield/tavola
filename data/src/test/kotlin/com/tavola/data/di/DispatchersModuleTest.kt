package com.tavola.data.di

import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class DispatchersModuleTest {
    @Test
    fun `cada calificador entrega su dispatcher estandar`() {
        assertSame(Dispatchers.IO, DispatchersModule.provideIoDispatcher())
        assertSame(Dispatchers.Default, DispatchersModule.provideDefaultDispatcher())
        assertSame(Dispatchers.Main, DispatchersModule.provideMainDispatcher())
    }
}
