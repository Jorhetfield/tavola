package com.tavola.feature.showcase

import app.cash.turbine.test
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.sample.SampleDataLoader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SampleDataViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val loader = RecordingSampleDataLoader()

    @Test
    fun `it starts idle with no result`() = runTest {
        assertEquals(SampleDataUiState(), SampleDataViewModel(loader).uiState.value)
    }

    @Test
    fun `loading calls the loader, blocks the buttons meanwhile and reports Loaded`() = runTest {
        loader.gate = CompletableDeferred()
        val viewModel = SampleDataViewModel(loader)

        viewModel.uiState.test {
            awaitItem()

            viewModel.onLoadClick()
            assertEquals(SampleDataUiState(isWorking = true), awaitItem())

            loader.gate?.complete(Unit)
            assertEquals(SampleDataUiState(isWorking = false, lastResult = SampleDataResult.Loaded), awaitItem())
            assertEquals(listOf("load"), loader.calls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing calls the loader and reports Cleared`() = runTest {
        val viewModel = SampleDataViewModel(loader)

        viewModel.onClearClick()

        assertEquals(SampleDataResult.Cleared, viewModel.uiState.value.lastResult)
        assertEquals(listOf("clear"), loader.calls)
    }

    @Test
    fun `a second click while working is ignored`() = runTest {
        loader.gate = CompletableDeferred()
        val viewModel = SampleDataViewModel(loader)

        viewModel.onLoadClick()
        viewModel.onClearClick()
        loader.gate?.complete(Unit)

        assertEquals(listOf("load"), loader.calls)
    }

    @Test
    fun `a failure is reported and the buttons come back`() = runTest {
        loader.failure = IllegalStateException("boom")
        val viewModel = SampleDataViewModel(loader)

        viewModel.onLoadClick()

        assertEquals(SampleDataUiState(isWorking = false, lastResult = SampleDataResult.Failed), viewModel.uiState.value)
    }

    private class RecordingSampleDataLoader : SampleDataLoader {
        val calls = mutableListOf<String>()
        var gate: CompletableDeferred<Unit>? = null
        var failure: Exception? = null

        override suspend fun loadSampleData() = record("load")

        override suspend fun clearAllData() = record("clear")

        private suspend fun record(call: String) {
            calls += call
            gate?.await()
            failure?.let { error -> throw error }
        }
    }
}
