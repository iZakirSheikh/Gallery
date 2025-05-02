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

package com.zs.gallery.folders

import com.zs.core.store.Folder
import com.zs.gallery.common.Action
import com.zs.gallery.common.Filter
import com.zs.gallery.common.Mapped
import com.zs.gallery.common.Route
import kotlinx.coroutines.flow.StateFlow

object RouteFolders: Route

interface FoldersViewState {
    /**
     * The list of supported orders.
     */
    val orders: List<Action>

    /**
     * The filter criteria for the list.
     */
    val filter: Filter

    /**
     * A state flow that emits the grid of folders in the app.
     * The list can be null, empty, or non-empty depending on the loading status and the availability of data.
     * A null value indicates that the folders are being loaded from the source and the UI should show a loading indicator.
     * An empty list indicates that there are no folders to display and the UI should show an empty state message.
     * A non-empty list indicates that the folders are successfully loaded and the UI should show them in a list view.
     * Any error that occurs during the loading process will be handled by a snackbar that shows the error message and a retry option.
     */
    val data: StateFlow<Mapped<Folder>?>

    /**
     * Updates the [filter] and triggers the update.
     */
    fun filter(ascending: Boolean = this.filter.first, order: Action = this.filter.second)
}