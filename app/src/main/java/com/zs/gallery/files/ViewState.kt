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

package com.zs.gallery.files

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.zs.api.store.MediaFile
import com.zs.gallery.common.FileActions
import com.zs.gallery.common.Route
import com.zs.gallery.common.SafeArgs
import com.zs.gallery.common.SelectionTracker

object RouteTimeline : Route
object RouteAlbum : Route

private const val PARAM_PATH = "pram_path"

object RouteFolder : SafeArgs<String> {
    override val route: String = "$domain/{${PARAM_PATH}}"
    override fun invoke(arg: String): String = "$domain/${Uri.encode(arg)}"
    override fun build(handle: SavedStateHandle): String = handle[PARAM_PATH]!!
}

interface TimelineViewState : FileActions, SelectionTracker {

    val data: Map<String, List<MediaFile>>?

    /**
     * Observes the list of selected items and returns an integer indicating the favorite status of all items.
     *
     * @return the state of favourites in selection
     *  - `0` if none of the items are favorite.
     *  - `-1` if some of the items are favorite.
     *  - `1` if all of the items are favorite.
     */
    val allFavourite: Int

    /**
     * Toggles the favorite status of the selected items.
     *
     * This function checks the current favorite status of the selected items and performs the following actions:
     * - If `all` selected items are already favorite, it `removes` them from favorites.
     * - If `some` selected items are favorite, it `adds the remaining` unfavored items to favorites.
     * - If `none` of the selected items are favorite, it `adds all` of them favorites.
     */
    fun toggleLike()
}

interface FolderViewState : TimelineViewState {
    val title: CharSequence
}

interface AlbumViewState : SelectionTracker {
    val data: Map<String, List<MediaFile>>?
    val title: CharSequence

    /**
     * Removes the selected items from favourite list.
     */
    fun remove()
}
