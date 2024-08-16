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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zs.domain.store.MediaFile
import com.zs.domain.store.MediaProvider
import com.zs.domain.store.isImage
import com.zs.domain.store.mediaUri
import com.zs.foundation.menu.MenuItem
import com.zs.gallery.R
import com.zs.gallery.common.get
import com.zs.gallery.viewer.Kind
import com.zs.gallery.viewer.RouteViewer
import com.zs.gallery.viewer.ViewerViewState
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "ViewerViewModel"

private val DELETE = MenuItem("action_delete", R.string.delete, Icons.Outlined.Delete)
private val SHARE = MenuItem("action_share", R.string.share, Icons.Outlined.Share)
private val USE_AS = MenuItem("action_use_as", R.string.use_as, Icons.Outlined.Image)
private val EDIT_IN = MenuItem("action_edit_in", R.string.edit_in, Icons.Outlined.Edit)
private val STAR = MenuItem("action_like", R.string.like, Icons.Outlined.StarOutline)
private val UN_STAR = MenuItem("action_unlike", R.string.unlike, Icons.Outlined.Star)

/**
 * Creates an Intent to edit an image at the given URI.
 *
 * @param uri The URI of the image to edit.
 * @return An Intent configured for image editing.
 */
private fun EditIn(uri: Uri) =
    Intent(Intent.ACTION_EDIT).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grants temporary read permission to the editing app
    }

/**
 * Creates an Intent to use an image as wallpaper.
 *
 * @param uri The URI of the image to set as wallpaper.
 * @return An Intent configured for setting the wallpaper.
 */
private fun Wallpaper(uri: Uri) =
    Intent(Intent.ACTION_ATTACH_DATA).apply {
        setDataAndType(uri, "image/*")
        putExtra("mimeType", "image/*") // Specifies the MIME type of the image
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grants temporary read permission to the wallpaper app
    }


class ViewerViewModel(
    handle: SavedStateHandle,
    provider: MediaProvider
) : MainViewModel<MediaFile>(provider), ViewerViewState {
    override val kind = handle[RouteViewer]

    override var values: List<MediaFile> by mutableStateOf(emptyList())
    override val data: List<MediaFile> get() = values

    override var focused by mutableLongStateOf(kind.focused)
    override val favourite: Boolean by derivedStateOf { favorites.contains(focused) }
    override val MediaFile.id: Long get() = id

    val current inline get() = values.find { it.id == focused }

    override val actions: List<MenuItem> by derivedStateOf {
        buildList {
            Log.d(TAG, "actions: changed ")
            this += if (favourite) UN_STAR else STAR
            this += SHARE
            this += DELETE
            // if this is video currently return otherwise editing can be called for video also
            if (current?.isImage == false) return@buildList
            this += USE_AS
            this += EDIT_IN
        }
    }


    override fun onAction(item: MenuItem, activity: Activity) {
        when (item) {
            STAR, UN_STAR -> toggleLike()
            DELETE -> delete(activity)
            SHARE -> share(activity)
            USE_AS -> {
                val current = current ?: return
                activity.startActivity(Wallpaper(current.mediaUri))
            }

            EDIT_IN -> {
                val current = current ?: return
                activity.startActivity(EditIn(current.mediaUri))
            }
        }
    }

    init {
        if (kind is Kind.Album)
            preferences[Settings.KEY_FAVOURITE_FILES]
                .onEach { refresh() }
                .launchIn(viewModelScope)
    }

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

    override suspend fun refresh() {
        delay(2)
        val order = MediaProvider.COLUMN_DATE_MODIFIED
        val ascending = false

        // TODO - Add a method in  MediaProvider that directly returns SimpleFile
        values = when (kind) {
            is Kind.Album -> provider.fetchFiles(
                *favorites.toLongArray(),
                order = order,
                ascending = ascending
            )

            is Kind.Folder -> provider.fetchFilesFromDirectory(
                path = kind.path,
                order = order,
                ascending = ascending
            )

            is Kind.Timeline -> provider.fetchFiles(order = order, ascending = ascending)
            is Kind.Trash -> TODO("Not Implemented yet!")
        }
    }
}