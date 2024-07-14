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

package com.zs.api.store

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
)

val MediaFile.isImage get() = mimeType.startsWith("image/")
val MediaFile.mediaUri
    get() = MediaProvider.contentUri(id)