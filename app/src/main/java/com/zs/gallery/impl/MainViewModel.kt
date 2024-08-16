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
@file:SuppressLint("NewApi")

package com.zs.gallery.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearbyError
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.primex.core.Rose
import com.primex.preferences.value
import com.zs.domain.store.MediaProvider
import com.zs.foundation.toast.Toast
import com.zs.gallery.R
import com.zs.gallery.common.FileActions
import com.zs.gallery.common.GroupSelectionLevel
import com.zs.gallery.common.SelectionTracker
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.also as then

private const val TAG = "MainViewModel"

/**
 * A top-level ViewModel that provides common functionality across different ViewModels,
 * such as file operations and selection tracking.
 ** This ViewModel continuously observes the [MediaProvider.EXTERNAL_CONTENT_URI] and triggers
 * a [refresh] whenever changes occur. Child ViewModels should update their state, including
 * any relevant values, during this refresh cycle.
 *
 * You can manually trigger a data invalidation using the [invalidate] method. To retrieve
 * the currently selected items, call the [consume] method, which returns an array of
 * selected IDs.
 *
 * This ViewModel also observes the [favorites] list and updates it based on changes to the
 * [Settings.KEY_FAVOURITE_FILES] preference flow.
 *
 * @param provider The MediaProvider to interact with.
 * @property values The list of values managed by this ViewModel.
 * @property id The unique ID associated with the values.
 * @property favorites An observable list of favorite items, updated from the [Settings.KEY_FAVOURITE_FILES] preference flow.
 */
abstract class MainViewModel<T>(
    val provider: MediaProvider
) : KoinViewModel(), FileActions, SelectionTracker {

    // Data related to the ViewModel
    abstract val values: List<T>
    abstract val T.id: Long


    /**
     * Triggers a refresh of the data store, causing it to re-emit its current value.
     */
    fun invalidate() {
        viewModelScope.launch { refresh() }
    }


    // Favourite Tracker.
    val favorites =
        mutableStateListOf<Long>().then { favourites ->
            // Observe changes to the favorite files preference
            preferences[Settings.KEY_FAVOURITE_FILES]
                .onEach { value ->
                    // Calculate the items to add and remove to efficiently update the favorites list
                    val toAdd = value.filterNot { it in favourites }
                    val toRemove = favourites.filterNot { it in value }

                    // Update the favorites list with the calculated changes
                    favourites.addAll(toAdd)
                    favourites.removeAll(toRemove)
                }
                .launchIn(viewModelScope)
        }

    /**
     * Observes the list of selected items and returns an integer indicating the favorite status of all items.
     *
     * @return the state of favourites in selection
     *  - `0` if none of the items are favorite.
     *  - `-1` if some of the items are favorite.
     *  - `1` if all of the items are favorite.
     */
    val allFavourite: Int by derivedStateOf {
        val list = selected // Get the list of selected items.
        val favourites = favorites // Get the list of favorite items.

        // Handle empty lists: If either list is empty, return 0 (none favorite).
        if (list.isEmpty() || favourites.isEmpty()) return@derivedStateOf 0

        // Check if all selected items are favorites.
        val all = list.all { favourites.contains(it) }

        // Return the appropriate state:
        // 1 if all selected items are favorites, -1 if some are, 0 otherwise.
        if (all) return@derivedStateOf 1 else -1
    }

    // SelectionTracker Properties
    final override val selected = mutableStateListOf<Long>()
    override val isInSelectionMode: Boolean by derivedStateOf(selected::isNotEmpty)
    override fun clear() = selected.clear()


    override val allSelected: Boolean by derivedStateOf {
        // FixMe: For now this seems reasonable.
        //      However take the example of the case when size is same but ids are different
        selected.size == values.size
    }

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

    override fun selectAll() {
        val data = values
        // Iterate through all items in the data and select them if they are not already selected.
        values.forEach { item ->
            val id = item.id
            if (!selected.contains(id)) {
                selected.add(id)
            }
        }
    }

    override fun select(id: Long) {
        val contains = selected.contains(id)
        if (contains) selected.remove(id) else selected.add(id)
    }

    /**
     * Toggles the favorite status of the selected items.
     *
     * This function checks the current favorite status of the selected items and performs the following actions:
     * - If `all` selected items are already favorite, it `removes` them from favorites.
     * - If `some` selected items are favorite, it `adds the remaining` unfavored items to favorites.
     * - If `none` of the selected items are favorite, it `adds all` of them favorites.
     */
    open fun toggleLike() {
        viewModelScope.launch {
            // Get the selected items and clear the selection.
            val selected = consume().toList()
            // Get a mutable list of favorite items.
            val favourites = favorites.toMutableList()

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

    open fun evaluateGroupSelectionLevel(key: String): GroupSelectionLevel {
        return GroupSelectionLevel.NONE
    }

    override fun isGroupSelected(key: String): State<GroupSelectionLevel> =
        derivedStateOf { evaluateGroupSelectionLevel(key) }

    @SuppressLint("NewApi")
    override fun trash(activity: Activity) {
        viewModelScope.launch {
            val selected = consume()
            // Ensure this is called on Android 10 or higher (API level 29).
            val result = com.primex.core.runCatching(TAG) {
                provider.trash(activity, *selected)
            }
            val msg = when (result) {
                null, 0, -1 -> getText(R.string.msg_files_trash_unknown_error) // General error
                -2 -> getText(R.string.msg_confirm_trashing) // Pending user confirmation (likely for trashing)
                -3 -> getText(R.string.msg_files_trashing_cancelled) // User canceled the operation
                else -> getText(
                    R.string.msg_files_trashing_success_out_total,
                    result,
                    selected.size
                ) // Success with count
            }
            showToast(msg)
        }
    }

    override fun remove(activity: Activity) {
        val isTrashEnabled = preferences.value(Settings.KEY_TRASH_CAN_ENABLED)
        if (isTrashEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            trash(activity)
        else
            delete(activity)
    }

    override fun delete(activity: Activity) {
        viewModelScope.launch {
            val selected = consume()
            val result = com.primex.core.runCatching(TAG) {
                // Less than R trash not matters.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    provider.delete(activity, *selected)
                else
                // Delete directly if trash is disabled.
                // TODO - Test this version of delete on android version < 11
                //        Add Confirmation snack for this.
                    provider.delete(*selected)
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
                // TODO - Construct custom content uri.
                val uri = selected.map {
                    ContentUris.withAppendedId(MediaProvider.EXTERNAL_CONTENT_URI, it)
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

    abstract suspend fun refresh()
    override fun move(dest: String): Unit = TODO("Not yet implemented")
    override fun copy(dest: String): Unit = TODO("Not yet implemented")
    override fun rename(name: String): Unit = TODO("Not yet implemented")

    override fun restore(activity: Activity) {
        viewModelScope.launch {
            val selected = consume()
            val result = com.primex.core.runCatching(TAG) {
                provider.restore(activity, *selected)
            }
            // Display a message based on the result of the deletion operation.
            val msg = when (result) {
                null, 0, -1 -> getText(R.string.msg_files_restore_unknown_error) // General error
                -2 -> getText(R.string.msg_confirm_restoring) // Pending user confirmation (likely for trashing)
                -3 -> getText(R.string.msg_files_restoring_cancelled) // User canceled the operation
                else -> getText(
                    R.string.msg_files_restoring_success_out_total,
                    result,
                    selected.size
                ) // Success with count
            }
            Log.d(TAG, "restore: $result")
            showToast(msg)
        }
    }

    init {
        Log.d(TAG, "${this::class.simpleName}: created.")
        provider.observer(MediaProvider.EXTERNAL_CONTENT_URI)
            .onEach { refresh() }
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