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

package com.zs.gallery.bin

import android.annotation.SuppressLint
import android.app.Activity
import com.zs.domain.store.Trashed
import com.zs.gallery.common.FileActions
import com.zs.gallery.common.Route
import com.zs.gallery.common.SelectionTracker

object RouteTrash : Route

interface TrashViewState : FileActions, SelectionTracker {
    val data: Map<String, List<Trashed>>?
    /**
     * Removes all items from bin.
     */
    fun empty(activity: Activity)

    /**
     * Restores all items from the bin
     */
    @SuppressLint("NewApi")
    fun restoreAll(activity: Activity){
        selectAll()
        restore(activity)
    }
}