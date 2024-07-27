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
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import com.zs.api.store.MediaProvider
import com.zs.gallery.preview.RouteViewer
import com.zs.gallery.preview.ViewerViewState

class ViewerViewModel(
    handle: SavedStateHandle,
    private val provider: MediaProvider
): KoinViewModel(), ViewerViewState {

    val args = RouteViewer[handle]

    override var index by mutableIntStateOf(args.index)

    override fun fetchUriForIndex(): Uri = MediaProvider.contentUri(args.ids[index])

    override val size: Int get() = args.ids.size

    override fun fetchIdForIndex(): Long {
       return args.ids[index]
    }

    override fun delete(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun remove(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun trash(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun move(dest: String) {
        TODO("Not yet implemented")
    }

    override fun copy(dest: String) {
        TODO("Not yet implemented")
    }

    override fun rename(name: String) {
        TODO("Not yet implemented")
    }

    override fun share(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun restore(activity: Activity) {
        TODO("Not yet implemented")
    }
}