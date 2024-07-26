/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 24-07-2024.
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
import android.text.format.Formatter
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import com.primex.core.withSpanStyle
import com.zs.api.store.MediaFile
import com.zs.api.store.MediaProvider
import com.zs.api.util.PathUtils
import com.zs.gallery.R
import com.zs.gallery.files.FolderViewState
import com.zs.gallery.files.RouteFolder
import kotlinx.coroutines.delay

private const val TAG = "FolderViewModel"

class FolderViewModel(
    handle: SavedStateHandle, provider: MediaProvider
) : TimelineViewModel(provider), FolderViewState {

    val path = RouteFolder[handle]

    override suspend fun update() {
        // Workaround: Parameter is unexpectedly null on the first call.
        // Root cause unknown
        delay(50)
        // Log the directory path that is about to be fetched
        Log.d(TAG, "fetch: Path - $path")

        // Fetch the files from the directory, ordered by last modified date in descending order
        val list = provider.fetchFilesFromDirectory(
            order = MediaProvider.COLUMN_DATE_MODIFIED,
            path = path,
            ascending = false
        )

        // Calculate the total size of all files and format it to a human readable string
        val size = formatFileSize(list.sumOf { it.size })

        // Get the number of files in the directory
        val count = list.size

        // Extract the name of the current directory from the path
        val name = PathUtils.name(path)

        // Build the title string for display, including the directory name, file countand total size
        title = buildAnnotatedString {
            appendLine(name)
            withSpanStyle(fontSize = 10.sp) {
                appendLine("$count files, $size")
            }
        }

        // Group the files by the relative time span string (e.g. "1 day ago", "2 hours ago")
        data = list.groupBy {
            DateUtils.getRelativeTimeSpanString(
                it.dateModified,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS
            ).toString()
        }
    }

    override var title: CharSequence by mutableStateOf(
       PathUtils.name(path)
    )
}