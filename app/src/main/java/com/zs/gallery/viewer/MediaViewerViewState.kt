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
import android.os.Bundle
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import com.zs.core.store.MediaFile
import com.zs.gallery.common.Action
import com.zs.gallery.common.Route
import com.zs.gallery.files.RouteFiles

private const val PARAM_FOCUSED = "param_focused"
private const val PARAM_ARG = "param_arg"
private const val PARAM_SOURCE = "param_source"

object RouteViewer : Route {
    override val route: String = "$domain/{${PARAM_SOURCE}}/{${PARAM_FOCUSED}}/{${PARAM_ARG}}"
    override fun invoke(): String = error("Invoke without $PARAM_FOCUSED is not allowed!")

    /** Navigates to files of folder identified by [source] providing args*/
    operator fun invoke(source: String, focused: Long, key: String = ""): String =
        "$domain/${source}/${focused}/${Uri.encode(key)}"
}

// Define the Route for this screen.
object RouteIntentViewer : Route {

    const val PARAM_URI = "param_uri"
    const val PARAM_MIME = "param_mime"

    override val route = "$domain/{${PARAM_URI}}/{${PARAM_MIME}}"

    operator fun invoke(uri: Uri, mime: String) =
        "$domain/${Uri.encode(uri.toString())}/${Uri.encode(mime)}"

    fun buildArgs(bundle: Bundle): Pair<Uri, String> {
        val uri = bundle.getString(PARAM_URI)!!.toUri()
        val mime = bundle.getString(PARAM_MIME)
        return uri to (mime ?: "image/*")
    }
}


operator fun SavedStateHandle.get(route: RouteViewer) =
    Triple(
        get<String>(PARAM_SOURCE).takeIf { !it.isNullOrEmpty() } ?: RouteFiles.SOURCE_TIMELINE,
        (get<String>(PARAM_FOCUSED)?.toLongOrNull() ?: -1L),
        get<String>(PARAM_ARG).takeIf { !it.isNullOrEmpty() }
    )

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
     * @param value The [Action] representing the selected action.
     * @param activity The [Activity] context in which the action is invoked.
     */
    fun onPerformAction(value: Action, resolver: Activity)
}