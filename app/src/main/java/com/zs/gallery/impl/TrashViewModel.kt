/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 27-07-2024.
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

package com.zs.gallery.impl

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zs.domain.store.MediaProvider
import com.zs.domain.store.Trashed
import com.zs.gallery.bin.TrashViewState
import com.zs.gallery.common.GroupSelectionLevel
import kotlin.time.Duration.Companion.milliseconds


class TrashViewModel(
    provider: MediaProvider
) : MainViewModel<Trashed>(provider), TrashViewState {
    override var values: List<Trashed> = emptyList()

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    override val Trashed.id: Long get() = id
    override var data: Map<String, List<Trashed>>? by mutableStateOf(null)

    override fun evaluateGroupSelectionLevel(key: String): GroupSelectionLevel {
        // Return NONE if data is not available.
        val data = data?.get(key) ?: return GroupSelectionLevel.NONE
        // Count selected
        val count = data.count { it.id in selected }
        return when (count) {
            data.size -> GroupSelectionLevel.FULL // All items in the group are selected.
            in 1..data.size -> GroupSelectionLevel.PARTIAL // Some items in the group are selected.
            else -> GroupSelectionLevel.NONE // No items in the group are selected.
        }
    }

    override fun select(key: String) {
        // Return if data is not available.
        val data = data ?: return
        // Get the current selection level of the group.
        val level = evaluateGroupSelectionLevel(key)
        // Get the IDs of all items in the group.
        val all = data[key]?.map { it.id } ?: emptyList()
        // Update the selected items based on the group selection level.
        when (level) {
            GroupSelectionLevel.NONE -> selected.addAll(all) // Select all items in the group.
            GroupSelectionLevel.PARTIAL -> selected.addAll(all.filterNot { it in selected }) // Select only unselected items.
            GroupSelectionLevel.FULL -> selected.removeAll(all.filter { it in selected }) // Deselect all selected items.
        }
    }

    @SuppressLint("NewApi")
    override suspend fun refresh() {
        // Fetch the list of trashed files from the provider
        values = provider.fetchTrashedFiles()

        // Group the trashed files by their remaining days until expiration
        data = values
            // Calculate days left for each file
            .groupBy() {
                (it.expires - System.currentTimeMillis()).milliseconds.inWholeDays
            }
            // Transform the keys to user-friendly labels
            .mapKeys { (daysLeft, _) ->
                when {
                    daysLeft <= 0 -> "Today" // Handle expired items
                    daysLeft == 1L -> "1 day left"
                    else -> "$daysLeft days left" // Format remaining days
                }
            }
    }


    override fun empty(activity: Activity) {
        selectAll()
        delete(activity)
    }
}