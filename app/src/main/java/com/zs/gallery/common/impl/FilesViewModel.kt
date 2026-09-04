/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 15 of Jan 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 15 of Jan 2026
 */

package com.zs.gallery.common.impl

import android.text.format.DateUtils
import android.util.Log
import androidx.collection.mutableIntSetOf
import androidx.compose.runtime.IntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.zs.domain.db.media.MediaProvider
import com.zs.domain.db.media.Snapshot
import com.zs.gallery.common.NavKey
import com.zs.gallery.common.Res
import com.zs.gallery.files.FilesViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch

class FilesViewModel(
    val key: NavKey.Files,
    val provider: MediaProvider
) : KoinViewModel(), FilesViewState {

    private val TAG = "FilesViewModel"

    override val meta: Pair<ImageVector?, CharSequence> =
        when(key.key){
            NavKey.Files.SRC_TIMELINE -> null to getText(Res.string.timeline)
            NavKey.Files.SRC_TRASH -> imageVector(Res.drawable.ic_recycling_filled) to getText(Res.string.trash)
            NavKey.Files.SRC_FAVOURITES -> imageVector(Res.drawable.ic_award_star_outline) to "Liked"
            else -> TODO("$key type not implemented yet!")
        }

    override val actions: List<Res.action> by derivedStateOf {
        buildList {
            val src = key.key
            this += Res.action.share; this += Res.action.restore; this += Res.action.select_all
        }
    }

    private val pager = Pager(
        // Configure paging behavior:
        // - pageSize = 20 → each page loads 20 items at a time.
        config = PagingConfig(pageSize = 20),

        // Provide the factory that creates the paging source.
        pagingSourceFactory = { provider.snapshots() }
    )
    override val data: Flow<PagingData<Snapshot>> =
        pager.flow // Reactive stream of paged Snapshot items.
            // Transform each emitted PagingData before it reaches the UI.
            .map { list ->
                // For each item in the paged list...
                list.map { item ->
                    // Attempt to interpret the item's header as a timestamp (Long).
                    // If parsing fails, leave the item unchanged.
                    val header = item.header?.toLongOrNull() ?: return@map item

                    // Replace the raw timestamp with a human-readable relative time string.
                    // Example: "5 minutes ago", "Yesterday", "3 days ago".
                    item.header = DateUtils.getRelativeTimeSpanString(
                        header,                       // The timestamp to format
                        System.currentTimeMillis(),   // Compare against current time
                        DateUtils.DAY_IN_MILLIS       // Minimum resolution: 1 day
                    ).toString()

                    // Return the updated item with its header transformed.
                    item
                }
            }
            // Cache the Flow in the ViewModel scope.
            // This ensures the paging data survives configuration changes
            // and avoids re-fetching when the UI is recreated.
            .cachedIn(viewModelScope)

    override val isInSelectionMode: Boolean by derivedStateOf { selected.isNotEmpty() }
    override val selected = mutableStateListOf<Long>()
    override fun clear() {
        selected.clear()
    }

    /**
     * Consumes the currently selected items and returns them as an array.
     *
     * This function creates a new array containing the selected items, clears the `selected` list, and returns the array.
     * @return An array containing the previously selected items.
     */
    fun consume(): LongArray {
        // Efficiently convert the list to an array.
        val data = selected.toLongArray()
        // Clear the selected items list.
        selected.clear()
        Log.d(TAG, "consume: ${data.size}")
        return data
    }

    override fun select(id: Long) {
        val contains = selected.contains(id)
        if (contains) selected.remove(id) else selected.add(id)
    }

    override fun select(key: String) {
        viewModelScope.launch {
            val list = data.first()
            list.
        }
    }

    override fun selectAll() {
        TODO("Not yet implemented")
    }

    override fun isGroupSelected(key: String): IntState {
        return mutableIntStateOf(0)
    }
}