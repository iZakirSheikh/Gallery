package com.zs.gallery.impl

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.zs.api.store.MediaProvider
import com.zs.gallery.folders.FoldersViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val TAG = "FoldersViewModel"

class FoldersViewModel(
    private val provider: MediaProvider
) : AbstractViewModel(),
    FoldersViewState {
        // Trigger for refreshing the list
    private val trigger = MutableStateFlow(false)
    private var _ascending: Boolean by mutableStateOf(false)
    private var _order: Int by mutableIntStateOf(FoldersViewState.ORDER_BY_DATE_MODIFIED)

    override var ascending: Boolean
        get() = _ascending
        set(value) {
            _ascending = value
            trigger.value = !trigger.value
        }

    override var order: Int
        get() = _order
        set(value) {
            _order = value
            trigger.value = !trigger.value
        }

    override val data = provider
        // Observe the changes in the URI
        .observer(MediaProvider.EXTERNAL_CONTENT_URI)
        // Observe the changes in trigger also
        .combine(trigger) { _, _ ->
            val folders = provider.getFolders()
            val result = when (_order) {
                FoldersViewState.ORDER_BY_SIZE -> folders.sortedBy { it.size }
                FoldersViewState.ORDER_BY_DATE_MODIFIED -> folders.sortedBy { it.lastModified }
                FoldersViewState.ORDER_BY_NAME -> folders.sortedBy { it.name }
                else -> error("Oops invalid id passed $_order")
            }
            if (ascending) result else result.reversed()
        }
        // Catch any exceptions in upstream flow and emit using the snackbar.
        .catch {
            emit(emptyList())   // push empty list
            Log.d(TAG, "error: $it ")
        }
        // Convert to state.
        .stateIn(viewModelScope, started = SharingStarted.Lazily, null)
}