/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 16-05-2025.
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
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import com.zs.compose.theme.sharedBounds
import com.zs.compose.theme.sharedElement
import com.zs.core.store.MediaFile
import com.zs.gallery.common.Action
import com.zs.gallery.common.Mapped
import com.zs.gallery.common.Route
import com.zs.gallery.common.SelectionTracker
import com.zs.gallery.files.RouteFiles.SOURCE_TIMELINE

fun RouteFolder(path: String) = RouteFiles(RouteFiles.SOURCE_FOLDER, path)
fun RouteLiked() = RouteFiles(RouteFiles.SOURCE_FAV)
fun RouteBin() = RouteFiles(RouteFiles.SOURCE_BIN)

private const val PARAM_ARG = "param_path"
private const val PARAM_SOURCE = "param_source"

object RouteFiles : Route {

    const val SOURCE_BIN = "source_bin"
    const val SOURCE_FAV = "source_fav"
    const val SOURCE_TIMELINE = "source_timeline"
    const val SOURCE_FOLDER = "source_folder"

    override val route: String = "$domain/{$PARAM_SOURCE}/{$PARAM_ARG}"

    /** Navigates to files of folder identified by [source] providing args*/
    operator fun invoke(source: String, key: String = ""): String =
        "$domain/${source}/${Uri.encode(key)}"

    @OptIn(ExperimentalSharedTransitionApi::class)
    private val DefaultBoundsTransform = BoundsTransform { _, _ -> tween(200) }

    /**
     * Generates a unique key for shared element transitions based on the given ID.
     * @param id ID to generate the key from.
     * @return The generated shared frame key.
     */
    @ExperimentalSharedTransitionApi
    fun sharedElement(id: Long) =
        Modifier.sharedBounds("shared_frame_$id", boundsTransform = DefaultBoundsTransform, zIndexInOverlay = 0.1f)
}

operator fun SavedStateHandle.get(route: RouteFiles) =
    (get<String>(PARAM_SOURCE)
        ?: SOURCE_TIMELINE) to get<String>(PARAM_ARG).takeIf { !it.isNullOrEmpty() }

/**
 * Represents the state of the files screen.
 * @property meta title and icon of the screen.
 *
 */
interface FilesViewState : SelectionTracker {

    /** If icon is null use app icon. */
    val meta: Pair<ImageVector?, CharSequence>

    /**
     * Represents the data associated with this screen.
     */
    val data: Mapped<MediaFile>?

    /** The list of actions supported by screen.*/
    val actions: List<Action>

    /** Executes the [value] using the [resolver]*/
    fun onRequest(value: Action, resolver: Activity)

    /** @return the navigation route for the [id]*/
    fun direction(id: Long): String
}
