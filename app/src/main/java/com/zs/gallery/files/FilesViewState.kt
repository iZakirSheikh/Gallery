/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 15 of Jan 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 15 of Jan 2026
 */

package com.zs.gallery.files

import androidx.compose.runtime.IntState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.paging.PagingData
import com.zs.domain.db.media.Snapshot
import com.zs.gallery.common.Res.action
import kotlinx.coroutines.flow.Flow

/**
 * Interface representing the state for the files view.
 *
 * @property meta If icon is null use app icon.
 * @property data Represents the data associated with this screen.
 * @property actions The list of actions supported by screen.
 * @property selected Represents the selected items in this view.
 * @property isInSelectionMode Indicates whether there is an active selection.
 */
interface FilesViewState {
    val meta: Pair<ImageVector?, CharSequence>
    val data: Flow<PagingData<Snapshot>>
    val actions: List<action>
    // selection
    val selected: List<Long>
    val isInSelectionMode: Boolean

    /**
     * Toggles the selection of item having [id]
     */
    fun select(id: Long)

    /**
     * Clears the selection.
     */
    fun clear()

    /**
     * Selects all items.
     */
    fun selectAll()
}