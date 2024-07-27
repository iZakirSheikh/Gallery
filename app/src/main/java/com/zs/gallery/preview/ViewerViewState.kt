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

package com.zs.gallery.preview

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.zs.api.store.MediaFile
import com.zs.gallery.common.FileActions
import com.zs.gallery.common.NULL_STRING
import com.zs.gallery.common.SafeArgs

/**
 * Represents the arguments for navigating to a viewer screen.
 * @property focused - The current focused item in the viewer.
 */
sealed interface ViewerArgs {

    val focused: Long

    /**
     * Arguments for the timeline viewer.
     */
    data class Timeline(override val focused: Long) : ViewerArgs

    /**
     * Arguments for the folder viewer.
     *
     * @property path The path of the folder to display.
     */
    data class Folder(val path: String, override val focused: Long) : ViewerArgs

    /**
     * Arguments for the album viewer.
     *
     * @property name The name of the album to display.
     */
    data class Album(val name: String, override val focused: Long) : ViewerArgs

    /**
     * Arguments for the trash viewer.
     */
    data class Trash(override val focused: Long) : ViewerArgs
}

private const val PARAM_FOCUSED = "param_focused"
private const val PARAM_TARGET = "param_target"
private const val PARAM_KEY = "param_key"

object RouteViewer : SafeArgs<ViewerArgs> {
    /**
     * Generates a unique key for shared element transitions based on the given ID.
     * @param id ID to generate the key from.
     * @return The generated shared frame key.
     */
    fun buildSharedFrameKey(id: Long) = "shared_frame_$id"

    override fun build(handle: SavedStateHandle): ViewerArgs {
        val target = handle.get<String>(PARAM_TARGET)!!
        val key = handle.get<String>(PARAM_KEY)!!
        val focused = handle.get<String>(PARAM_FOCUSED)!!.toLong()
        return when (target) {
            "album" -> ViewerArgs.Album(key, focused)
            "folder" -> ViewerArgs.Folder(key, focused)
            "trash" -> ViewerArgs.Trash(focused)
            else -> ViewerArgs.Timeline(focused)
        }
    }

    override val route = "$domain/{$PARAM_TARGET}/{$PARAM_KEY}/{$PARAM_FOCUSED}"

    override fun invoke(arg: ViewerArgs): String {
        val serialized = when (arg) {
            // The URL format is: [domain]/[target]/[key]/[focused]
            is ViewerArgs.Album -> "album/${arg.name}/${arg.focused}"
            is ViewerArgs.Folder -> "folder/${Uri.encode(arg.path)}/${arg.focused}"
            is ViewerArgs.Timeline -> "timeline/${SavedStateHandle.NULL_STRING}/${arg.focused}"
            is ViewerArgs.Trash -> "trash/${SavedStateHandle.NULL_STRING}/${arg.focused}"
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
            album != null -> invoke(ViewerArgs.Album(album, focused))
            folder != null -> invoke(ViewerArgs.Folder(folder, focused))
            trash -> invoke(ViewerArgs.Trash(focused))
            else -> invoke(ViewerArgs.Timeline(focused))
        }
    }
}

/**
 * Represents the view state for a viewer screen.
 * @property focused  The current focused item.
 * @property index  The index corresponding to [focused] id. default 0
 * @property data  The list of media files.
 */
interface ViewerViewState : FileActions {
    var focused: Long
    val data: List<MediaFile>
    val index get() = if (data.isEmpty()) 0 else data.indexOfFirst { it.id == focused }


    val favourite: Boolean

    fun toggleLike()
}