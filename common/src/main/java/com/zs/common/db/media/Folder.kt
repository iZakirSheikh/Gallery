/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 12 of Jan 2026
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
 * Last Modified by sheik on 12 of Jan 2026
 */

package com.zs.common.db.media

/**
 * Represents a collection of [MediaFile] items grouped by their parent folder.
 *
 * @property name Display name of the Folder.
 * @property path Absolute path of the folder.
 * @property thumbnail Store ID of the representative cover item.
 * @property count Total number of media items contained in the Folder.
 * @property size Combined size of all media items, in bytes.
 * @property lastModifiedMs Last modification timestamp of the directory expressed in milliseconds
 *                        since epoch.
 */
class Folder(
    @JvmField val name: String,
    @JvmField val path: String,
    @JvmField val thumbnail: Long,
    @JvmField val count: Int,
    @JvmField val size: Long,
    @JvmField val lastModifiedMs: Long,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Folder

        if (thumbnail != other.thumbnail) return false
        if (count != other.count) return false
        if (size != other.size) return false
        if (lastModifiedMs != other.lastModifiedMs) return false
        if (name != other.name) return false
        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbnail.hashCode()
        result = 31 * result + count
        result = 31 * result + size.hashCode()
        result = 31 * result + lastModifiedMs.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }

    override fun toString(): String {
        return "Folder(displayName='$name', path='$path', coverMediaID=$thumbnail, itemCount=$count, totalSizeInBytes=$size, lastModifiedMills=$lastModifiedMs)"
    }
}