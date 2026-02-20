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
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Star
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zs.core.BuildConfig
import com.zs.core.store.MediaFile
import com.zs.core.store.MediaProvider
import com.zs.gallery.R
import com.zs.gallery.common.Action
import com.zs.gallery.common.EditInIntent
import com.zs.gallery.common.FallbackWallpaperIntent
import com.zs.gallery.common.GoogleLensIntent
import com.zs.gallery.common.NearByShareIntent
import com.zs.gallery.common.ShareFilesIntent
import com.zs.gallery.common.WallpaperIntent
import com.zs.gallery.common.debounceAfterFirst
import com.zs.gallery.common.icons.NearbyShare
import com.zs.gallery.files.RouteFiles.SOURCE_BIN
import com.zs.gallery.files.RouteFiles.SOURCE_FAV
import com.zs.gallery.files.RouteFiles.SOURCE_FOLDER
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.MediaViewerViewState
import com.zs.gallery.viewer.RouteViewer
import com.zs.gallery.viewer.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "MediaViewerViewModel"


private val DELETE = Action(R.string.delete, Icons.TwoTone.Delete)
private val SHARE = Action(R.string.share, Icons.Outlined.Share)
private val STAR = Action(R.string.like, Icons.Outlined.StarBorder)
private val UN_STAR = Action(R.string.unlike, Icons.TwoTone.Star)
private val RESTORE = Action(R.string.restore, Icons.Outlined.Restore)
private val QUICK_SHARE = Action(R.string.beam, Icons.Filled.NearbyShare)
private val EDIT_IN = Action(R.string.edit_in, Icons.Outlined.Edit)
private val USE_AS = Action(R.string.wallpaper, Icons.Outlined.Wallpaper)
private val GOOGLE_LENS = Action(R.string.lens, Icons.Outlined.DocumentScanner)

class MediaViewerViewModel(
    handle: SavedStateHandle,
    provider: MediaProvider,
) : StoreViewModel(provider), MediaViewerViewState {


    val _args = handle[RouteViewer]

    override var focused: Long by mutableLongStateOf(_args.second)
    private val source = _args.first

    private val sdf = SimpleDateFormat("MMMM d, yyyy\nh:mm a", Locale.getDefault())
    override var data: List<MediaFile> by mutableStateOf(emptyList())
    override var details: MediaFile? by mutableStateOf(null)
    val current inline get() = data.find { it.id == focused }
    override val title: CharSequence by derivedStateOf {
        sdf.format(Date(current?.dateModified ?: return@derivedStateOf ""))
    }

    //
    override var showDetails: Boolean
        get() = details != null
        set(value) {
            details = when {
                value -> current
                else -> null
            }
        }

    //
    override val favourite: Boolean by derivedStateOf { favourites.contains(focused) }
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

    override fun onPerformAction(value: Action, resolver: Activity) {
        launch {
            when(value){
                RESTORE -> restore(resolver, focused)
                DELETE if (source == SOURCE_BIN) -> delete(resolver, focused)
                DELETE -> remove(resolver, focused)
                STAR -> toggleLike(focused)
                UN_STAR -> toggleLike(focused)
                GOOGLE_LENS -> {
                    val result = runCatching {
                        resolver.startActivity(GoogleLensIntent(current!!.mediaUri))
                    }
                    if (result.isFailure)
                        showPlatformToast(R.string.msg_viewer_scr_no_g_lens)
                }
                SHARE -> {
                    val res = runCatching {
                        resolver.startActivity(ShareFilesIntent(focused))
                    }
                    if (res.isFailure)
                        showPlatformToast(R.string.msg_error_sharing_files)
                }
                QUICK_SHARE -> {
                    val res = runCatching {
                        resolver.startActivity(NearByShareIntent(focused))
                    }
                    if (res.isFailure)
                        showPlatformToast(R.string.msg_files_quick_share_error)
                }
                USE_AS -> {
                    val res = runCatching {
                        resolver.startActivity(WallpaperIntent(current!!.mediaUri))
                    }
                    if (res.isFailure) resolver.startActivity(FallbackWallpaperIntent(current!!.mediaUri))
                }
                EDIT_IN -> resolver.startActivity(EditInIntent(current!!.mediaUri))
                else -> error("Action not supported")
            }
        }
    }

    @Suppress("BuildListAdds")
    override val actions: List<Action> by derivedStateOf {
        buildList {
            if (source == SOURCE_BIN) {
                this += RESTORE; this += DELETE
                return@buildList
            }
            this += if (favourite) UN_STAR else STAR
            if (BuildConfig.FLAVOR != BuildConfig.FLAVOR_COMMUNITY)
                this += GOOGLE_LENS
            this += QUICK_SHARE
            // if this is video currently return otherwise editing can be called for video also
            if (current?.isImage == true)
                this += USE_AS; this += EDIT_IN
            this += SHARE
            this += DELETE
        }
    }

    val onRequestUpdate: suspend (List<MediaFile>) -> Unit = { values ->
        delay(600) // necessary for shared animation to work effectively.
        // experiment for optimal value
        data = values
    }

    init {
        @Suppress("NewApi")
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
                            _args.third!!,
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