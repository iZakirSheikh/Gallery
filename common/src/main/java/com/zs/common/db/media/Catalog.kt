/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 7 of Jan 2026
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
 * Last Modified by sheik on 7 of Jan 2026
 *
 */

package com.zs.common.db.media

/**
 * Represents a collection of [MediaFile] items grouped either by their parent folder
 * (via [MediaFile.data]) or by an [Album].
 *
 * This model provides lightweight metadata about a directory, album, or other grouping
 * of media files. It is intended for quick access to summary information rather than
 * detailed file inspection.
 *
 * @property name Display name of the catalog (e.g., folder name or album title).
 * @property key Unique identifier for the catalog (e.g., folder path or album ID).
 * @property cover Optional file path or URI pointing to a representative thumbnail image.
 * @property count Total number of media items contained in the catalog.
 * @property size Combined size of all media items, in bytes.
 * @property dateModified Timestamp (milliseconds since epoch) of the last modification
 *                        to the directory or album.
 */
class Catalog(
    @JvmField val name: String,
    @JvmField val key: String,
    @JvmField val cover: String?,
    @JvmField val count: Int,
    @JvmField val size: Long, // size in bytes
    @JvmField val dateModified: Long,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Catalog

        if (count != other.count) return false
        if (size != other.size) return false
        if (dateModified != other.dateModified) return false
        if (name != other.name) return false
        if (key != other.key) return false
        if (cover != other.cover) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + size.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + (cover?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Catalog(name='$name', key='$key', cover=$cover, count=$count, size=$size, dateModified=$dateModified)"
    }
}