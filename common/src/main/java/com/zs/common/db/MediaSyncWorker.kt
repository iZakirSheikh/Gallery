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

package com.zs.common.db

import android.content.Context
import android.media.ExifInterface
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zs.common.db.media.MediaFile
import com.zs.common.db.media.MediaFile.Timeline
import com.zs.common.db.media.MediaProvider
import com.zs.common.util.query2
import java.util.concurrent.TimeUnit
import android.provider.MediaStore.Files.FileColumns as Column
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
internal class MediaSyncWorker private constructor(
    val context: Context,
    params: Params
) : CoroutineWorker(context, params) {

    private val TAG = "MediaSyncWorker"

    /** Base URI for querying all files in external MediaStore storage. */
    private val URI = MediaStore.Files.getContentUri("external")

    // Projection of media columns for queries
    private val MEDIA_QUERY_PROJECTION = buildList {
        // 🔑 Identity / path
        this += Column._ID           // 0: unique row ID
        this += Column.DATA          // 1: absolute file path
        this += Column.DISPLAY_NAME  // 2: human-readable file name
        this += Column.MIME_TYPE     // 3: content type (e.g., video/mp4)

        // 📂 File attributes
        this += Column.SIZE          // 4: file size in bytes

        // 🎬 Media-specific properties
        this += Column.DURATION      // 5: duration in ms (audio/video)
        this += Column.BITRATE       // 6: encoding bitrate
        this += Column.YEAR          // 7: year tag (if available)
        this += Column.WIDTH         // 8: pixel width
        this += Column.HEIGHT        // 9: pixel height
        this += Column.ORIENTATION   // 10: rotation/orientation metadata

        // ⏱ Temporal metadata
        this += Column.DATE_ADDED    // 11: when added to MediaStore
        this += Column.DATE_MODIFIED // 12: last modified timestamp
        this += Column.DATE_TAKEN    // 13: original capture timestamp

        // 🗑 Lifecycle (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this += Column.DATE_EXPIRES // 14: expiry date (e.g., auto-deletion)
            this += Column.IS_TRASHED   // 15: whether item is in trash
        }
    }.toTypedArray()

    /**
     * Schedule a one-time sync job triggered by MediaStore content changes.
     *
     * Uses [Constraints] to listen for changes in Images and Videos URIs.
     * Ensures only the latest request is active by using [ExistingWorkPolicy.APPEND_OR_REPLACE].
     */
    private fun schedule(context: Context) {
        // Define constraints for the worker
        val constraints = Constraints().apply {
            // Add the desired MediaStore URIs to observe
            addContentUriTrigger(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true)
            addContentUriTrigger(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true)
            setTriggerContentMaxDelay(5, TimeUnit.SECONDS)
            // Optional: throttle triggers (API 26+) //
        }
        // Build a one-time work request with the defined constraints
        val request = OneTimeWorkRequestBuilder<MediaSyncWorker>()
            .setConstraints(constraints.build())
            //.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        // Enqueue the unique work request
        val instance = WorkManager.getInstance(context)
        // Use ExistingWorkPolicy.REPLACE to ensure only the latest request is pending
        instance.enqueueUniqueWork("sync_schedule", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    /**
     * Maps a native orientation value (video rotation degrees or EXIF orientation)
     * to the app-specific [Library] orientation constants.
     *
     * Video sources report rotation as degrees, while images use EXIF orientation
     * codes. This function normalizes both into a single internal representation.
     */
    private fun mapNativeToAppOrientation(native: Int, isVideo: Boolean): Int = when {
        // ── Video rotation (degrees → app orientation) ──────────
        isVideo && native == 90 -> MediaProvider.ORIENTATION_ROTATE_90
        isVideo && native == 180 -> MediaProvider.ORIENTATION_ROTATE_180
        isVideo && native == 270 -> MediaProvider.ORIENTATION_ROTATE_270
        isVideo -> MediaProvider.ORIENTATION_NORMAL
        // ── Image EXIF orientation (EXIF → app orientation) ─────
        native == ExifInterface.ORIENTATION_UNDEFINED -> MediaProvider.ORIENTATION_UNDEFINED
        native == ExifInterface.ORIENTATION_NORMAL -> MediaProvider.ORIENTATION_NORMAL
        native == ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> MediaProvider.ORIENTATION_FLIP_HORIZONTAL
        native == ExifInterface.ORIENTATION_FLIP_VERTICAL -> MediaProvider.ORIENTATION_FLIP_VERTICAL
        native == ExifInterface.ORIENTATION_TRANSPOSE -> MediaProvider.ORIENTATION_TRANSPOSE
        native == ExifInterface.ORIENTATION_ROTATE_90 -> MediaProvider.ORIENTATION_ROTATE_90
        native == ExifInterface.ORIENTATION_TRANSVERSE -> MediaProvider.ORIENTATION_TRANSVERSE
        native == ExifInterface.ORIENTATION_ROTATE_270 -> MediaProvider.ORIENTATION_ROTATE_270
        // ── Safe fallback ──────────────────────────────────────
        else -> MediaProvider.ORIENTATION_NORMAL
    }

    // Synchronization workflow
    override suspend fun doWork(): Result {
        // Schedule next cycle immediately to keep sync continuous
        schedule(context)
        val db = RoomDb.getInstance()                // database wrapper
        val resolver = context.contentResolver      // MediaStore access
        val dao = db.mediaProvider                   // DAO for media table
        // --- Step 1: Detect and clean up missing files ---
        // Goal: Ensure local DB stays in sync with MediaStore.
        // Strategy:
        //   1. Query MediaStore for all current file IDs.
        //   2. Compare against DB entries.
        //   3. Delete any DB rows whose IDs are no longer present in MediaStore.
        resolver.query2(
            URI,
            projection = arrayOf(Column._ID), // Only need the unique IDs for comparison
            use = { c ->
                // Build a comma-separated list of IDs (e.g., "1,2,3").
                // Note: On Android 10+ you can use GROUP_CONCAT directly in SQL
                //       for faster aggregation instead of manual string building.
                val before = dao.count() // Snapshot count before deletion

                val ids = buildString {
                    while (c.moveToNext()) {
                        append(c.getLong(0))
                        if (!c.isLast)
                            append(',')
                    }
                }

                // Delete any DB entries whose storeId is NOT in the current MediaStore set.
                dao.deleteByStoreIdNotIn(ids)

                // Log how many rows were removed for debugging/monitoring.
                Log.d(TAG, "Deleted: ${before - dao.count()}")
            }
        )
        // --- Step 2: Compute modification threshold ---
        // Convert lastModified from millis → seconds and subtract 2s.
        // This offset ensures very recent changes are not missed.
        val lastModified = (dao.lastModified() / 1000 - 2)
        // --- Step 3: Fetch modified/new media ---
        // Goal: Detect new or updated media files since the last sync.
        // Strategy:
        //   1. Query MediaStore for items (images/videos) with DATE_MODIFIED >= lastModified.
        //   2. For each result, construct a MediaFile object.
        //   3. Upsert into DB: insert if new, update if existing.
        //   4. Wrap in a transaction for atomicity and consistency.
        resolver.query2(
            URI,
            // language = sql
            selection = "${Column.MEDIA_TYPE} IN (${Column.MEDIA_TYPE_IMAGE}, ${Column.MEDIA_TYPE_VIDEO}) AND ${Column.DATE_MODIFIED} >= $lastModified",
            projection = MEDIA_QUERY_PROJECTION,
            use = { cursor ->
                Log.d(TAG, "Changed: ${cursor.count}")
                val transaction = suspend {
                    val isAtLeast11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R


                    // Iterate through all changed rows.
                    while (cursor.moveToNext()) {
                        // 🔑 Step A: Identify the store ID and check if entry already exists in DB.
                        val storeID = cursor.getLong(0)
                        val existing = dao.getMediaFile(storeID) // null if not present
                        val mimeType = cursor.getString(3)
                        val newFile = MediaFile(
                            id = existing?.id ?: 0, // reuse DB id if present
                            storeID = storeID,
                            data = cursor.getString(1),   // file path
                            name = cursor.getString(2),     // display name
                            mimeType = mimeType,
                            size = cursor.getLong(4),
                            bitrate = cursor.getInt(5),
                            year = cursor.getInt(7),

                            // ⏱ Convert timestamps from seconds → millis
                            dateAdded = cursor.getLong(11) * 1000,
                            dateModified = cursor.getLong(12) * 1000,
                            dateTaken = cursor.getLong(13) * 1000,
                            dateExpires = if (isAtLeast11) cursor.getLong(14) * 1000 else Long.MIN_VALUE,

                            // 📝 Optional metadata (currently null / placeholder)
                            description = null,
                            location = MediaFile.Location(Float.NaN, Float.NaN),

                            // 📐 Resolution info
                            resolution = MediaFile.Resolution(cursor.getInt(8), cursor.getInt(9)),

                            // 🎞 Timeline: preserve playback position if available
                            timeline = let {
                                val position = existing?.timeline?.position ?: Int.MIN_VALUE
                                Timeline(position, cursor.getInt(5) / 1000 /* seconds */)
                            },

                            // ➕ Extras: preserve existing extras, update trash/orientation flags
                            extras = let {
                                val extras = existing?.extras ?: MediaFile.Extras(MediaProvider.DEFAULT_EXTRAS)
                                extras.copy(
                                    isTrashed = if (isAtLeast11) cursor.getInt(15) != 0 else false,
                                    orientation = mapNativeToAppOrientation(
                                        cursor.getInt(9),
                                        mimeType.startsWith("video")
                                    )
                                )
                            }
                        )
                        // TODO: Preserve custom tags during updates.
                        // Consider partial-update or SQL-level UPSERT to reduce overhead.
                        Log.d(TAG, "${if (existing == null) "New" else "Update"}: {${newFile}}")
                        // 🗄️ Step C: Upsert into DB (insert if new, update if existing).
                        if (existing == null) dao.insert(newFile) else dao.update(newFile)
                    }
                }
                // 🔒 Step D: Wrap in transaction for atomicity.
                db.withTransaction(transaction)
            }
        )
        return Result.success()
    }
}