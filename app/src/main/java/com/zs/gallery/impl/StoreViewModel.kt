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

package com.zs.gallery.impl

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NearbyError
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.zs.compose.foundation.Rose
import com.zs.compose.foundation.runCatching
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarResult
import com.zs.core.store.MediaProvider
import com.zs.gallery.R
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.launch

abstract class StoreViewModel(val provider: MediaProvider): KoinViewModel(){

    private val TAG = "StoreViewModel"

    /**
     * Toggles the favorite status of the selected items.
     *
     * This function checks the current favorite status of the selected items and performs the following actions:
     * - If `all` selected items are already favorite, it `removes` them from favorites.
     * - If `some` selected items are favorite, it `adds the remaining` unfavored items to favorites.
     * - If `none` of the selected items are favorite, it `adds all` of them favorites.
     */
    fun toggleLike(vararg id: Long) {
        viewModelScope.launch {
            // Get the selected items and clear the selection.
            val selected = id.toList()
            // show rationale if items are in bulk
            if (selected.size > 1) {
                val res = showSnackbar(
                    "Modify ${selected.size} items in Favorites?",
                    "Confirm",
                    Icons.Outlined.FavoriteBorder,
                    duration = SnackbarDuration.Long
                )
                if (res != SnackbarResult.ActionPerformed)
                    return@launch
            }
            // Get a mutable list of favorite items.
            val favourites = preferences[Settings.KEY_FAVOURITE_FILES].toMutableList()

            // Determine the action and message based on whether all selected items are already favorites.
            when {
                // Remove all selected items from favorites.
                favourites.containsAll(selected) -> favourites.removeAll(selected)
                // Add the un-favorite selected items to favorites.
                selected.any { it in favourites } -> {
                    val filtered = selected.filterNot { it in favourites }
                    favourites.addAll(filtered)
                }
                // Add the un-favorite selected items to favorites.
                else -> favourites.addAll(selected)
            }

            // Update the favorite items in preferences.
            preferences[Settings.KEY_FAVOURITE_FILES] = favourites
            // Display a message to the user.
            showPlatformToast(R.string.msg_favourites_updated)
        }
    }

    /** Deletes file[s] represented by id[s]. */
    fun delete(resolver: Activity, vararg id: Long) {
        viewModelScope.launch {
            val result = runCatching (TAG) {
                // Get the selected items for deletion
                val consumed = id
                // For Android R and above, use the provider's delete function directly
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    return@runCatching provider.delete(resolver, *consumed)
                // For versions below Android R, show a confirmation toast
                // If the user performs the action, proceed with deletion
                // Otherwise, return -3 to indicate user cancellation
                val action = showSnackbar(
                    message = R.string.msg_files_confirm_deletion,
                    action = R.string.delete,
                    icon = Icons.Outlined.NearbyError,
                    accent = Color.Rose,
                    duration = SnackbarDuration.Indefinite
                )
                // Delete the selected items
                // else return -3 to indicate user cancellation
                if (action == SnackbarResult.ActionPerformed)
                    return@runCatching provider.delete(*consumed)
                // else return user cancelled
                -3
            }
            // Display a message based on the result of the deletion operation.
            if (result == null || result == 0 || result == -1)
                showPlatformToast(R.string.msg_files_delete_unknown_error)// General error
        }
    }

    @Suppress("NewApi")
    fun trash(resolver: Activity, vararg id: Long) {
        viewModelScope.launch {
            // Ensure this is called on Android 10 or higher (API level 29).
            val result = runCatching(TAG) {
                // consume selected
                val selected = id
                provider.trash(resolver, *selected)
            }
            // General error
            if (result == null || result == 0 || result == -1)
                showPlatformToast(R.string.msg_files_trash_unknown_error)
        }
    }

    /** Deletes or Trashes file(s) represented by id(s).*/
    fun remove(resolver: Activity, vararg id: Long) {
        val isTrashEnabled = preferences[Settings.KEY_TRASH_CAN_ENABLED]
        if (isTrashEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            trash(resolver = resolver, *id)
        else {
            Log.d(TAG, "remove: ${id.size}")
            delete(resolver = resolver, *id)
        }
    }

    @Suppress("NewApi")
    fun restore(resolver: Activity, vararg id: Long) {
        viewModelScope.launch {
            val selected = id
            val result = runCatching(TAG) {
                provider.restore(resolver, *selected)
            }
            // Display a message based on the result of the deletion operation.
            // General error
            if (result == null || result == 0 || result == -1)
                showPlatformToast(R.string.msg_files_restore_unknown_error)
        }
    }
}