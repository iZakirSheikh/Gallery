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

import android.annotation.SuppressLint
import android.app.Activity
import android.text.format.DateUtils
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.FolderCopy
import androidx.compose.material.icons.twotone.HotelClass
import androidx.compose.material.icons.twotone.Star
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.zs.gallery.MainActivity
import com.zs.gallery.R
import com.zs.gallery.common.Action
import com.zs.gallery.common.Mapped
import com.zs.gallery.common.NearByShareIntent
import com.zs.gallery.common.SelectionTracker.Level
import com.zs.gallery.common.ShareFilesIntent
import com.zs.gallery.common.debounceAfterFirst
import com.zs.gallery.common.ellipsize
import com.zs.gallery.common.icons.NearbyShare
import com.zs.gallery.files.FilesViewState
import com.zs.gallery.files.RouteFiles
import com.zs.gallery.files.RouteFiles.SOURCE_BIN
import com.zs.gallery.files.RouteFiles.SOURCE_FAV
import com.zs.gallery.files.RouteFiles.SOURCE_FOLDER
import com.zs.gallery.files.RouteFiles.SOURCE_TIMELINE
import com.zs.gallery.files.get
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.RouteViewer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "FilesViewModel"

private val DELETE = Action(R.string.delete, Icons.TwoTone.Delete)
private val SHARE = Action(R.string.share, Icons.Outlined.Share)
private val STAR = Action(R.string.like, Icons.Outlined.StarBorder)
private val UN_STAR = Action(R.string.unlike, Icons.TwoTone.Star)
private val SELECT_ALL = Action(R.string.select_all, Icons.Outlined.SelectAll)
private val RESTORE = Action(R.string.restore, Icons.Outlined.Restore)
private val EMPTY_BIN = Action(R.string.empty_bin, Icons.Outlined.PlaylistRemove)
private val STAR_APP = Action(R.string.rate_us, Icons.TwoTone.HotelClass)
private val QUICK_SHARE = Action(R.string.beam, Icons.Filled.NearbyShare)
private val TELEGRAM = Action(R.string.report, Icons.Outlined.SupportAgent)

@SuppressLint("NewApi")
class FilesViewModel(
    handle: SavedStateHandle,
    provider: MediaProvider,
) : StoreViewModel(provider), FilesViewState {

    val param = handle[RouteFiles]
    val source = param.first
    val arg = param.second

    override val meta: Pair<ImageVector?, CharSequence> =
        when (source) {
            SOURCE_BIN -> Icons.Outlined.Recycling to getText(R.string.recycle_bin)
            SOURCE_FAV -> Icons.Outlined.FavoriteBorder to getText(R.string.favourites)
            SOURCE_TIMELINE -> null to getText(R.string.timeline)
            else -> Icons.TwoTone.FolderCopy to buildAnnotatedString {
                append(PathUtils.name(arg!!).ellipsize(10))
                withStyle(ParagraphStyle(lineHeight = 12.sp)) {
                    withStyle(SpanStyle(fontSize = 11.sp, color = Color.DarkGray)) {
                        appendLine(PathUtils.parent(arg))
                    }
                }
            }
        }

    override var data: Mapped<MediaFile>? by mutableStateOf(null)

    // Represents the
    override fun clear() = selected.clear()
    override val selected = mutableStateListOf<Long>()
    override val isInSelectionMode: Boolean by derivedStateOf(selected::isNotEmpty)
    override val allSelected: Boolean by derivedStateOf {
        // FixMe: For now this seems reasonable.
        //      However take the example of the case when size is same but ids are different
        selected.size == data?.values?.sumOf { it.size }
    }


    /**
     * Consumes the currently selected items and returns them as an array.
     *
     * This function creates a new array containing the selected items, clears the `selected` list, and returns the array.
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

    override fun direction(id: Long) =
        RouteViewer(source, id, arg ?: "")

    @Suppress("BuildListAdds")
    override val actions: List<Action> by derivedStateOf {
        buildList {
            // Add "Select All" action if not in timeline and not all items are selected
            if (source != SOURCE_TIMELINE && !allSelected)
                this += SELECT_ALL
            // Handle actions specific to the Bin source
            if (source == SOURCE_BIN && !isInSelectionMode) {
                this += RESTORE; this += EMPTY_BIN
                return@buildList
            }
            // Handle actions for bin when not selected.
            else if (source == SOURCE_BIN) {
                this += RESTORE; this += DELETE
            }
            // Handle actions for Timeline when not in selection mode
            else if (source == SOURCE_TIMELINE && !isInSelectionMode) {
                this += STAR_APP; this += TELEGRAM
            }
            // Handle actions for Favorites when not in selection mode
            else if (source == SOURCE_FAV && !isInSelectionMode) {
                this += UN_STAR
            }
            // Default actions for other cases (e.g., albums, selection mode)
            // These actions are typically available when items are selected or in a general context.
            // - QUICK_SHARE: For quickly sharing selected items.
            // - STAR: For marking items as favorites.
            // - DELETE: For moving items to the bin.
            // - SHARE: For standard sharing options.
            else {
                this += QUICK_SHARE; this += STAR; this += DELETE; this += SHARE
            }
        }
    }

    override fun onRequest(value: Action, resolver: Activity) {
        // Launch a coroutine to handle the action asynchronously.
        launch {
            // Determine the items to focus on for the action.
            val focused = when {
                // If there are selected items, consume them.
                // "consume()" clears the selection and returns the selected item IDs.
                selected.isNotEmpty() -> consume()
                // Otherwise, if no items are selected, consider all items in the current view as focused.
                // This is relevant for actions like "Empty Bin" or "Unlike" (when viewing favorites).
                else -> {
                    // If data is null (e.g., still loading), do nothing.
                    val data = data ?: return@launch
                    // Build a list of all item IDs from the current data.
                    buildList {
                        data.forEach { _, items ->
                            this.addAll(items.map { it.id })
                        }
                    }.toLongArray()
                }
            }
            // Perform the action based on the 'value' parameter.
            when (value) {
                RESTORE -> restore(resolver = resolver, *focused)
                // If deleting and the source is the bin, permanently delete.
                DELETE if(source == SOURCE_BIN) -> delete(resolver, *focused)
                DELETE -> remove(resolver, *focused)
                STAR -> toggleLike(*focused)
                UN_STAR -> toggleLike(*focused)
                EMPTY_BIN -> delete(resolver = resolver, *focused)
                // TODO - Instead of this; launch app-intent.
                STAR_APP -> (resolver as MainActivity).launchAppStore()
                SELECT_ALL -> selectAll()
                TELEGRAM -> resolver.startActivity(Settings.TelegramIntent)
                // Handle sharing files.
                SHARE -> {
                    val result = runCatching {
                        resolver.startActivity(ShareFilesIntent(*focused))
                    }
                    // If sharing fails, display an error message.
                    if (result.isFailure)
                        error(getText(R.string.msg_error_sharing_files))
                }
                // Handle quick sharing (e.g., Nearby Share).
                QUICK_SHARE -> {
                    val res = runCatching {
                        resolver.startActivity(NearByShareIntent(*focused))
                    }
                    // If quick sharing fails, display an error message.
                    if (res.isFailure)
                        error(getText(R.string.msg_files_quick_share_error))
                }

                else -> error("${getText(value.label)} is not supported.")
            }
        }
    }

    val onRequestUpdate: suspend (List<MediaFile>) -> Unit = { values ->
        data = if (source == SOURCE_BIN)
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
        //
        else
            values.groupBy {
                DateUtils.getRelativeTimeSpanString(
                    it.dateModified,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS
                ).toString()
            }
    }

    init {
        provider.observer(MediaProvider.EXTERNAL_CONTENT_URI)
            .debounceAfterFirst(300)
            .let { stream ->
                when (source) {
                    SOURCE_FAV -> stream.combine(
                        preferences.observe(Settings.KEY_FAVOURITE_FILES).debounceAfterFirst(300),
                        transform = { _, ids ->
                            provider.fetchFiles(
                                ids = ids.toLongArray(),
                                order = MediaProvider.COLUMN_DATE_MODIFIED,
                                ascending = false
                            )
                        }
                    )

                    SOURCE_BIN -> stream.map { provider.fetchTrashedFiles() }
                    SOURCE_FOLDER -> stream.map {
                        provider.fetchFilesFromDirectory(
                            arg!!,
                            order = MediaProvider.COLUMN_DATE_MODIFIED,
                            ascending = false
                        )
                    }

                    else -> stream.map {
                        provider.fetchFiles(
                            order = MediaProvider.COLUMN_DATE_MODIFIED,
                            ascending = false
                        )
                    }
                }
            }
            .onEach(onRequestUpdate)
            .catch { exception ->
                Log.d(TAG, "provider: ${exception.stackTraceToString()}")
                val action = report(exception.message ?: getText(R.string.msg_unknown_error))
            }
            .launchIn(viewModelScope)
    }
}