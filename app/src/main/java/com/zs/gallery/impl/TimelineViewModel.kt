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

package com.zs.gallery.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearbyError
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.primex.core.Rose
import com.primex.preferences.value
import com.zs.api.store.MediaFile
import com.zs.api.store.MediaProvider
import com.zs.compose_ktx.toast.Toast
import com.zs.gallery.R
import com.zs.gallery.common.GroupSelectionLevel
import com.zs.gallery.files.TimelineViewState
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "TimelineViewModel"

open class TimelineViewModel(
    val provider: MediaProvider
) : KoinViewModel(), TimelineViewState {
    // Trigger for refreshing the list
    private val _trigger = MutableStateFlow(false)
    val favourites =
        preferences[Settings.KEY_FAVOURITE_FILES]
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Forces the data store to refresh.
     */
    fun invalidate() {
        _trigger.value = (!_trigger.value)
    }

    override var data: Map<String, List<MediaFile>>? by mutableStateOf(null)
    final override val selected = mutableStateListOf<Long>()
    override val isInSelectionMode: Boolean by derivedStateOf(selected::isNotEmpty)
    override fun clear() = selected.clear()

    /**
     * Consumes the currently selected items and returns them as an array.
     *
     * This function creates a new array containing the selected items, clears the `selected` list, and returns the array.
     *
     * @return An array containing the previously selected items.
     */
    fun consume(): LongArray {
        // Efficiently convert the list to an array.
        val data = selected.toLongArray()
        // Clear the selected items list.
        selected.clear()
        return data
    }

    override val allSelected: Boolean by derivedStateOf {
        // FixMe: For now this seems reasonable.
        //      However take the example of the case when size is same but ids are different
        selected.size == data?.values?.flatten()?.size
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

    private fun evaluateGroupSelectionLevel(key: String): GroupSelectionLevel {
        val data = data ?: return GroupSelectionLevel.NONE //Return NONE if data is not available.

        val all = data[key]?.map { it.id } ?: emptyList() // Get IDs of all items in the group.
        val count =
            all.count { it in selected } // Count how many items from the group are currently selected.// Determine the selection level based on the count.
        return when (count) {
            all.size -> GroupSelectionLevel.FULL // All items in the group are selected.
            in 1..all.size -> GroupSelectionLevel.PARTIAL // Some items in the group are selected.
            else -> GroupSelectionLevel.NONE // No items in the group are selected.
        }
    }

    override fun isGroupSelected(key: String): State<GroupSelectionLevel> =
        derivedStateOf { evaluateGroupSelectionLevel(key) }

    override fun selectAll() {
        val data = data ?: return // Return if data is not available.

        // Iterate through all items in the data and select them if they are not already selected.
        data.values.flatten().forEach { item ->
            val id = item.id
            if (!selected.contains(id)) {
                selected.add(id)
            }
        }
    }

    override val allFavourite: Int by derivedStateOf {
        val list = selected // Get the list of selected items.
        val favourites = favourites.value // Get the list of favorite items.

        // Handle empty lists: If either list is empty, return 0 (none favorited).
        if (list.isEmpty() || favourites.isEmpty()) return@derivedStateOf 0

        // Check if all selected items are favorites.
        val all = list.all { favourites.contains(it) }

        // Return the appropriate state:
        // 1 if all selected items are favorites, -1 if some are, 0 otherwise.
        if (all) return@derivedStateOf 1 else -1
    }

    override fun move(dest: String): Unit = TODO("Not yet implemented")
    override fun copy(dest: String): Unit = TODO("Not yet implemented")
    override fun rename(name: String): Unit = TODO("Not yet implemented")
    override fun restore(activity: Activity) = TODO("Not yet implemented")

    @SuppressLint("NewApi")
    suspend fun trash(ids: LongArray, activity: Activity) {
        // Ensure this is called on Android 10 or higher (API level 29).
        val result = com.primex.core.runCatching(TAG) {
            provider.trash(activity, *ids)
        }
        val msg = when (result) {
            null, 0, -1 -> getText(R.string.msg_files_trash_unknown_error) // General error
            -2 -> getText(R.string.msg_confirm_trashing) // Pending user confirmation (likely for trashing)
            -3 -> getText(R.string.msg_files_trashing_cancelled) // User canceled the operation
            else -> getText(
                R.string.msg_files_trashing_success_out_total,
                result,
                ids.size
            ) // Success with count
        }
        showToast(msg)
    }

    override fun delete(activity: Activity) {
        viewModelScope.launch {
            val selected = consume()
            val trash = preferences.value(Settings.KEY_TRASH_CAN_ENABLED)
            val result = com.primex.core.runCatching(TAG) {
                // Less than R trash not matters.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                    // TODO - Test this version of delete on android version < 11
                    //        Add Confirmation snack for this.
                    return@runCatching provider.delete(*selected)
                // Handle deletion based on Android version and trash setting.
                if (trash)
                    return@launch trash(selected, activity)
                // Delete directly if trash is disabled.
                provider.delete(activity, *selected)
            }
            // Display a message based on the result of the deletion operation.
            val msg = when (result) {
                null, 0, -1 -> getText(R.string.msg_files_delete_unknown_error) // General error
                -2 -> getText(R.string.msg_confirm_deletion) // Pending user confirmation (likely for trashing)
                -3 -> getText(R.string.msg_files_deletion_cancelled) // User canceled the operation
                else -> getText(
                    R.string.msg_files_deletion_success_out_total,
                    result,
                    selected.size
                ) // Success with count
            }
            showToast(msg)
        }
    }

    override fun share(activity: Activity) {
        viewModelScope.launch {
            // Get the list of selected items to share.
            val selected = consume()
            // Create an intent to share the selected items
            val intent = Intent().apply {
                // Map selected IDs to content URIs.
                val uri = selected.map {
                    ContentUris.withAppendedId(
                        MediaProvider.EXTERNAL_CONTENT_URI,
                        it
                    )
                }
                // Set the action to send multiple items.
                action = Intent.ACTION_SEND_MULTIPLE
                // Add the URIs as extras.
                putParcelableArrayListExtra(
                    Intent.EXTRA_STREAM,
                    uri.toMutableList() as ArrayList<Uri>
                )
                // Grant read permission to the receiving app.
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Set the MIME type to allow sharing of various file types.
                type = "*/*"
                // Specify supported MIME types.
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            }
            // Start the sharing activity with a chooser.
            try {
                activity.startActivity(Intent.createChooser(intent, "Share Photos & Video"))
            } catch (e: Exception) {
                // Handle exceptions and display an error message.
                showToast(R.string.msg_error_sharing_files)
                Log.d(TAG, "share: ${e.message}")
            }
        }
    }

    override fun select(id: Long) {
        val contains = selected.contains(id)
        if (contains) selected.remove(id) else selected.add(id)
    }

    override fun toggleLike() {
        viewModelScope.launch {
            // Get the selected items and clear the selection.
            val selected = consume().toList()
            // Get a mutable list of favorite items.
            val favourites = favourites.value.toMutableList()

            // Determine the action and message based on whether all selected items are already favorites.
            val result = when {
                favourites.containsAll(selected) -> {
                    favourites.removeAll(selected) // Remove all selected items from favorites.
                    "Removed ${selected.size} files from favorites"
                }

                selected.any { it in favourites } -> {
                    // Add the un-favorite selected items to favorites.
                    val filtered = selected.filterNot { it in favourites }
                    favourites.addAll(filtered)
                    "Added ${filtered.size} out of ${selected.size} files to favorites"
                }

                else -> {
                    // Add the un-favorite selected items to favorites.
                    favourites.addAll(selected)
                    "Added ${selected.size} files to favorites"
                }
            }

            // Update the favorite items in preferences.
            preferences[Settings.KEY_FAVOURITE_FILES] = favourites
            // Display a message to the user.
            showToast(result)
        }
    }

    /**
     * Updates the state of the ViewModel by fetching files from the provider, grouping them by date,
     * and updating the 'data' property with the result.
     */
    open suspend fun update() {
        // Fetch files from the provider, ordered by modification date in descending order.
        val value =
            provider.fetchFiles(order = MediaProvider.COLUMN_DATE_MODIFIED, ascending = false)
                // Group the files by their relative time span (e.g., "Today", "Yesterday").
                .groupBy {
                    DateUtils.getRelativeTimeSpanString(
                        it.dateModified,
                        System.currentTimeMillis(),
                        DateUtils.DAY_IN_MILLIS
                    ).toString()
                }
        // Update the 'data' property with the grouped files.
        data = value
    }

    init {
        Log.d(TAG, "${this::class.simpleName}: created.")
        provider.observer(MediaProvider.EXTERNAL_CONTENT_URI)
            .combine(_trigger) { _, _ -> update() }
            .catch { exception ->
                Log.e(TAG, "provider: ${exception.message}")
                // Handle any exceptions that occur during the flow.
                // This might involve logging the exception using Firebase Crashlytics.
                // Display a toast message to the user, indicating something went wrong and suggesting they report the issue.
                val action = showToast(
                    exception.message ?: "",
                    getText(R.string.report),
                    Icons.Outlined.NearbyError,
                    Color.Rose,
                    Toast.DURATION_INDEFINITE
                )
            }
            .launchIn(viewModelScope)
    }
}