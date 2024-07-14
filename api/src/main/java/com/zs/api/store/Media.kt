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
 * This sealed interface defines the common properties that can be associated with a media file.
 * Implementing classes can provide specific implementations based on the type of media file.
 *
 * @property id The ID of the media file.
 * @property name The name of the media file.
 * @property mimeType The MIME type of the media file.
 * @property parent The parent directory of the media file.
 * @property path The path of the media file.
 * @property dateAdded The date when the media file was added.
 * @property dateModified The date when the media file was last modified.
 * @property size The size of the media file in bytes.
 * @property dateTaken The date when the media was taken (applicable for photos).
 * @property orientation The orientation of the media file.
 * @property height The height of the media file (applicable for photos).
 * @property width The width of the media file (applicable for photos).
 */
sealed interface Media {
    val id: Long
    val name: String
    val mimeType: String
    val parent: String
    val path: String
    val dateAdded: Long
    val dateModified: Long
    val size: Int
    val dateTaken: Long
    val orientation: Int
    val height: Int
    val width: Int

    // TODO: Maybe include in future version the necessary permission.
    val latitude: Float get() = 0.0f
    val longitude: Float get() = 0.0f


    data class Image(
        override val id: Long,
        override val name: String,
        override val mimeType: String,
        override val parent: String,
        override val path: String,
        override val dateAdded: Long,
        override val dateModified: Long,
        override val size: Int,
        override val dateTaken: Long,
        override val orientation: Int,
        override val height: Int,
        override val width: Int
    ) : Media

    data class Video(
        override val id: Long,
        override val name: String,
        override val mimeType: String,
        override val parent: String,
        override val path: String,
        override val dateAdded: Long,
        override val dateModified: Long,
        override val size: Int,
        override val dateTaken: Long,
        override val orientation: Int,
        override val height: Int,
        override val width: Int
    ) : Media
}
