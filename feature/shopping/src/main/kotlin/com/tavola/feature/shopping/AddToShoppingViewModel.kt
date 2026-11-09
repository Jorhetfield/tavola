package com.tavola.feature.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.ShoppingCandidate
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingCategorySuggester
import com.tavola.domain.model.ShoppingItem
import com.tavola.domain.model.ShoppingNames
import com.tavola.domain.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Adds to the shopping list the things another feature offers (the ingredients of a recipe): every one that is not on the list yet
 * starts picked and the person unpicks what they already have at home. Each gets the category guessed from its name.
 */
@HiltViewModel
class AddToShoppingViewModel @Inject constructor(private val shoppingRepository: ShoppingRepository) : ViewModel() {
    private val candidates = MutableStateFlow<List<ShoppingCandidate>>(emptyList())
    private val unpicked = MutableStateFlow<Set<Int>>(emptySet())
    private val saving = MutableStateFlow(false)
    private val retryCount = MutableStateFlow(0)

    /** The items of the list that are still to buy; `null` if the repository failed to load them. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val itemsToBuy = retryCount.flatMapLatest {
        shoppingRepository.observeItems()
            .map<List<ShoppingItem>, List<ShoppingItem>?> { loaded -> loaded.filter { item -> !item.checked } }
            .catch { emit(null) }
    }

    val uiState: StateFlow<AddToShoppingUiState> = combine(candidates, itemsToBuy, unpicked, saving) { offered, toBuy, skipped, isSaving ->
        if (toBuy == null) {
            AddToShoppingUiState.Error
        } else {
            AddToShoppingUiState.Success(
                rows = offered.mapIndexed { index, candidate ->
                    val onList = toBuy.any { item -> ShoppingNames.same(item.name, candidate.name) }
                    AddToShoppingRow(index, candidate, onList, selected = !onList && index !in skipped)
                },
                saving = isSaving,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AddToShoppingUiState.Loading)

    /** What is offered now. A new sheet starts with everything picked again. */
    fun onCandidates(offered: List<ShoppingCandidate>) {
        candidates.value = offered
        unpicked.value = emptySet()
    }

    fun onToggle(index: Int, selected: Boolean) = unpicked.update { skipped -> if (selected) skipped - index else skipped + index }

    /** Picks everything that can be picked, or, if it all is already, nothing. */
    fun onSelectAllToggle() {
        val state = uiState.value as? AddToShoppingUiState.Success ?: return
        unpicked.value = if (state.allSelected) state.rows.filter { row -> !row.onList }.map { row -> row.index }.toSet() else emptySet()
    }

    fun onRetryClick() = retryCount.update { count -> count + 1 }

    /**
     * Adds what is picked and tells [onAdded] how many were added, so the sheet can close and say so. If something fails nothing
     * is announced and the sheet stays; what was already saved shows up as "on the list", so trying again adds only the rest.
     */
    fun onAddClick(onAdded: (count: Int) -> Unit) {
        val state = uiState.value as? AddToShoppingUiState.Success ?: return
        val picked = state.rows.filter { row -> row.selected }
        if (picked.isEmpty() || state.saving) return
        saving.value = true
        viewModelScope.launch {
            try {
                picked.forEach { row ->
                    val name = row.candidate.name
                    val category = ShoppingCategorySuggester.suggest(name) ?: ShoppingCategory.Other
                    shoppingRepository.saveItem(ShoppingItem(id = "", name = name, category = category, quantity = row.candidate.quantity))
                }
                onAdded(picked.size)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Not everything was added; the sheet stays so it can be tried again.
            } finally {
                saving.value = false
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
