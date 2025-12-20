/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 20 of Dec 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 20 of Dec 2025
 */

package com.zs.common.db

import android.content.Context
import android.provider.MediaStore
import androidx.work.Worker
import androidx.work.WorkerParameters as Params

class MediaSyncWorker(context: Context, params: Params): Worker(context, params) {

    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.ORIENTATION,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.BITRATE,
        MediaStore.MediaColumns.DATE_TAKEN,
    )



    override fun doWork(): Result {
        val resolver =

        TODO("Not yet implemented")
    }
}