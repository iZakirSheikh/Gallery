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
 * This model provides lightweight metadata about a directory or album, optimized
 * for display and aggregation rather than full media detail.
 *
 * @property thumbnail Optional file path or URI of a representative thumbnail for the directory.
 * @property key Unique key identifying the directory or album (e.g., folder path or album ID).
 * @property count Total number of media items contained in the directory.
 * @property size Combined size of all media items in bytes.
 * @property dateModified Timestamp (ms since epoch) when the directory or album was last modified.
 */
class Directory(
    @JvmField val thumbnail: String?,
    @JvmField val key: String,
    @JvmField val count: Int,
    @JvmField val size: Long, // size in bytes
    @JvmField val dateModified: Long,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Directory

        if (count != other.count) return false
        if (size != other.size) return false
        if (dateModified != other.dateModified) return false
        if (thumbnail != other.thumbnail) return false
        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + size.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + key.hashCode()
        return result
    }

    override fun toString(): String {
        return "Directory(thumbnail=$thumbnail, key='$key', count=$count, size=$size, dateModified=$dateModified)"
    }
}