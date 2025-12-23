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

package com.zs.common.db

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.zs.common.db.albums.Albums
import com.zs.common.db.albums.MediaFile
import java.util.concurrent.TimeUnit
import android.provider.MediaStore.Files.FileColumns as FC
import androidx.work.Constraints.Builder as Constraints
import androidx.work.WorkerParameters as Params

/**
 * A [CoroutineWorker] that synchronizes the local database with the Android MediaStore.
 *
 * Responsibilities:
 * - Detect and remove stale entries from the database that no longer exist in MediaStore.
 * - Insert or update new/modified media files (images/videos) into the database.
 * - Schedule itself to run again when MediaStore content changes.
 *
 * This worker ensures that the app's media database stays consistent with the device's MediaStore.
 */
class SyncWorker private constructor(val context: Context, params: Params) :
    CoroutineWorker(context, params) {
    //
    companion object {
        private const val TAG = "SyncWorker"
        /** Base URI for querying all files in external MediaStore storage. */
        private val URI = MediaStore.Files.getContentUri("external")

        // Core projection: the set of columns we always want to read from MediaStore.
        // These cover identity, metadata, and timestamps. Extra columns (DATE_EXPIRES, IS_TRASHED)
        // are conditionally added later depending on API level.
        private val _PROJECTION
            get() = arrayOf(
                FC.DATA, // 0
                FC.DISPLAY_NAME, // 1
                FC.MIME_TYPE, // 2
                FC.SIZE, // 3
                FC.DURATION, // 4
                FC.BITRATE, // 5
                FC.YEAR, // 6
                FC.WIDTH, // 7
                FC.HEIGHT, // 8
                FC.ORIENTATION, // 9
                FC.DATE_ADDED, // 10
                FC.DATE_MODIFIED, // 11
                FC.DATE_TAKEN, // 12
            )

        /** Projection adjusted for API level (adds DATE_EXPIRES, IS_TRASHED on Android 11+). */
        private val PROJECTION = when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> _PROJECTION
            else -> _PROJECTION + arrayOf(FC.DATE_EXPIRES, FC.IS_TRASHED)
        }

        /**
         * Run an immediate one-time synchronization.
         *
         * Typically invoked at app startup to ensure the database is initialized
         * with the latest MediaStore state.
         */
        fun execute(context: Context) {
            // run on app start up for first time
            val workManager = WorkManager.getInstance(context.applicationContext)
            val work = OneTimeWorkRequestBuilder<SyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            workManager.enqueueUniqueWork("immediate_sync", ExistingWorkPolicy.KEEP, work)
        }

        /**
         * Schedule a one-time sync job triggered by MediaStore content changes.
         *
         * Uses [Constraints] to listen for changes in Images and Videos URIs.
         * Ensures only the latest request is active by using [ExistingWorkPolicy.APPEND_OR_REPLACE].
         */
        internal fun schedule(context: Context) {
            // Define constraints for the worker
            val constraints = Constraints().apply {
                // Add the desired MediaStore URIs to observe
                addContentUriTrigger(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true)
                addContentUriTrigger(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true)
                setTriggerContentMaxDelay(5, TimeUnit.SECONDS)
                // Optional: throttle triggers (API 26+) //
            }
            // Build a one-time work request with the defined constraints
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints.build())
                //.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            // Enqueue the unique work request
            val instance = WorkManager.getInstance(context)
            // Use ExistingWorkPolicy.REPLACE to ensure only the latest request is pending
            instance.enqueueUniqueWork("sync_schedule", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
        }
    }

    override suspend fun doWork(): Result {
        schedule(context)  // Schedule next cycle immediately to keep sync continuous
        val db = Archive(context)                  // database wrapper
        val resolver = context.contentResolver      // MediaStore access
        val dao = db.albums                   // DAO for media table

        // --- Step 1: Detect missing files ---
        // Query MediaStore for all current file paths (DATA column only).
        // Delete any DB entries not present in MediaStore.
        val count = resolver.query2(
            URI,
            projection = arrayOf(FC.DATA),
            use = { c ->
                val paths = buildList {
                    while (c.moveToNext()) this += c.getString(0)
                }
                dao.deleteAllNotIn(paths) // bulk delete rows not in MediaStore
            }
        )
        Log.d(TAG, "Delete: $count")
        // --- Step 2: Compute modification threshold ---
        // Convert lastModified from millis → seconds and subtract 2s.
        // This offset ensures very recent changes are not missed.
        val lastModified = (dao.lastModified() / 1000 - 2)
        // --- Step 3: Fetch modified/new media ---
        resolver.query2(
            URI,
            selection = "${FC.MEDIA_TYPE} IN (${FC.MEDIA_TYPE_IMAGE}, ${FC.MEDIA_TYPE_VIDEO}) AND ${FC.DATE_MODIFIED} >= $lastModified",
            projection = PROJECTION,
            use = { cursor ->
                Log.d(TAG, "Changed: ${cursor.count}")
                db.withTransaction {
                    // Iterate through each row and upsert into DB.
                    val isAtLeast11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    while (cursor.moveToNext()) {
                        val data = cursor.getString(0)
                        val existing = dao.get(data) // get existing or null mediaFile.
                        // Construct new MediaFile object (reuse DB id if present).
                        val newFile = MediaFile(
                            id = existing?.id ?: 0, // reuse DB id if present
                            data = data,
                            name = cursor.getString(1),
                            mimeType = cursor.getString(2),
                            size = cursor.getLong(3),
                            duration = cursor.getLong(4),
                            bitrate = cursor.getInt(5),
                            year = cursor.getInt(6),
                            width = cursor.getInt(7),
                            height = cursor.getInt(8),
                            orientation = cursor.getInt(9),
                            dateAdded = cursor.getLong(10) * 1000,    // convert seconds → millis
                            dateModified = cursor.getLong(11) * 1000, // convert seconds → millis
                            dateTaken = cursor.getLong(12) * 1000,    // convert seconds → millis
                            dateExpires = if (isAtLeast11) cursor.getLong(13) * 1000 else -1,
                            flags = let{
                                // Preserve existing flags, then set/clear the trash bit.
                                val newFlags = existing?.flags ?: Albums.FLAG_NONE
                                val isTrashed = if (isAtLeast11) cursor.getInt(14) != 0 else false
                                when {
                                    isTrashed -> newFlags or Albums.FLAG_IS_TRASHED
                                    else -> newFlags and Albums.FLAG_IS_TRASHED.inv()
                                }
                            }
                        )

                        Log.d(TAG, "${if (existing == null) "New" else "Update"}: {${newFile.data}}")
                        // Upsert: insert if new, update if existing.
                        if (existing == null) dao.insert(newFile) else dao.update(newFile)
                    }
                }
            }
        )
        return Result.success()
    }
}