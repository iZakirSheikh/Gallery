/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 19-07-2024.
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

package com.zs.gallery.common

import android.app.Activity
import com.zs.api.store.MediaFile


/**
 * An interface that together works with [SelectionTracker] to do operations on [MediaFile] of MediaProvider.
 */
interface FileActions {

    /**
     * Deletes the currently [selected] files.
     * @param activity  the current activity, used for context.
     * @param trash  whether to move files to trash (if supported) or permanently delete them.
     */
    fun delete(activity: Activity, trash: Boolean = true)

    /**
     * Moves the currently [selected] files to the specified folder.
     * @param dest  the destination folder path.
     */
    fun move(dest: String)

    /**
     * Copies the given [file]s to the specified folder.
     * @param file  the media files to copy.
     * @param dest  the destination folder path.
     */
    fun copy(dest: String)

    /**
     * Renames the current select file to the new [name].
     * @param name  the new name for the file.
     * @throws [IllegalStateException] if selected files are more than 1.
     */
    fun rename(name: String)

    /**
     * Shares the currently selected files.
     * @param activity  the current activity, used for context.
     */
    fun share(activity: Activity)

    /**
     * Restores the  currently selected files from trash.
     */
    fun restore(activity: Activity)
}