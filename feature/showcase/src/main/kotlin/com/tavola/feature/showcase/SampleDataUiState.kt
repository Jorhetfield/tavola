package com.tavola.feature.showcase

/** What the last sample-data action did, so the section can tell the developer. */
enum class SampleDataResult { Loaded, Cleared, Failed }

/** @property isWorking an action is running, so the buttons are disabled. */
data class SampleDataUiState(val isWorking: Boolean = false, val lastResult: SampleDataResult? = null)
