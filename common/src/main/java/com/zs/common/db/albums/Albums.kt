/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 20 of Dec 2025
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
 * Last Modified by sheik on 20 of Dec 2025
 */

package com.zs.common.db.albums

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

abstract class Albums {

    /**
     * Bit flags representing the state of a media file.
     *
     * Flags can be combined using bitwise operations to represent multiple
     * states at once and are stored as a single integer column in the database.
     *
     * @property FLAG_NONE No flags set.
     * @property FLAG_IS_PRIVATE Media is marked as private and should be hidden from public views.
     * @property FLAG_IS_TRASHED Media is marked as trashed.
     * @property FLAG_IS_FAVOURITE Media is marked as a favourite.
     * @property FLAG_IS_PENDING Media is pending (still loading or processing).
     */
    companion object {
        const val FLAG_NONE = 0
        const val FLAG_IS_PRIVATE   = 1 shl 0  // 0001
        const val FLAG_IS_TRASHED   = 1 shl 1  // 0010
        const val FLAG_IS_FAVOURITE = 1 shl 2  // 0100
        const val FLAG_IS_PENDING   = 1 shl 3  // 1000
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(vararg file: MediaFile): LongArray

    @Query("DELETE FROM tbl_media WHERE id IN (:id)")
    abstract suspend fun delete(vararg id: Long): Int

    @Update
    abstract suspend fun update(vararg file: MediaFile): Int

    @Query(" SELECT COALESCE(MAX(date_modified), 0) FROM tbl_media")
    internal abstract suspend fun lastModified(): Long



}