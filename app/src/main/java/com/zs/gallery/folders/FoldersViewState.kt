/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 20-07-2024.
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

import com.zs.api.store.Folder
import com.zs.gallery.common.Route
import kotlinx.coroutines.flow.StateFlow


object RouteFolders : Route

interface FoldersViewState {

    companion object {
        const val ORDER_BY_NAME = 0
        const val ORDER_BY_DATE_MODIFIED = 1
        const val ORDER_BY_SIZE = 2
    }

    /**
     * Gets or sets the grouping criterion of the list of folders.
     * A value of [ORDER_BY_NAME] means the list is grouped by the folder name, while a value of [ORDER_BY_DATE_MODIFIED] means the list is grouped by the folder date modified, and a value of [ORDER_BY_SIZE] means the list is grouped by the folder size.
     * The default value is [ORDER_BY_SIZE].
     */
    var ascending: Boolean

    /**
     * Gets or sets the grouping criterion of the list of folders.
     * A value of [ORDER_BY_NAME] means the list is grouped by the folder name, while a value of [ORDER_BY_DATE_MODIFIED] means the list is grouped by the folder date modified, and a value of [ORDER_BY_SIZE] means the list is grouped by the folder size.
     * The default value is [ORDER_BY_SIZE].
     */
    var order: Int

    /**
     * A state flow that emits the list of folders in the app.
     * The list can be null, empty, or non-empty depending on the loading status and the availability of data.
     * A null value indicates that the folders are being loaded from the source and the UI should show a loading indicator.
     * An empty list indicates that there are no folders to display and the UI should show an empty state message.
     * A non-empty list indicates that the folders are successfully loaded and the UI should show them in a list view.
     * Any error that occurs during the loading process will be handled by a snackbar that shows the error message and a retry option.
     */
    val data: StateFlow<List<Folder>?>
}