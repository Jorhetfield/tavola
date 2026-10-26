package com.tavola.feature.showcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.sample.SampleDataLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SampleDataViewModel @Inject constructor(private val sampleDataLoader: SampleDataLoader) : ViewModel() {
    private val _uiState = MutableStateFlow(SampleDataUiState())
    val uiState: StateFlow<SampleDataUiState> = _uiState.asStateFlow()

    fun onLoadClick() = run(SampleDataResult.Loaded) { sampleDataLoader.loadSampleData() }

    fun onClearClick() = run(SampleDataResult.Cleared) { sampleDataLoader.clearAllData() }

    private fun run(success: SampleDataResult, action: suspend () -> Unit) {
        if (_uiState.value.isWorking) return

        _uiState.update { state -> state.copy(isWorking = true) }
        viewModelScope.launch {
            val result = try {
                action()
                success
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                SampleDataResult.Failed
            }
            _uiState.update { SampleDataUiState(isWorking = false, lastResult = result) }
        }
    }
}
