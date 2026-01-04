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

package com.zs.common.db.album


import com.zs.common.db.album.MediaFile.Extras
import com.zs.common.db.album.MediaFile.Resolution
import com.zs.common.db.album.MediaFile.Timeline

/**
 * Lightweight, read-only snapshot of a media item optimized for list and grid rendering.
 *
 * This model represents a **projection**, not a full entity. It is typically backed by
 * a Room query that selects only the columns required for fast scrolling and preview
 * rendering. Several logical attributes are **packed into `LONG` values** and exposed
 * here via derived properties.
 *
 * @property id Stable identifier of the media item.
 * @property poster File path or URI of the thumbnail, or `null` if unavailable.
 *
 * @property order Play order of the item **within its album**.
 * This value defines playback sequence and is independent of UI sort order.
 *
 * @property section Timestamp used for temporal grouping (e.g. day/month headers).
 * A value of `-1` indicates that this item is not a section header.
 *
 * @property rawResolution Packed width/height stored as a single `LONG`.
 * Prefer using [resolution] for decoded access.
 *
 * @property rawTimeline Packed temporal metadata used for grouping and sorting.
 * Prefer using [timeline] for decoded access.
 *
 * @property rawExtras Packed flags and small attributes stored as a single `LONG`
 * (e.g. privacy, archive state, liked state, orientation).
 *
 * @property resolution Decoded [MediaFile.Resolution] derived from [rawResolution].
 * @property timeline Decoded [MediaFile.Timeline] derived from [rawTimeline].
 */
class Snapshot(
    @JvmField val id: Long,
    @JvmField val poster: String?,
    @JvmField val order: Int,
    @JvmField val section: Long,
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
        if (section != other.section) return false
        if (rawResolution != other.rawResolution) return false
        if (rawTimeline != other.rawTimeline) return false
        if (rawExtras != other.rawExtras) return false
        if (poster != other.poster) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + order
        result = 31 * result + section.hashCode()
        result = 31 * result + rawResolution.hashCode()
        result = 31 * result + rawTimeline.hashCode()
        result = 31 * result + rawExtras
        result = 31 * result + (poster?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Snapshot(id=$id, poster=$poster, order=$order, section=$section, resolution=$resolution, timeline=$timeline, extras=$extras)"
    }
}