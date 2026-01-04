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
import com.zs.common.db.album.MediaProvider
import com.zs.common.db.album.MediaFile
import com.zs.common.db.album.MediaFile.Timeline
import com.zs.common.util.query2
import org.intellij.lang.annotations.Language
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
internal class MediaSyncWorker private constructor(
    val context: Context,
    params: Params
) : CoroutineWorker(context, params) {

    private val TAG = "SyncWorker"

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
            FC._ID // 14
        )

    /** Projection adjusted for API level (adds DATE_EXPIRES, IS_TRASHED on Android 11+). */
    private val PROJECTION = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> _PROJECTION
        else -> _PROJECTION + arrayOf(FC.DATE_EXPIRES, FC.IS_TRASHED)
    }

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

    // ─────────────────────────────────────────────
    // Synchronization workflow
    // ─────────────────────────────────────────────
    override suspend fun doWork(): Result {
        // Schedule next cycle immediately to keep sync continuous
        schedule(context)

        val db = AppDb.getInstance()                // database wrapper
        val resolver = context.contentResolver      // MediaStore access
        val dao = db.mediaProvider                   // DAO for media table
        // --- Step 1: Detect missing files ---
        // TODO: Replace DATA-based matching with store_id for better stability.
        // Query MediaStore for all current file paths (DATA column only).
        // Delete any DB entries not present in MediaStore.
        val count = resolver.query2(
            URI,
            projection = arrayOf(@Language("RoomSql") "GROUP_CONCAT(${FC._ID}, ''',''')"),
            use = { c ->
                c.moveToFirst()
                val ids = "'${c.getString(0)}'"
                dao.deleteNotIn(ids) // bulk delete rows not in MediaStore
            }
        )
        Log.d(TAG, "Deleted = $count")
        // --- Step 2: Compute modification threshold ---
        // Convert lastModified from millis → seconds and subtract 2s.
        // This offset ensures very recent changes are not missed.
        val lastModified = (dao.lastModified() / 1000 - 2)
        // --- Step 3: Fetch modified/new media ---
        resolver.query2(
            URI,
            selection = @Language("SQL") "${FC.MEDIA_TYPE} IN (${FC.MEDIA_TYPE_IMAGE}, ${FC.MEDIA_TYPE_VIDEO}) AND ${FC.DATE_MODIFIED} >= $lastModified",
            projection = PROJECTION,
            use = { cursor ->
                Log.d(TAG, "Changed: ${cursor.count}")
                val transaction = suspend {
                    // Iterate through each row and upsert into DB.
                    val isAtLeast11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    while (cursor.moveToNext()) {
                        val storeID = cursor.getLong(14)
                        val existing = dao.getMediaFile(storeID) // get existing or null mediaFile.
                        // Construct new MediaFile object (reuse DB id if present).
                        val mimeType = cursor.getString(2)
                        val newFile = MediaFile(
                            id = existing?.id ?: 0, // reuse DB id if present
                            storeID = storeID,
                            source = cursor.getString(0),
                            name = cursor.getString(1),
                            mimeType = mimeType,
                            size = cursor.getLong(3),
                            bitrate = cursor.getInt(5),
                            year = cursor.getInt(6),
                            dateAdded = cursor.getLong(10) * 1000,    // convert seconds → millis
                            dateModified = cursor.getLong(11) * 1000, // convert seconds → millis
                            dateTaken = cursor.getLong(12) * 1000,    // convert seconds → millis
                            dateExpires = if (isAtLeast11) cursor.getLong(13) * 1000 else Long.MIN_VALUE,
                            description = null,
                            location = MediaFile.Location(Float.NaN, Float.NaN),
                            resolution = MediaFile.Resolution(cursor.getInt(7), cursor.getInt(8)),
                            timeline = let {
                                val position = existing?.timeline?.position ?: Int.MIN_VALUE
                                Timeline(position, cursor.getInt(4) / 1000/*s*/)
                            },
                            extras = let {
                                val extras =
                                    existing?.extras ?: MediaFile.Extras(MediaProvider.DEFAULT_EXTRAS)
                                extras.copy(
                                    isTrashed = if (isAtLeast11) cursor.getInt(14) != 0 else false,
                                    orientation = mapNativeToAppOrientation(
                                        cursor.getInt(9),
                                        mimeType.startsWith("video")
                                    )
                                )
                            }
                        )
                        // TODO: Preserve custom tags during updates.
                        // A partial-update or SQL-level upsert strategy could significantly
                        // reduce overhead and improve performance.
                        Log.d(TAG, "${if (existing == null) "New" else "Update"}: {${newFile.source}}")
                        // Upsert: insert if new, update if existing.
                        if (existing == null) dao.insert(newFile) else dao.update(newFile)
                    }
                }
                db.withTransaction(transaction)
            }
        )
        return Result.success()
    }
}