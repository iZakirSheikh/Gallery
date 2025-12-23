/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 23 of Dec 2025
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
 * Last Modified by sheik on 23 of Dec 2025
 */

package com.zs.common.db.albums

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zs.common.db.Archive

@Dao
abstract class Albums {

    companion object {
        const val FLAG_NONE = 0
        const val FLAG_IS_PRIVATE = 1 shl 0  // 0001
        const val FLAG_IS_TRASHED = 1 shl 1  // 0010
        const val FLAG_IS_FAVOURITE = 1 shl 2  // 0100

        operator fun invoke(context: Context) = Archive(context).albums
    }

    /* For maintain media database.*/
    @Insert
    internal abstract suspend fun insert(file: MediaFile): Long

    @Update
    internal  abstract suspend fun update(file: MediaFile): Int

    @Query("DELETE FROM tbl_media WHERE data NOT IN (:data)")
    internal abstract suspend fun deleteAllNotIn(data: List<String>): Int

    @Query("SELECT * FROM tbl_media WHERE data = :data LIMIT 1")
    internal abstract suspend fun get(data: String): MediaFile?

    @Query(" SELECT COALESCE(MAX(date_modified), 0) FROM tbl_media ORDER BY date_modified DESC LIMIT 1")
    internal abstract suspend fun lastModified(): Long
}