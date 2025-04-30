/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 27-04-2025.
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
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.NearbyError
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.FolderCopy
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material.icons.twotone.WorkspacePremium
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zs.compose.foundation.Rose
import com.zs.compose.foundation.runCatching
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarResult
import com.zs.core.common.PathUtils
import com.zs.core.store.MediaFile
import com.zs.core.store.MediaProvider
import com.zs.gallery.MainActivity
import com.zs.gallery.R
import com.zs.gallery.common.Action
import com.zs.gallery.common.Mapped
import com.zs.gallery.common.SelectionTracker.Level
import com.zs.gallery.common.ellipsize
import com.zs.gallery.files.FilesViewState
import com.zs.gallery.files.RouteFiles
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.MediaViewerViewState
import com.zs.gallery.viewer.RouteViewer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "StoreViewModel"

private const val SOURCE_TIMELINE = 0
private const val SOURCE_FOLDER = 1
private const val SOURCE_BIN = 2
private const val SOURCE_FAV = 3

abstract class StoreViewModel(
    handle: SavedStateHandle,
    private val provider: MediaProvider,
) : KoinViewModel() {

    //  Represent the key and source of the data
    val key = with(RouteFiles) { handle.key }
    val source = when (key) {
        null -> SOURCE_TIMELINE
        RouteFiles.ALBUM_BIN -> SOURCE_BIN
        RouteFiles.ALBUM_FAV -> SOURCE_FAV
        else -> SOURCE_FOLDER
    }


    abstract suspend fun emit(values: List<MediaFile>)

    @SuppressLint("NewApi")
    private val flow = provider
        .observer(MediaProvider.EXTERNAL_CONTENT_URI)
        .let {
            if (SOURCE_FAV == source)
                return@let it.combine(
                    preferences.observe(Settings.KEY_FAVOURITE_FILES),
                    transform = { _, ids ->
                        provider.fetchFiles(
                            ids = ids.toLongArray(),
                            order = MediaProvider.COLUMN_DATE_MODIFIED,
                            ascending = false
                        )
                    }
                )
            it.map(
                transform = { _ ->
                    when (source) {
                        SOURCE_BIN -> provider.fetchTrashedFiles()
                        SOURCE_FOLDER -> provider.fetchFilesFromDirectory(
                            key!!,
                            order = MediaProvider.COLUMN_DATE_MODIFIED,
                            ascending = false
                        )

                        else -> provider.fetchFiles(
                            order = MediaProvider.COLUMN_DATE_MODIFIED,
                            ascending = false
                        )
                    }
                }
            )
        }
        .onEach { emit(it) }
        .catch { exception ->
            Log.d(TAG, "provider: ${exception.stackTraceToString()}")
            val action = report(exception.message ?: getText(R.string.msg_unknown_error))
        }

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
             val res =   showSnackbar(
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
            val result = runCatching(TAG) {
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

    /** Shares file(s) represented by id(s).*/
    fun share(resolver: Activity, vararg id: Long) {
        viewModelScope.launch {
            // Get the list of selected items to share.
            val selected = id
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
                resolver.startActivity(Intent.createChooser(intent, "Share Photos & Video"))
            } catch (e: Exception) {
                // Handle exceptions and display an error message.
                showPlatformToast(R.string.msg_error_sharing_files)
                Log.d(TAG, "share: ${e.message}")
            }
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

    init {
        flow.launchIn(viewModelScope)
    }
}

// TODO - What will happen when these are shared by more than one instance at a time
//        and what will happen when these are changed by some instance.
private val DELETE = Action(R.string.delete, Icons.TwoTone.Delete)
private val SHARE = Action(R.string.share, Icons.Outlined.Share)
private val STAR = Action(R.string.like, Icons.Outlined.StarBorder)
private val UN_STAR = Action(R.string.unlike, Icons.TwoTone.Star)
private val SELECT_ALL = Action(R.string.select_all, Icons.Outlined.SelectAll)
private val RESTORE = Action(R.string.restore, Icons.Outlined.Restore)
private val EMPTY_BIN = Action(R.string.empty_bin, Icons.Outlined.PlaylistRemove)
private val STAR_APP = Action(R.string.rate_us, Icons.TwoTone.WorkspacePremium)

/**
 * Represents the state of the files screen.
 */
class FilesViewModel(
    handle: SavedStateHandle,
    provider: MediaProvider,
) : StoreViewModel(handle, provider), FilesViewState {

    override val meta: Pair<ImageVector, CharSequence> =
        when (source) {
            SOURCE_BIN -> Icons.Outlined.Recycling to getText(R.string.recycle_bin)
            SOURCE_FAV -> Icons.Outlined.HotelClass to getText(R.string.favourites)
            SOURCE_TIMELINE -> ImageVector(R.drawable.ic_app) to getText(R.string.timeline)
            else -> Icons.TwoTone.FolderCopy to buildAnnotatedString {
                append(PathUtils.name(key!!).ellipsize(15))
                withStyle(ParagraphStyle(lineHeight = 12.sp)) {
                    withStyle(SpanStyle(fontSize = 11.sp, color = Color.DarkGray)) {
                        appendLine(PathUtils.parent(key))
                    }
                }
            }
        }

    override var data: Mapped<MediaFile>? by mutableStateOf(null)

    // Represents the
    override val selected = mutableStateListOf<Long>()
    override val isInSelectionMode: Boolean by derivedStateOf(selected::isNotEmpty)
    override val allSelected: Boolean by derivedStateOf {
        // FixMe: For now this seems reasonable.
        //      However take the example of the case when size is same but ids are different
        selected.size == data?.values?.sumOf { it.size }
    }

    @Suppress("BuildListAdds")
    override val actions: List<Action> by derivedStateOf {
        buildList {
            if (source != SOURCE_TIMELINE && !allSelected)
                this += SELECT_ALL
            when (source) {
                SOURCE_BIN if (!isInSelectionMode) -> {
                    this += RESTORE; this += EMPTY_BIN
                }

                SOURCE_BIN -> {
                    this += RESTORE; this += DELETE
                }

                SOURCE_TIMELINE if (!isInSelectionMode) -> this += STAR_APP
                SOURCE_FAV if(!isInSelectionMode) -> {
                    this += UN_STAR
                }

                else -> {
                    this += STAR; this += DELETE; this += SHARE
                }
            }
        }
    }

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
        Log.d(TAG, "consume: ${data.size}")
        return data
    }

    override fun selectAll() {
        val data = data ?: return
        // Iterate through all items in the data and select them if they are not already selected.
        data.forEach { _, items ->
            items.forEach { item ->
                val id = item.id
                if (!selected.contains(id)) {
                    selected.add(id)
                }
            }
        }
    }

    override fun select(id: Long) {
        val contains = selected.contains(id)
        if (contains) selected.remove(id) else selected.add(id)
    }

    fun evaluateGroupSelectionLevel(key: String): Level {
        // Return NONE if data is not available.
        val data = data?.get(key) ?: return Level.NONE
        // Count selected
        val count = data.count { it.id in selected }
        return when (count) {
            data.size -> Level.FULL // All items in the group are selected.
            in 1..data.size -> Level.PARTIAL // Some items in the group are selected.
            else -> Level.NONE // No items in the group are selected.
        }
    }

    override fun isGroupSelected(key: String) =
        derivedStateOf { evaluateGroupSelectionLevel(key) }

    override fun select(key: String) {
        // Return if data is not available.
        val data = data ?: return
        // Get the current selection level of the group.
        val level = evaluateGroupSelectionLevel(key)
        // Get the IDs of all items in the group.
        val all = data[key]?.map { it.id } ?: emptyList()
        // Update the selected items based on the group selection level.
        when (level) {
            Level.NONE -> selected.addAll(all) // Select all items in the group.
            Level.PARTIAL -> selected.addAll(all.filterNot { it in selected }) // Select only unselected items.
            Level.FULL -> selected.removeAll(all.filter { it in selected }) // Deselect all selected items.
        }
    }

    override suspend fun emit(values: List<MediaFile>) {
        if (source != SOURCE_BIN) {
            data = values.groupBy {
                DateUtils.getRelativeTimeSpanString(
                    it.dateModified,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS
                ).toString()
            }
            // return
            return
        }

        // Calculate days left for each file
        data =
            values.groupBy() { (it.expires - System.currentTimeMillis()).milliseconds.inWholeDays }
                // Transform the keys to user-friendly labels
                .mapKeys { (daysLeft, _) ->
                    when {
                        daysLeft <= 0 -> getText(R.string.today) // Handle expired items
                        daysLeft == 1L -> getText(R.string.trash_one_day_left)
                        else -> getText(
                            R.string.trash_days_left_d,
                            daysLeft
                        ) // Format remaining days
                    }.toString()
                }
    }

    override fun buildRouteViewer(id: Long) = RouteViewer(id, key ?: "")

    override fun onAction(value: Action, activity: Activity) {
        viewModelScope.launch {
            val focused = when {
                // first pref is given to selected;
                // otherwise whole collection is considered selected.
                // these options will not be present in screen if no items are selected.
                selected.isNotEmpty() -> consume()
                else -> {
                    val data = data ?: return@launch
                    buildList {
                        data.forEach { _, items ->
                            this.addAll(items.map { it.id })
                        }
                    }.toLongArray()
                }
            }
            when (value) {
                RESTORE -> restore(resolver = activity, *focused)
                DELETE if(source == SOURCE_BIN) -> delete( activity, *focused)
                DELETE -> remove( activity, *focused)
                STAR -> toggleLike(*focused)
                UN_STAR -> toggleLike(*focused)
                SHARE -> share( activity, *focused)
                EMPTY_BIN -> trash(resolver = activity, *focused)
                STAR_APP -> (activity as MainActivity).launchAppStore()
                SELECT_ALL -> selectAll()
                else -> error("Action not supported")
            }
        }
    }
}

/**
 * Creates an Intent to edit an image at the given URI.
 *
 * @param uri The URI of the image to edit.
 * @return An Intent configured for image editing.
 */
private fun EditIn(uri: Uri) =
    Intent(Intent.ACTION_EDIT).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grants temporary read permission to the editing app
    }

/**
 * Sets the wallpaper using the provided URI.
 *
 * @param uri The URI of the image to be set as wallpaper.
 */
private fun Activity.setWallpaper(uri: Uri) {
    try {
        // first try to set the wallpaper through offcial way
        val intent = Intent("android.service.wallpaper.CROP_AND_SET_WALLPAPER").apply {
            setDataAndType(uri, "image/*")
            putExtra("mimeType", "image/*") // Specifies the MIME type of the image
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grants temporary read permission to the wallpaper app
            addCategory(Intent.CATEGORY_DEFAULT)
            startActivity(intent)
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // If the official intent is not supported, try using ACTION_ATTACH_DATA
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            addCategory(Intent.CATEGORY_DEFAULT);
            // Grant read permission to the wallpaper app
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//add this if your targetVersion is more than Android 7.0+
            setDataAndType(uri, "image/*");
            putExtra("mimeType", "image/*");
        }
        startActivity(Intent.createChooser(intent, getString(R.string.viewer_set_as)));
    } catch (e: Exception) {
        // If any other exception occurs, show a toast message to the user
        android.widget.Toast.makeText(
            this,
            getString(R.string.viewer_msg_no_wallpaper_app_found),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

private val EDIT_IN = Action(R.string.edit_in, Icons.Outlined.Edit)
private val USE_AS = Action(R.string.set_as_wallpaper, Icons.Outlined.Wallpaper)

class MediaViewerViewModel(
    handle: SavedStateHandle,
    provider: MediaProvider,
) : StoreViewModel(handle, provider), MediaViewerViewState {

    private val _focused = with(RouteViewer) { handle.focused }
    override var focused: Long by mutableLongStateOf(_focused)

    override var data: List<MediaFile> by mutableStateOf(emptyList())
    override var details: MediaFile? by mutableStateOf(null)

    override var showDetails: Boolean
        get() = details != null
        set(value) {
            details = when {
                value -> current
                else -> null
            }
        }


    override suspend fun emit(values: List<MediaFile>) {
        delay(1000) // necessary for shared animation to work effectively.
        // experiment for optimal value
        data = values
    }

    override val favourite: Boolean by derivedStateOf { favourites.contains(focused) }
    val current inline get() = data.find { it.id == focused }

    override val title: CharSequence by derivedStateOf {
        buildAnnotatedString {
            val current = current ?: return@buildAnnotatedString
            withStyle(SpanStyle(fontSize = 12.sp)) {
                append(current.name.ellipsize(20))
            }
            append("\n")
            val modified =
                DateUtils.formatDateTime(
                    null,
                    current.dateModified,
                    DateUtils.FORMAT_ABBREV_MONTH
                )
            append(modified)
        }
    }

    val favourites = mutableStateListOf<Long>().also { favourites ->
        // Observe changes to the favorite files preference
        preferences.observe(Settings.KEY_FAVOURITE_FILES)
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

    @Suppress("BuildListAdds")
    override val actions: List<Action> by derivedStateOf {
        buildList {
            if (source == SOURCE_BIN) {
                this += RESTORE; this += DELETE
                return@buildList
            }
            Log.d(TAG, "actions: changed ")
            this += if (favourite) UN_STAR else STAR
            this += SHARE
            this += DELETE
            // if this is video currently return otherwise editing can be called for video also
            if (current?.isImage == false) return@buildList
            this += USE_AS
            this += EDIT_IN
        }
    }

    override fun onAction(value: Action, activity: Activity) {
        if (data.isEmpty()) return
        when(value){
            RESTORE -> restore(activity, focused)
            DELETE if (source == SOURCE_BIN) -> delete(activity, focused)
            DELETE -> remove(activity, focused)
            STAR -> toggleLike(focused)
            UN_STAR -> toggleLike(focused)
            SHARE -> share(activity, focused)
            USE_AS -> activity.setWallpaper(current!!.mediaUri)
            EDIT_IN -> activity.startActivity(EditIn(current!!.mediaUri))
            else -> error("Action not supported")
        }
    }
}
