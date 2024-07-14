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

package com.zs.gallery.preview

import androidx.lifecycle.SavedStateHandle
import com.zs.gallery.common.SafeArgs

private const val PARAM_INDEX = "param_index"
private const val PARAM_IDS = "param_ids"

data class ViewerArgs(val index: Int, val ids: List<Long>)

object RouteViewer : SafeArgs<ViewerArgs> {

    operator fun invoke(index: Int, vararg ids: Long) = RouteViewer(ViewerArgs(index, ids.toList()))
    operator fun invoke(index: Int, ids: List<Long>) = RouteViewer(ViewerArgs(index, ids))
    override val route: String = "$domain/{${PARAM_INDEX}}/{${PARAM_IDS}}"

    /**
     * Generates a unique key for shared element transitions based on the given ID.
     * @param id ID to generate the key from.
     * @return The generated shared frame key.
     */
    fun buildSharedFrameKey(id: Long) =
        "shared_frame_$id"

    private const val DELIMITER = "_"
    override fun invoke(arg: ViewerArgs): String {
        return "$domain/${arg.index}/${arg.ids.joinToString(DELIMITER)}"
    }

    override fun get(handle: SavedStateHandle): ViewerArgs {
        val index = handle.get<String>(PARAM_INDEX)!!.toInt()
        val ids = handle.get<String>(PARAM_IDS)!!.split(DELIMITER).map { it.toLong() }
        return ViewerArgs(index, ids)
    }
}

interface ViewerViewState