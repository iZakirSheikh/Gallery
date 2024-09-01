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

package com.zs.gallery.viewer

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.zs.domain.store.MediaFile
import com.zs.foundation.menu.MenuItem
import com.zs.gallery.common.NULL_STRING
import com.zs.gallery.common.SafeArgs

/**
 * Represents the arguments for navigating to a viewer screen.
 * @property focused  The current focused item in the viewer.
 */
sealed interface Kind {
    val focused: Long

    /**
     * Arguments for the timeline viewer.
     */
    @JvmInline
    value class Timeline(override val focused: Long) : Kind

    /**
     * Arguments for the folder viewer.
     *
     * @property path The path of the folder to display.
     */
    data class Folder(val path: String, override val focused: Long) : Kind

    /**
     * Arguments for the album viewer.
     *
     * @property name The name of the album to display.
     */
    data class Album(val name: String, override val focused: Long) : Kind

    /**
     * Arguments for the trash viewer.
     */
    @JvmInline
    value class Trash(override val focused: Long) : Kind
}

private const val PARAM_FOCUSED = "param_focused"
private const val PARAM_TARGET = "param_target"
private const val PARAM_KEY = "param_key"

object RouteViewer : SafeArgs<Kind> {

    override val route = "$domain/{$PARAM_TARGET}/{$PARAM_KEY}/{$PARAM_FOCUSED}"

    /**
     * Generates a unique key for shared element transitions based on the given ID.
     * @param id ID to generate the key from.
     * @return The generated shared frame key.
     */
    fun buildSharedFrameKey(id: Long) = "shared_frame_$id"

    override fun build(handle: SavedStateHandle): Kind {
        val target = handle.get<String>(PARAM_TARGET)!!
        val key = handle.get<String>(PARAM_KEY)!!
        val focused = handle.get<String>(PARAM_FOCUSED)!!.toLong()
        return when (target) {
            "album" -> Kind.Album(key, focused)
            "folder" -> Kind.Folder(key, focused)
            "trash" -> Kind.Trash(focused)
            else -> Kind.Timeline(focused)
        }
    }

    override fun invoke(arg: Kind): String {
        val serialized = when (arg) {
            // The URL format is: [domain]/[target]/[key]/[focused]
            is Kind.Album -> "album/${arg.name}/${arg.focused}"
            is Kind.Folder -> "folder/${Uri.encode(arg.path)}/${arg.focused}"
            is Kind.Timeline -> "timeline/${SavedStateHandle.NULL_STRING}/${arg.focused}"
            is Kind.Trash -> "trash/${SavedStateHandle.NULL_STRING}/${arg.focused}"
        }
        return "$domain/$serialized"
    }

    /**
     * Creates a RouteViewer based on the provided arguments.
     * Only one of 'album', 'folder', or 'trash' should be specified, or none at all.
     *
     * @param focused The focused item id.
     * @param album The album name (optional).
     * @param folder The folder path (optional).
     * @param trash Whether to show the trash view (optional, defaults to false).
     */
    operator fun invoke(
        focused: Long,
        album: String? = null,
        folder: String? = null,
        trash: Boolean = false
    ): String {
        // Ensure at most one of album, folder, or trash is specified
        require(
            (album == null && folder == null && !trash)
                    || (album != null || folder != null || trash)
        )
        // Invoke the RouteViewer with the specified arguments
        return when {
            album != null -> invoke(Kind.Album(album, focused))
            folder != null -> invoke(Kind.Folder(folder, focused))
            trash -> invoke(Kind.Trash(focused))
            else -> invoke(Kind.Timeline(focused))
        }
    }
}

/**
 * Represents the view state for a viewer screen. This interface provides information about the
 * currently focused media item, user preferences, the displayed media data, and the type of viewer.
 *
 * @property focused The ID of the currently focused media item.
 * @property favourite Indicates whether the currently focused item is marked as a favorite by the user.
 * @property data A list of [MediaFile] objects representing the media files being displayed.
 * @property kind The source of files in the viewer, represented by the [Kind] enum.
 * @property details The details of the currently displayed file, as a [MediaFile] object.
 *     Future versions may provide more advanced details using [MediaMetadataRetriever] and [ExifInterface].
 * @property showDetails Controls the visibility of the details section. Set to `true` to display details,
 *     or `false` to hide them.
 */
interface ViewerViewState {
    var focused: Long
    val favourite: Boolean
    val data: List<MediaFile>
    val kind: Kind

    val details: MediaFile?
    var showDetails: Boolean

    /**
     * The list of actions supported by the currently displayed file, represented as [MenuItem] objects.
     */
    val actions: List<MenuItem>

    /**
     * Callback function to be invoked when an action is selected.
     *
     * @param item The [MenuItem] representing the selected action.
     * @param activity The [Activity] context in which the action is invoked.
     */
    fun onAction(item: MenuItem, activity: Activity)
}