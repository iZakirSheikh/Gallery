package com.prime.gallery.impl.vms

import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prime.gallery.core.db.Folder
import com.prime.gallery.folders.Folders
import com.prime.gallery.impl.Repository
import com.prime.gallery.impl.SystemDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import javax.inject.Inject


private val Folder.firstTitleChar
    inline get() = name.uppercase(Locale.ROOT)[0].toString()
private const val TAG = "FoldersViewModel"
private val URI = MediaStore.Files.getContentUri("external")

//FixMe: Move the ViewModel Impl to separate package.
@HiltViewModel
class FoldersViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    private val delegate: SystemDelegate,
) : ViewModel(), Folders, SystemDelegate by delegate {
    val trigger = MutableStateFlow(false)
    var _ascending: Boolean by mutableStateOf(false)
    var _order: Int by mutableIntStateOf(Folders.ORDER_BY_NAME)

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


    override val folders: StateFlow<List<Folder>?> =
        repository
            // Observe the changes in the URI
            .observe(URI)
            // Observe the changes in trigger also
            .combine(trigger) { _, _ ->
                val folders = repository.getFolders()
                val result = when (_order) {
                    Folders.ORDER_BY_SIZE -> folders.sortedBy { it.size }
                    Folders.ORDER_BY_DATE_MODIFIED -> folders.sortedBy { it.lastModified }
                    Folders.ORDER_BY_NAME -> folders.sortedBy { it.name }
                    else -> error("Oops invalid id passed $_order")
                }
                if (ascending) result else result.reversed()
            }
            //Catch any exceptions in upstream flow and emit using the snackbar.
            .catch {
                emit(emptyList())   // push empty list
                Log.d(TAG, "error: $it ")
            }
            // Convert to state.
            .stateIn(viewModelScope, started = SharingStarted.Lazily, null)
}