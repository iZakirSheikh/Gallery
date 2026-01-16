/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
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
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

package com.zs.common.db.media


/**
 * A lightweight, read-only snapshot of a media item optimized for fast list and grid rendering.
 *
 * This class is a **projection**, not a full entity. It is typically backed by a Room query
 * that selects only the minimal set of columns required for smooth scrolling and preview display.
 * Several attributes are **bit-packed into `Long` values** and exposed through derived properties.
 *
 * @property id Stable identifier of the media item.
 * @property mediaId Actual media identifier of the file. If the file is available locally,
 *                   this is the store-ID (safe to convert to `Long`); otherwise, it represents
 *                   a remote cloud source ID.
 * @property order Playback order within the album, or `-1` if unavailable.
 * @property header Grouping key. Non-null for the first item in a group, null for subsequent items.
 * @property mimeType MIME type of the media, if available.
 * @property resolution Decoded [MediaFile.Resolution] derived from [rawResolution].
 * @property timeline Decoded [MediaFile.Timeline] derived from [rawTimeline].
 * @property extras Decoded [MediaFile.Extras] derived from [rawExtras].
 */
class Snapshot(
    @JvmField val id: Long,
    @JvmField val mediaId: String,
    @JvmField val order: Int,
    @JvmField var header: String?,
    @JvmField val mimeType: String?,
    @JvmField internal val rawResolution: Long,
    @JvmField internal val rawTimeline: Long,
    @JvmField internal val rawExtras: Int
){

    val resolution get() = MediaFile.Resolution(rawResolution)
    val timeline get() = MediaFile.Timeline(rawTimeline)
    val extras get() = MediaFile.Extras(rawExtras)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Snapshot

        if (id != other.id) return false
        if (order != other.order) return false
        if (rawResolution != other.rawResolution) return false
        if (rawTimeline != other.rawTimeline) return false
        if (rawExtras != other.rawExtras) return false
        if (mediaId != other.mediaId) return false
        if (header != other.header) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + order
        result = 31 * result + rawResolution.hashCode()
        result = 31 * result + rawTimeline.hashCode()
        result = 31 * result + rawExtras.hashCode()
        result = 31 * result + mediaId.hashCode()
        result = 31 * result + (header?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Snapshot(id=$id, mediaId='$mediaId', order=$order, header=$header, mimeType=$mimeType, rawResolution=$rawResolution, rawTimeline=$rawTimeline, rawExtras=$rawExtras)"
    }
}