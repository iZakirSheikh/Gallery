/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 26-04-2025.
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

package com.zs.gallery.viewer

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.zs.core.store.MediaFile
import com.zs.gallery.common.Action
import com.zs.gallery.common.Route

object RouteViewer : Route {

    private const val PARAM_KEY = "param_key"
    private const val PARAM_FOCUSED = "param_focused"

    override val route: String =
        "$domain/{${PARAM_FOCUSED}}/{${PARAM_KEY}}"

    val SavedStateHandle.key
        get() = get<String>(PARAM_KEY).takeIf { !it.isNullOrEmpty() }

    val SavedStateHandle.focused
        get() = get<String>(PARAM_FOCUSED)!!.toLong()

    override fun invoke(): String = error("Invoke without $PARAM_FOCUSED is not allowed!")

    /** Navigates to files of folder identified by [source] providing args*/
    operator fun invoke(focused: Long, key: String = ""): String =
        "$domain/${focused}/${Uri.encode(key)}"
}

interface MediaViewerViewState {

    val title: CharSequence
    var focused: Long
    val favourite: Boolean

    val data: List<MediaFile>

    val details: MediaFile?
    var showDetails: Boolean



    /**
     * A list of available [Action]s for the currently displayed file or item.
     */
    val actions: List<Action>

    /**
     * Called when an action is selected.
     *
     * @param item The [Action] representing the selected action.
     * @param activity The [Activity] context in which the action is invoked.
     */
    fun onAction(item: Action, activity: Activity)
}