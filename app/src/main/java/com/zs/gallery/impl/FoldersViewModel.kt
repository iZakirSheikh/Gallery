/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 04-04-2025.
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


import android.text.format.DateUtils
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.zs.compose.foundation.castTo
import com.zs.core.store.Folder
import com.zs.core.store.MediaProvider
import com.zs.gallery.R
import com.zs.gallery.common.Action
import com.zs.gallery.common.Filter
import com.zs.gallery.common.Mapped
import com.zs.gallery.folders.FoldersViewState
import com.zs.preferences.Key
import com.zs.preferences.StringSaver
import com.zs.preferences.stringPreferenceKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

private const val TAG = "FoldersViewModel"

private val Folder.firstTitleChar
    inline get() = name.uppercase(Locale.ROOT)[0].toString()

private val ORDER_BY_DATE_MODIFIED = Action(R.string.last_modified, Icons.Outlined.DateRange)
private val ORDER_BY_NAME = Action(R.string.name, Icons.Outlined.TextFields)
private val ORDER_BY_NONE = Action(R.string.none)

private val PrefFilter: Key.Key2<String, Filter?> = stringPreferenceKey(
    "folders_filter",
    null,
    object : StringSaver<Filter?> {
        val delimiter = " | "
        override fun restore(value: String): Filter? {
            if (value.isEmpty()) return null
            val (first, second) = value.split(delimiter, limit = 2)
            val order = when (second) {
                ORDER_BY_DATE_MODIFIED.id -> ORDER_BY_DATE_MODIFIED
                ORDER_BY_NAME.id -> ORDER_BY_NAME
                else -> ORDER_BY_NONE
            }
            return (first == "0") to order

        }

        override fun save(value: Filter?): String {
            if (value == null) return ""
            val first = if (value.first == true) "1" else "0"
            val second = value.second.id
            return "$first$delimiter$second"
        }
    }
)

class FoldersViewModel(
    private val provider: MediaProvider,
) : KoinViewModel(), FoldersViewState {

    override val orders: List<Action> = listOf(ORDER_BY_NONE, ORDER_BY_DATE_MODIFIED, ORDER_BY_NAME)

    override fun filter(ascending: Boolean, order: Action) {
        if (ascending == filter.first && order == filter.second) return
        // means only change in ascending happened
        // we don't support that, in order none.
        if (order == filter.second && order == ORDER_BY_NONE && filter.first != ascending)
            return
        val newFilter = ascending to order
        preferences[PrefFilter] = newFilter
        filter = newFilter
    }

    override var filter: Filter by mutableStateOf(preferences[PrefFilter] ?: (true to ORDER_BY_NAME))

    override val data = combine(
        snapshotFlow(::filter), provider.observer(MediaProvider.EXTERNAL_CONTENT_URI)
    ) { (ascending, order), _ ->
        val folders = provider.fetchFolders(ascending = false)
        val result = when (order) {
            ORDER_BY_NONE -> folders.groupBy { "" }
            ORDER_BY_NAME -> folders.sortedBy { it.firstTitleChar }
                .let { if (ascending) it else it.reversed() }.groupBy { it.firstTitleChar }

            ORDER_BY_DATE_MODIFIED -> folders.sortedBy { it.lastModified }
                .let { if (ascending) it else it.reversed() }
                .groupBy { DateUtils.getRelativeTimeSpanString(it.lastModified).toString() }

            else -> error("Oops invalid id passed $order")
        }
        // This should be safe
        castTo(result) as Mapped<Folder>
    }
        // Catch any exceptions in upstream flow and emit using the snackbar.
        .catch { exception ->
            Log.e(TAG, "provider: ${exception.message}")
            // Handle any exceptions that occur during the flow.
            // This might involve logging the exception using Firebase Crashlytics.
            // Display a toast message to the user, indicating something went wrong and suggesting they report the issue.
            report(
                exception.message ?: getText(R.string.msg_unknown_error),
            )
        }
        // Convert to state.
        .stateIn(viewModelScope, started = SharingStarted.Lazily, null)
}
