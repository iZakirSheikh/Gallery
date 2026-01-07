/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
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
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

package com.zs.gallery.common.impl

import android.text.format.DateUtils
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.zs.common.db.media.MediaProvider
import com.zs.common.db.media.Snapshot
import com.zs.gallery.common.NavKey
import com.zs.gallery.common.Res.action
import com.zs.gallery.files.FilesViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilesViewModel(
    provider: MediaProvider,
    override val key: NavKey,
) : KoinViewModel(), FilesViewState {

    val source = when (key) {
        NavKey.Timeline -> provider.folderSnapshotSource()
        else -> TODO("$key not implemented yet!")
    }

    // Reactive stream of paged Snapshot items.
    override val files: Flow<PagingData<Snapshot>> = Pager(
        // Configure paging behavior:
        // - pageSize = 20 → each page loads 20 items at a time.
        config = PagingConfig(pageSize = 20),

        // Provide the factory that creates the paging source.
        pagingSourceFactory = { source }
    )
        // Convert the Pager into a Flow so it can be collected reactively in Compose.
        .flow
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

    override val selected = mutableStateListOf<Long>()
    override val isInSelectionMode: Boolean = !selected.isEmpty()
    override val actions: List<action> = mutableStateListOf()

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun select(id: Long) {
        TODO("Not yet implemented")
    }

    override fun selectAll() {
        TODO("Not yet implemented")
    }
}