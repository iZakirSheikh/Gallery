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


import com.zs.common.db.media.MediaFile.Extras
import com.zs.common.db.media.MediaFile.Resolution
import com.zs.common.db.media.MediaFile.Timeline

/**
 * Represents a lightweight, read-only snapshot of a media item optimized for list and grid rendering.
 *
 * This model is a **projection**, not a full entity. It is typically backed by
 * a Room query that selects only the columns required for fast scrolling and preview
 * rendering. Several logical attributes are **bit-packed into `LONG` values** and
 * exposed through derived properties.
 *
 * @property id Stable identifier of the media item.
 * @property thumbnail File path or URI of the thumbnail image.
 *
 * @property order Playback order of the item within its album, or `-1` if unavailable.
 *
 * @property header Column value used for grouping. Non-null for the first item in a group,
 *                  null for subsequent items.
 * @property isImage Indicates whether this snapshot represents a still image, video, or animated file.
 *
 * @property resolution Decoded [MediaFile.Resolution] derived from [rawResolution].
 * @property timeline Decoded [MediaFile.Timeline] derived from [rawTimeline].
 * @property extras Decoded [MediaFile.Extras] derived from [rawExtras].
 */
class Snapshot(
    @JvmField val id: Long,
    @JvmField val thumbnail: String,
    @JvmField val order: Int,
    @JvmField var header: String?,
    @JvmField val isImage: Boolean,
    @JvmField internal val rawResolution: Long,
    @JvmField internal val rawTimeline: Long,
    @JvmField internal val rawExtras: Int,
) {
    val resolution get() = Resolution(rawResolution)
    val timeline get() = Timeline(rawTimeline)
    val extras get() = Extras(rawExtras)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Snapshot

        if (id != other.id) return false
        if (order != other.order) return false
        if (isImage != other.isImage) return false
        if (rawResolution != other.rawResolution) return false
        if (rawTimeline != other.rawTimeline) return false
        if (rawExtras != other.rawExtras) return false
        if (thumbnail != other.thumbnail) return false
        if (header != other.header) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + order
        result = 31 * result + isImage.hashCode()
        result = 31 * result + rawResolution.hashCode()
        result = 31 * result + rawTimeline.hashCode()
        result = 31 * result + rawExtras
        result = 31 * result + thumbnail.hashCode()
        result = 31 * result + (header?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Snapshot(id=$id, thumbnail='$thumbnail', order=$order, header=$header, isImage=$isImage, resolution=$resolution, timeline=$timeline, extras=$extras)"
    }
}