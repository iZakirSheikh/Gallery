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

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Update
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.zs.common.db.RoomDb
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
        fun initialize(context: Context) = RoomDb.initialize(context)
        fun getInstance() = RoomDb.getInstance().mediaProvider

        /**
         * Run an immediate one-time synchronization.
         *
         * Typically invoked at app startup to ensure the database is initialized
         * with the latest MediaStore state.
         */
        fun runImmediateSync(context: Context) {
            // run on app start up for first time
            val workManager = WorkManager.getInstance(context.applicationContext)
            val work = OneTimeWorkRequestBuilder<MediaSyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            workManager.enqueueUniqueWork("immediate_sync", ExistingWorkPolicy.KEEP, work)
        }
    }

    /* For maintain media database.*/
    @Insert
    internal abstract suspend fun insert(file: MediaFile): Long

    @Update
    internal abstract suspend fun update(file: MediaFile): Int

    @Query("SELECT * FROM tbl_media WHERE store_id = :id LIMIT 1")
    internal abstract suspend fun getMediaFile(id: Long): MediaFile?

    @Query(" SELECT COALESCE(MAX(date_modified), 0) FROM tbl_media ORDER BY date_modified DESC LIMIT 1")
    internal abstract suspend fun lastModified(): Long

    @Query("SELECT COUNT(*) FROM tbl_media")
    internal abstract suspend fun count(): Int

    @RawQuery
    internal abstract suspend fun rawQuery(query: RoomRawQuery): Int
    internal suspend fun deleteByStoreIdNotIn(ids: String) {
        // Deletes rows from tbl_media where store_id is NOT in the provided ids.
        //
        // Why this approach?
        // - SQLite has a hard limit of 999 bind parameters in an IN/NOT IN clause.
        //   If we try to pass a List<Long> with thousands of IDs (e.g., from MediaStore),
        //   the query will exceed this limit and fail.
        // - On modern phones, users may have 30,000+ photos. Passing all of those IDs
        //   directly into a query would create a massive SQL statement, which is both slow
        //   and error-prone.
        // - By interpolating a prebuilt comma-separated string of IDs, we avoid Room’s
        //   parameter expansion limit. This lets us handle larger sets of IDs.
        //
        // ⚠️ Caveats:
        // - The ids string must be properly formatted (e.g., (1,2,3)).
        // - This method does not return the number of rows deleted. If you need that,
        //   prefer a count() fun.
        rawQuery(RoomRawQuery("DELETE FROM tbl_media WHERE store_id NOT IN ($ids)"))
    }

    @RawQuery(observedEntities = [MediaFile::class])
    internal abstract fun rawSnapshot(query: RoomRawQuery): PagingSource<Int, Snapshot>

    fun snapshots(path: String? = null): PagingSource<Int, Snapshot> {
        //language=Room Sql
        val query = """
                    SELECT 
                        id,
                        -1 AS `order`,
                        extras      AS rawExtras,
                        timeline    AS rawTimeline,
                        resolution  AS rawResolution,
                        store_id    AS mediaId,   -- prefer store_id if available
                        mime_type   AS mimeType,
                        CASE
                            -- First row in the result set → always emit header
                            WHEN LAG(date_modified, 1, -1) OVER (ORDER BY date_modified DESC) = -1
                            THEN date_modified
                
                            -- Last row in the result set → never emit header
                            WHEN LEAD(date_modified, 1, -1) OVER (ORDER BY date_modified DESC) = -1
                            THEN NULL
                
                            -- Emit header when calendar day changes compared to previous row
                            -- Using SQLite date() function to normalize timestamp to local day
                            WHEN LAG(
                                date(date_modified / 1000, 'unixepoch', 'localtime')
                            ) OVER (ORDER BY date_modified DESC)
                            != date(date_modified / 1000, 'unixepoch', 'localtime')
                            THEN date_modified
                
                            -- Otherwise → no header
                            ELSE NULL
                        END AS header
                    FROM tbl_media
                    WHERE (extras & $FLAG_TRASHED)  = 0
                      AND (extras & $FLAG_ARCHIVED) = 0
                      AND (extras & $FLAG_PRIVATE)  = 0
                    ORDER BY date_modified DESC
                  """.trimIndent()
        return rawSnapshot(RoomRawQuery(query))
    }

    fun snapshots(albumID: Long): PagingSource<Int, Snapshot> { TODO() }
    fun favourites(): PagingSource<Int, Snapshot> { TODO() }

    fun privates(): PagingSource<Int, Snapshot> { TODO() }
    fun archives(): PagingSource<Int, Snapshot> { TODO() }
    fun albums(): PagingSource<Int, Album> { TODO() }

    fun folders(): PagingSource<Int, Folder> { TODO() }
}