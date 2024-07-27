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
import android.os.Build
import androidx.annotation.RequiresApi
import com.zs.api.store.MediaFile


/**
 * An interface for performing actions on [MediaFile] items in conjunction with a [SelectionTracker].
 */
interface FileActions {

    /**
     * permanently `deletes` the currently `selected` files.
     * @param activity The current activity, used for context.
     */
    fun delete(activity: Activity)

    /**
     * Deletes or trashes the currently `selected` files, prioritizing trashing if available.
     *
     * This method behaves similarly to [delete] but prioritizes moving files to the trash/recycle
     * bin if it's enabled and supported on the current Android version (R+).
     *
     * @param activity The current activity, used for context.
     */
    fun remove(activity: Activity)

    /**
     * Moves the currently `selected` files to the trash/recycle bin.
     *
     * ***Note - This method is only available on Android versions R and above.***
     *
     * @param activity The current activity, used for context.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun trash(activity: Activity)

    /**
     * Moves the currently `selected` files to the specified folder.
     *
     * @param dest The destination folder path.
     */
    fun move(dest: String)

    /**
     * Copies the currently `selected` files to the specified folder.
     *
     * @param dest The destination folder path.
     */
    fun copy(dest: String)

    /**
     * Renames the currently selected file to the new [name].
     *
     * @param name The new name for the file.
     * @throws IllegalStateException if more than one file is selected.
     */
    fun rename(name: String)

    /**
     * Shares the currently selected files.
     *
     * @param activity The current activity, used for context.
     */
    fun share(activity: Activity)

    /**
     * Restores the currently selected files from the trash/recycle bin.
     *
     * ***Note - This method is only available on Android versions R and above.***
     *
     * @param activity The current activity, used for context.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun restore(activity: Activity)
}