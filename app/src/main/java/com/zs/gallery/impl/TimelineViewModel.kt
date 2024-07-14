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

import android.app.Activity
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
import com.zs.api.store.MediaFile
import com.zs.api.store.MediaProvider
import com.zs.compose_ktx.toast.Toast
import com.zs.gallery.R
import com.zs.gallery.common.GroupSelectionLevel
import com.zs.gallery.files.TimelineViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

private const val TAG = "TimelineViewModel"

class TimelineViewModel(val provider: MediaProvider) : KoinViewModel(), TimelineViewState {

    // Trigger for refreshing the list
    private val _trigger = MutableStateFlow(false)

    /**
     * Forces the data store to refresh.
     */
    fun invalidate() {
        _trigger.value = (!_trigger.value)
    }

    override var data: Map<String, List<MediaFile>>? by mutableStateOf(null)
    override val selected = mutableStateListOf<Long>()
    override val isInSelectionMode: Boolean by derivedStateOf(selected::isNotEmpty)
    override fun clear() = selected.clear()

    override val allSelected: Boolean by derivedStateOf {
        // FixMe: For now this seems reasonable.
        //      However take the example of the case when size is same but ids are different
        selected.size == data?.values?.flatten()?.size
    }

    private fun evaluateGroupSelectionLevel(key: String): GroupSelectionLevel {
        val data = data ?: return GroupSelectionLevel.NONE
        val all = data[key]?.map { it.id } ?: emptyList()
        val count = all.count { it in selected }
        return when (count) {
            all.size -> GroupSelectionLevel.FULL
            in 1..all.size -> GroupSelectionLevel.PARTIAL
            else -> GroupSelectionLevel.NONE
        }
    }

    override fun isGroupSelected(key: String): State<GroupSelectionLevel> =
        derivedStateOf { evaluateGroupSelectionLevel(key) }

    override fun selectAll() {
        val data = data ?: return
        data.values.flatten().forEach { items ->
            val id = items.id
            val contains = selected.contains(id)
            if (!contains) selected.add(id)
        }
    }

    override fun addToFavourite() {
        TODO("Not yet implemented")
    }

    override fun select(id: Long) {
        val contains = selected.contains(id)
        if (contains) selected.remove(id) else selected.add(id)
    }

    override fun move(dest: String): Unit = TODO("Not yet implemented")
    override fun copy(dest: String): Unit = TODO("Not yet implemented")
    override fun rename(name: String): Unit = TODO("Not yet implemented")
    override fun share(activity: Activity): Unit = TODO("Not yet implemented")
    override fun restore(activity: Activity): Unit = TODO("Not yet implemented")
    override fun delete(activity: Activity, trash: Boolean) = TODO("Not yet implemented")

    private suspend fun fetch(): Map<String, List<MediaFile>> {
        return provider.fetchFiles(order = MediaProvider.COLUMN_DATE_MODIFIED, ascending = false)
            .groupBy {
                DateUtils.getRelativeTimeSpanString(
                    it.dateModified,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS
                ).toString()
            }
    }

    init {
        Log.d(TAG, "${this::class.simpleName}: created.")
        provider.observer(MediaProvider.EXTERNAL_CONTENT_URI)
            .combine(_trigger) { _, _ -> data = fetch() }
            .catch { exception ->
                Log.e(TAG, "provider: ${exception.message}")
                // Handle any exceptions that occur during the flow.
                // This might involve logging the exception using Firebase Crashlytics.
                // Display a toast message to the user, indicating something went wrong and suggesting they report the issue.
                val action = showToast(
                    getText(R.string.oops_something_went_wrong_please_report_this_to_developer),
                    getText(R.string.report),
                    Icons.Outlined.NearbyError,
                    Color.Rose,
                    Toast.DURATION_INDEFINITE
                )
            }
            .launchIn(viewModelScope)
    }
}
