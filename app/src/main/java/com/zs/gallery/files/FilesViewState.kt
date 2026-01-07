/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
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
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

package com.zs.gallery.files

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.paging.PagingData
import com.zs.common.db.media.Directory
import com.zs.common.db.media.Snapshot
import com.zs.gallery.common.NavKey
import com.zs.gallery.common.Res.action
import kotlinx.coroutines.flow.Flow

/**
 * Represents the state of a file view within a [Directory].
 *
 * This interface models:
 * - The directory being browsed (timeline, album, folder, docs, liked, private, recycled, archived, etc.)
 * - The stream of files available in that directory
 * - The actions supported by the directory
 * - The current selection state and operations to manage it
 *
 * @property directory The directory being viewed.
 *                     Null if timeline; otherwise points to the actual directory
 *                     (album, folder, docs, liked, private, recycled, archived, etc.).
 * @property files A reactive stream of paged [Snapshot] items representing the files in this directory.
 * @property actions The list of supported actions for this directory, exposed as a [SnapshotStateList].
 * @property selected The list of currently selected file IDs.
 * @property isInSelectionMode True if there is an active selection, false otherwise.
 */
interface FilesViewState {

    // ───────────── DIRECTORY  ─────────────
    val key: NavKey
    val files: Flow<PagingData<Snapshot>>
    val actions: List<action>

    // ───────────── SELECTION ─────────────

    val selected: List<Long>
    val isInSelectionMode: Boolean

    // ───────────── Methods ─────────────

    /**
     * Selects or unselects a single item by its ID.
     *
     * @param id The unique identifier of the item to toggle selection for.
     */
    fun select(id: Long)

    /**
     * Clears the current selection.
     */
    fun clear()

    /**
     * Selects all items in the current directory.
     */
    fun selectAll()
}