/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 04-04-2025.
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

package com.zs.gallery.files

import android.app.Activity
import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import com.zs.core.store.MediaFile
import com.zs.gallery.common.Action
import com.zs.gallery.common.Mapped
import com.zs.gallery.common.Route
import com.zs.gallery.common.SelectionTracker


object RouteFiles : Route {

    private const val PARAM_KEY = "param_key"

    const val ALBUM_BIN = "album_bin"
    const val ALBUM_FAV = "album_fav"

    override val route: String =
        "$domain/{$PARAM_KEY}"

    val SavedStateHandle.key
        get() =
            get<String>(PARAM_KEY).takeIf { !it.isNullOrEmpty() }

    override fun invoke() = error("use RouteTimeline instead!")

    /** Navigates to files of folder identified by [source] providing args*/
    operator fun invoke(key: String): String = "$domain/${Uri.encode(key)}"

    /**
     * Generates a unique key for shared element transitions based on the given ID.
     * @param id ID to generate the key from.
     * @return The generated shared frame key.
     */
    fun buildSharedFrameKey(id: Long) = "shared_frame_$id"
}

// Some special cases
object RouteTimeline : Route
fun RouteLiked() = RouteFiles(RouteFiles.ALBUM_FAV)
fun RouteBin() = RouteFiles(RouteFiles.ALBUM_BIN)

/**
 * Represents the state of the files screen.
 * @property meta title and icon of the screen.
 *
 */
interface FilesViewState : SelectionTracker {

    /** Represents the */
    val meta: Pair<ImageVector, CharSequence>

    /**
     * Represents the data associated with this screen.
     */
    val data: Mapped<MediaFile>?

    /** The list of actions supported by screen.*/
    val actions: List<Action>

    /** Executes the [value] using the [activity]*/
    fun onAction(value: Action, activity: Activity)

    /** @return the navigation route for the [id]*/
    fun buildRouteViewer(id: Long): String
}