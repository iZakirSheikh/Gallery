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
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.zs.api.store.MediaFile
import com.zs.api.store.MediaProvider
import com.zs.api.util.PathUtils
import com.zs.gallery.files.FolderViewState
import com.zs.gallery.files.RouteFolder
import kotlinx.coroutines.delay

private const val TAG = "FolderViewModel"

class FolderViewModel(
    handle: SavedStateHandle, provider: MediaProvider
) : TimelineViewModel(provider), FolderViewState {

    val path = RouteFolder.get(handle)

    override suspend fun fetch(): Map<String, List<MediaFile>> {
        // Workaround: Parameter is unexpectedly null on the first call.
        // Root cause unknown.delay(100L)
        delay(50)
        Log.d(TAG, "fetch: Path - $path")
        return provider.fetchFilesFromDirectory(
            order = MediaProvider.COLUMN_DATE_MODIFIED, path = path, ascending = false
        ).groupBy {
            DateUtils.getRelativeTimeSpanString(
                it.dateModified, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS
            ).toString()
        }
    }

    override val title: CharSequence = PathUtils.name(path)
}