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
import com.zs.gallery.preview.RouteViewer

object RouteTimeline : Route

private const val PARAM_PATH = "pram_path"

object RouteFolder : SafeArgs<String> {
    override val route: String = "$domain/{${PARAM_PATH}}"
    override fun invoke(arg: String): String = "$domain/${Uri.encode(arg)}"
    override fun get(handle: SavedStateHandle): String = handle[PARAM_PATH]!!
}

/**
 * A simple interface that provides data for displaying in the list. if the data is null; it represents loading state.
 *  if it is empty; it represents empty state. else it represents the data.
 */
interface DataProvider {
    val data: Map<String, List<MediaFile>>?
}

/**
 * @return the route for the given id.
 */
fun DataProvider.buildViewerRoute(id: Long): String {
    val list = data?.values?.flatten()?.map { it.id } ?: emptyList()
    return RouteViewer(list.indexOf(id), list)
}

interface TimelineViewState : FileActions, SelectionTracker, DataProvider {
    /**
     * Adds selects items to favourite.
     */
    fun addToFavourite()
}

interface FolderViewState : TimelineViewState {
    val title: CharSequence
}