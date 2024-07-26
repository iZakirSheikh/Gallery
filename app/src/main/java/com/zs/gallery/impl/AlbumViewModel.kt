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
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.primex.core.withSpanStyle
import com.zs.api.store.MediaFile
import com.zs.api.store.MediaProvider
import com.zs.gallery.R
import com.zs.gallery.files.AlbumViewState
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "AlbumViewModel"

class AlbumViewModel(provider: MediaProvider) : TimelineViewModel(provider), AlbumViewState {
    override var title: CharSequence  by mutableStateOf(getText(R.string.favourites))


    override suspend fun update() {
        delay(50)
        val ids = favourites.value
        if (ids.isEmpty()) return

        title = buildAnnotatedString {
            appendLine(getText(R.string.favourites))
            withSpanStyle(fontSize = 10.sp) {
                append("${favourites.value.size} Files")
            }
        }

        data = provider.fetchFiles(
            *ids.toLongArray(),
            order = MediaProvider.COLUMN_DATE_MODIFIED,
            ascending = false
        ).groupBy {
            DateUtils.getRelativeTimeSpanString(
                it.dateModified,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS
            ).toString()
        }
    }

    override fun remove() {
        val selected = consume()
        // Get the current list of favorite items
        val oldList = favourites.value
        // Filter the old list to exclude selected items, creating a new list
        val newList = oldList.filterNot { it in selected }
        // Update the preferences with the new list of favorite items
        preferences[Settings.KEY_FAVOURITE_FILES] = newList// Invalidate the current state to trigger recomposition
        invalidate()
    }
}