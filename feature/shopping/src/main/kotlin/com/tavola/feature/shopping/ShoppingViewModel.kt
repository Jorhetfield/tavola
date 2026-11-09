package com.tavola.feature.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem
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

/** The shopping list tab: the items grouped by category, a quick add field, ticking, and removing with undo. */
@HiltViewModel
class ShoppingViewModel @Inject constructor(private val shoppingRepository: ShoppingRepository) : ViewModel() {
    private val draft = MutableStateFlow(AddItemDraft())
    private val retryCount = MutableStateFlow(0)
    private val removedItems = MutableStateFlow<List<ShoppingItem>>(emptyList())

    /** Every item; `null` if the repository failed to load them. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val items = retryCount.flatMapLatest {
        shoppingRepository.observeItems()
            .map<List<ShoppingItem>, List<ShoppingItem>?> { loaded -> loaded }
            .catch { emit(null) }
    }

    val uiState: StateFlow<ShoppingUiState> = combine(items, draft, removedItems) { all, typed, removed ->
        if (all == null) {
            ShoppingUiState.Error
        } else {
            ShoppingUiState.Success(sections = all.groupedByCategory(), draft = typed, removedItems = removed)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ShoppingUiState.Loading)

    /** The text is cut to the longest name an item can have, so what is typed is always what is saved. Typing clears the "added" notice. */
    fun onDraftNameChange(text: String) =
        draft.update { typed -> typed.copy(name = text.take(ShoppingItem.MAX_NAME_LENGTH), addedName = null) }

    fun onDraftQuantityChange(text: String) = draft.update { typed -> typed.copy(quantity = text.take(ShoppingItem.MAX_QUANTITY_LENGTH)) }

    /** The person picked a category themselves, which wins over the one guessed from the name. */
    fun onDraftCategoryPick(category: ShoppingCategory) = draft.update { typed -> typed.copy(pickedCategory = category) }

    /** The sheet that adds items was closed: what was typed is dropped, so the next time it opens it is empty. */
    fun onAddSheetDismiss() = draft.update { AddItemDraft() }

    fun onRetryClick() = retryCount.update { count -> count + 1 }

    /**
     * Adds what is typed to the list and empties the draft at once, so the next item can be typed (the sheet stays open); the
     * category goes back to being guessed for each one, and [AddItemDraft.addedName] tells what was just added. If saving fails
     * the draft comes back (unless something else was typed meanwhile).
     */
    fun onAddClick() {
        val typed = draft.value
        val name = typed.name.trim()
        if (name.isEmpty()) return
        draft.value = AddItemDraft(addedName = name)
        viewModelScope.launch {
            try {
                shoppingRepository.saveItem(ShoppingItem(id = "", name = name, category = typed.category, quantity = typed.quantity))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                draft.update { current -> if (current.name.isEmpty()) typed else current }
            }
        }
    }

    fun onItemCheckedChange(itemId: String, checked: Boolean) {
        viewModelScope.launch {
            try {
                shoppingRepository.setChecked(itemId, checked)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Not saved, so the item stays as the list says.
            }
        }
    }

    fun onItemRemove(itemId: String) {
        val item = currentItems().firstOrNull { shown -> shown.id == itemId } ?: return
        remove(listOf(item))
    }

    /** Removes every ticked item, which is what is done after the shopping is done. */
    fun onRemoveCheckedClick() {
        val ticked = currentItems().filter { item -> item.checked }
        if (ticked.isNotEmpty()) remove(ticked)
    }

    /** Saving the items again brings them back with the same id and time. If that fails the notice comes back so Undo can be tried again. */
    fun onRemoveUndo() {
        val items = removedItems.value
        if (items.isEmpty()) return
        removedItems.value = emptyList()
        viewModelScope.launch {
            try {
                items.forEach { item -> shoppingRepository.saveItem(item) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                removedItems.value = items
            }
        }
    }

    /** The "removed" notice ended without undo: there is nothing left to restore. */
    fun onRemovedShown() {
        removedItems.value = emptyList()
    }

    /** Removes [items] and, once they are gone, keeps them in [removedItems] so it can be undone. If it fails nothing is announced. */
    private fun remove(items: List<ShoppingItem>) {
        viewModelScope.launch {
            try {
                shoppingRepository.removeItems(items.map { item -> item.id })
                removedItems.value = items
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Not removed, so the items stay where they are.
            }
        }
    }

    private fun currentItems(): List<ShoppingItem> =
        (uiState.value as? ShoppingUiState.Success)?.sections?.flatMap { section -> section.items }.orEmpty()

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
