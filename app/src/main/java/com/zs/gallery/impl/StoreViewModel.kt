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
import android.text.format.DateUtils
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
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
import com.zs.core.common.PathUtils
import com.zs.core.store.MediaFile
import com.zs.core.store.MediaProvider
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
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "StoreViewModel"

private const val SOURCE_TIMELINE = 0
private const val SOURCE_FOLDER = 1
private const val SOURCE_BIN = 2
private const val SOURCE_FAV = 3

abstract class StoreViewModel(
    handle: SavedStateHandle,
    provider: MediaProvider,
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

    init {
        flow.launchIn(viewModelScope)
    }
}

// TODO - What will happen when these are shared by more than one instance at a time
//        and what will happen when these are changed by some instance.
private val DELETE = Action(R.string.delete, Icons.TwoTone.Delete)
private val SHARE = Action(R.string.share, Icons.Outlined.Share)
private val EDIT_IN = Action(R.string.edit_in, Icons.Outlined.Edit)
private val STAR = Action(R.string.like, Icons.Outlined.Star)
private val UN_STAR = Action(R.string.like, Icons.TwoTone.Star)
private val SELECT_ALL = Action(R.string.select_all, Icons.Outlined.SelectAll)
private val RESTORE = Action(R.string.restore, Icons.Outlined.Restore)
private val EMPTY_BIN = Action(R.string.empty_bin, Icons.Outlined.PlaylistRemove)
private val STAR_APP = Action(R.string.rate_us, Icons.TwoTone.WorkspacePremium)
private val USE_AS = Action(R.string.set_as_wallpaper, Icons.Outlined.Wallpaper)


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
                append(PathUtils.name(key!!))
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

    override fun buildRouteViewer(id: Long) = RouteViewer(id)

    override fun onAction(value: Action, activity: Activity) {
        TODO("Not yet implemented")
    }
}

class MediaViewerViewModel(
    handle: SavedStateHandle,
    provider: MediaProvider,
) : StoreViewModel(handle, provider), MediaViewerViewState {

    private val _focused = with(RouteViewer) { handle.focused }
    override var focused: Long by mutableLongStateOf(_focused)

    override var data: List<MediaFile> by mutableStateOf(emptyList())
    override var details: MediaFile? by mutableStateOf(null)

    override suspend fun emit(values: List<MediaFile>) {
        delay(500) // necessary for shared animation to work effectively.
        // experiment for optimal value
        data = values
    }

    override val title: CharSequence by derivedStateOf {
        buildAnnotatedString {
            val current = current ?: return@buildAnnotatedString
            appendLine(current.name.ellipsize(15))
            val modified =
                DateUtils.formatDateTime(null, current.dateModified, DateUtils.FORMAT_SHOW_DATE)
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
            }.launchIn(viewModelScope)
    }
    override val favourite: Boolean by derivedStateOf { favourites.contains(focused) }

    val current inline get() = data.find { it.id == focused }

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


    override var showDetails: Boolean
        get() = details != null
        set(value) {
            details = when {
                value -> current
                else -> null
            }
        }

    override fun onAction(item: Action, activity: Activity) {
        TODO("Not yet implemented")
    }
}
