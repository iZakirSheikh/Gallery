/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 25-07-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.gallery.impl

import android.text.format.DateUtils
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.primex.core.withSpanStyle
import com.zs.api.store.MediaProvider
import com.zs.gallery.R
import com.zs.gallery.files.AlbumViewState
import kotlinx.coroutines.delay

private const val TAG = "AlbumViewModel"

class AlbumViewModel(
    provider: MediaProvider
) : TimelineViewModel(provider), AlbumViewState {

    override var title: CharSequence by mutableStateOf(getText(R.string.favourites))

    override suspend fun refresh() {
        // Introduce a slight delay (for potential visual feedback)
        delay(50)

        val ids = favorites
        // If no favorites, skip the refresh
        if (ids.isEmpty()) return

        // Update the title with the number of favorite files
        title = buildAnnotatedString {
            appendLine(getText(R.string.favourites))
            withSpanStyle(fontSize = 10.sp) {
                append("${favorites.size} Files")
            }
        }

        // Fetch favorite files from the provider, ordered by modification date
        values = provider.fetchFiles(
            *ids.toLongArray(),
            order = MediaProvider.COLUMN_DATE_MODIFIED,
            ascending = false
        )

        // Group the files by their relative time span (e.g., "Today", "Yesterday")
        data = values.groupBy {
            DateUtils.getRelativeTimeSpanString(
                it.dateModified,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS
            ).toString()
        }
    }


    // Since all items in this album are favorites, the selected items are also favorites.
    // Calling toggleLike will therefore remove them from the favorites list.
    override fun remove() {
        toggleLike()
        invalidate()
    }
}