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

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.zs.common.db.AppDb
import com.zs.common.db.MediaSyncWorker

@Dao
abstract class MediaProvider {
    //
    companion object {
        // ── Boolean flags ───────────────────────────────
        internal const val DEFAULT_EXTRAS: Int = 0
        internal const val FLAG_PRIVATE: Int = 1 shl 0   // 0001
        internal const val FLAG_TRASHED: Int = 1 shl 1   // 0010
        internal const val FLAG_LIKED: Int = 1 shl 2   // 0100
        internal const val FLAG_ARCHIVED: Int = 1 shl 3   // 0100

        //
        internal const val MASK_ORIENTATION: Int = 0b1111 shl 4

        // Bit layout: xxxx (4 bits)
        const val ORIENTATION_UNDEFINED = 0b0000
        const val ORIENTATION_NORMAL = 0b0001
        const val ORIENTATION_FLIP_HORIZONTAL = 0b0010
        const val ORIENTATION_ROTATE_180 = 0b0011
        const val ORIENTATION_FLIP_VERTICAL = 0b0100
        const val ORIENTATION_TRANSPOSE = 0b0101
        const val ORIENTATION_ROTATE_90 = 0b0110
        const val ORIENTATION_TRANSVERSE = 0b0111
        const val ORIENTATION_ROTATE_270 = 0b1000

        /**
         * @see AppDb.initialize
         */
        fun initialize(context: Context) = AppDb.initialize(context)
        fun getInstance() = AppDb.getInstance().mediaProvider

        /**
         * Run an immediate one-time synchronization.
         *
         * Typically invoked at app startup to ensure the database is initialized
         * with the latest MediaStore state.
         */
        fun runImmediateSync(context: Context){
            // run on app start up for first time
            val workManager = WorkManager.getInstance(context.applicationContext)
            val work = OneTimeWorkRequestBuilder<MediaSyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            workManager.enqueueUniqueWork("immediate_sync", ExistingWorkPolicy.KEEP, work)
        }
    }

    @RawQuery
    internal abstract suspend fun _deleteAllNotIn(query: SupportSQLiteQuery): Int

    /* For maintain media database.*/
    @Insert
    internal abstract suspend fun insert(file: MediaFile): Long

    @Update
    internal abstract suspend fun update(file: MediaFile): Int

    @Query("SELECT * FROM tbl_media WHERE store_id = :id LIMIT 1")
    internal abstract suspend fun getMediaFile(id: Long): MediaFile?

    @Query(" SELECT COALESCE(MAX(date_modified), 0) FROM tbl_media ORDER BY date_modified DESC LIMIT 1")
    internal abstract suspend fun lastModified(): Long

    /**
     * deletes all files not in [ids] from [MediaFile]
     */
    internal suspend fun deleteNotIn(ids: String): Int {
        return _deleteAllNotIn(SimpleSQLiteQuery("DELETE FROM tbl_media WHERE store_id NOT IN ($ids)"))
    }
}