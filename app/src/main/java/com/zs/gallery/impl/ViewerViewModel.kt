/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 23-07-2024.
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

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zs.api.store.MediaFile
import com.zs.api.store.MediaProvider
import com.zs.gallery.common.get
import com.zs.gallery.preview.RouteViewer
import com.zs.gallery.preview.ViewerArgs
import com.zs.gallery.preview.ViewerViewState
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class ViewerViewModel(
    handle: SavedStateHandle,
    provider: MediaProvider
) : MainViewModel<MediaFile>(provider), ViewerViewState {

    val args = handle[RouteViewer]
    override var focused by mutableLongStateOf(args.focused)
    override val data: List<MediaFile> get() = values
    override var values: List<MediaFile> by mutableStateOf(emptyList())
    override val favourite: Boolean by derivedStateOf {
        favorites.contains(focused)
    }
    override val isTrashView: Boolean get() = args is ViewerArgs.Trash

    override val MediaFile.id: Long get() = id

    override fun restore(activity: Activity) {
        select(focused)
        super.restore(activity)
    }

    override fun delete(activity: Activity) {
        select(focused)
        super.delete(activity)
    }

    override fun remove(activity: Activity) {
        select(focused)
        super.remove(activity)
    }

    override fun share(activity: Activity) {
        select(focused)
        super.share(activity)
    }

    override fun toggleLike() {
        select(focused)
        super.toggleLike()
    }

    @SuppressLint("NewApi")
    override suspend fun refresh() {
        delay(2)
        val order = MediaProvider.COLUMN_DATE_MODIFIED
        val ascending = false
        values = when (args) {
            is ViewerArgs.Folder -> provider.fetchFilesFromDirectory(
                path = args.path,
                order = order,
                ascending = ascending
            )

            is ViewerArgs.Timeline -> provider.fetchFiles(
                order = order,
                ascending = ascending
            )

            is ViewerArgs.Album -> {
                // currently the album is always favorite
                provider.fetchFiles(
                    *favorites.toLongArray(),
                    order = order,
                    ascending = ascending
                )
            }

            else -> throw UnsupportedOperationException("The Operation is not supported! $args")
        }
    }

    init {
        if (args is ViewerArgs.Album)
            preferences[Settings.KEY_FAVOURITE_FILES].onEach { refresh() }.launchIn(viewModelScope)
    }
}