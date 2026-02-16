/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 11-07-2024.
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

package com.zs.core.store

import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore

/**
 * Represents a media file.
 *
 * @property id A unique identifier for the media file.
 * @property name The name of the media file, typically including its file extension.
 * @property mimeType The MIME type of the media file, specifying its format (e.g., "image/jpeg").
 * @property path The full path to the media file.
 * @property dateAdded The date and time when the media file was added to the system,
 *           represented as a timestamp in milliseconds.
 * @property dateModified The date and time when the media file was last modified,
 *           represented as a timestamp in milliseconds.
 * @property size The size of the media file in bytes.
 * @property dateTaken The date and time when the media was captured (applicable to photos and videos),
 *           represented as a timestamp in milliseconds.
 * @property orientation The orientation of the media file, indicating how it should be displayed.
 * @property height The height of the media file in pixels (applicable to images and videos).
 * @property width The width of the media file in pixels (applicable to images and videos).
 * @property duration The duration of the media file in milliseconds (applicable to audio and videos).
 * @property expires The expiration date of the media file, represented as a timestamp in milliseconds. if -1 then no expiration date is set
 */
data class MediaFile(
    val id: Long,
    val name: String,
    val mimeType: String,
    val path: String,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val dateTaken: Long,
    val orientation: Int,
    val height: Int,
    val width: Int,
    val duration: Int,
    val expires: Long =  -1
) {
    val isImage get() = mimeType.startsWith("image/")
    val mediaUri
        get() = if (isImage)
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        else
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)


    internal constructor(cursor: Cursor) : this(
        id = cursor.getLong(0),
        name = cursor.getString(1) ?: "",
        dateAdded = cursor.getLong(2) * 1000,
        dateModified = cursor.getLong(3) * 1000,
        size = cursor.getLong(4),
        mimeType = cursor.getString(5) ?: "",
        orientation = cursor.getInt(6),
        height = cursor.getInt(7),
        width = cursor.getInt(8),
        path = cursor.getString(9),
        dateTaken = cursor.getLong(10) * 1000,
        duration = cursor.getInt(11),
        expires = if (cursor.columnCount == 13) (cursor.getLong(12) * 1000) else -1
    )
}