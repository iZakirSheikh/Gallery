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


import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    value = """
        SELECT
            tbl_media.source AS poster,
            SUBSTR(source, 1, LENGTH(RTRIM(source, REPLACE(source, '/', ''))) - 1 ) AS folder_path,
            COUNT(*) AS count,
            MAX(date_modified) AS dateModified,
            SUM(size) AS size
        FROM tbl_media
        GROUP BY folder_path
        ORDER BY dateModified DESC
    """,
    viewName = "vw_folder"
)
class Folder(
    @JvmField val poster: String?,
    @JvmField @ColumnInfo(name = "folder_path") val path: String,
    @JvmField val count: Int,
    @JvmField val size: Long, // size in bytes
    @JvmField val dateModified: Long,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Folder

        if (count != other.count) return false
        if (size != other.size) return false
        if (dateModified != other.dateModified) return false
        if (poster != other.poster) return false
        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + size.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + (poster?.hashCode() ?: 0)
        result = 31 * result + path.hashCode()
        return result
    }

    override fun toString(): String {
        return "Folder(poster=$poster, path='$path', count=$count, bytes=$size, lastModified=$dateModified)"
    }
}