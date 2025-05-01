/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 02-04-2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.core.store

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import com.zs.core.common.PathUtils
import com.zs.core.store.MediaProvider.Companion.COLUMN_DATE_MODIFIED
import com.zs.core.store.MediaProvider.Companion.COLUMN_ID
import com.zs.core.store.MediaProvider.Companion.COLUMN_IS_TRASHED
import com.zs.core.store.MediaProvider.Companion.COLUMN_MEDIA_TYPE
import com.zs.core.store.MediaProvider.Companion.COLUMN_NAME
import com.zs.core.store.MediaProvider.Companion.COLUMN_PATH
import com.zs.core.store.MediaProvider.Companion.COLUMN_SIZE
import com.zs.core.store.MediaProvider.Companion.EXTERNAL_CONTENT_URI
import com.zs.core.store.MediaProvider.Companion.MEDIA_PROJECTION
import com.zs.core.store.MediaProvider.Companion.MEDIA_TYPE_IMAGE
import com.zs.core.store.MediaProvider.Companion.MEDIA_TYPE_VIDEO
import kotlinx.coroutines.flow.Flow
import java.io.File

internal class MediaProviderImpl(context: Context) : MediaProvider {

    private val TAG = "MediaProviderImpl"

    private val resolver = context.contentResolver
    override fun observer(uri: Uri): Flow<Boolean> = resolver.observe(uri)

    /**
     * Counts the number of media items in the MediaStore.
     *
     * @param trashed Whether to include trashed items in the count. Defaults to false.
     * @return The number of media items.
     */
    private suspend fun count(trashed: Boolean = false): Int {
        /*val noTrashSelection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "$COLUMN_IS_TRASHED != 1" else ""*/
        // count all weather trashed or not.
        return resolver.query2(
            EXTERNAL_CONTENT_URI,
            arrayOf(COLUMN_ID),
            selection =  "${MediaStore.Audio.Media._ID} != 0"/*if (!trashed) noTrashSelection else "$COLUMN_IS_TRASHED == 1"*/,
            transform = { c ->
                c.count
            },
        )
    }

    /**
     * Fetches the content URIs for the given media IDs.
     *
     * This function queries the MediaStore to determine the typeof each media item (image or video)
     * based on the provided IDs and constructs the corresponding content URIs.
     *
     * @param ids The IDs of the media items to fetch URIs for.
     * @return A list of content URIs corresponding to the given IDs.
     */
    suspend fun fetchContentUri(vararg ids: Long): List<Uri> {
        // Create a comma-separated string of IDs for the SQL IN clause.
        val idsString = ids.joinToString(",") { it.toString() }

        // Define the projection to retrieve the ID and media type of each item.
        val projection = arrayOf(COLUMN_ID, COLUMN_MEDIA_TYPE)

        // Define the selection clause to filter items based on the provided IDs.
        val selection = "$COLUMN_ID IN ($idsString)"

        // Query the MediaStore and transform the result into a list of content URIs.
        return resolver.query2(
            EXTERNAL_CONTENT_URI, // The base content URI for media files
            projection, // The columns to retrieve
            selection, // The selection clause to filter results
            transform = { c ->
                List(c.count) { index -> // Iterate over the cursor results
                    c.moveToPosition(index) // Move to the current row
                    val type = c.getInt(1) // Get the media type (image or video)
                    // Construct the appropriate content URI based on the media type.
                    val uri = if (type == MEDIA_TYPE_IMAGE) {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    ContentUris.withAppendedId(uri, c.getLong(0)) // Append the ID to the URI
                }
            }
        )
    }

    override suspend fun delete(vararg id: Long): Int {
        // Create a comma-separated string of IDs for the SQL IN clause.
        val idString = id.joinToString(",") { "$it" }

        // Define the projection to retrieve the file path of each item.
        val projection = arrayOf(COLUMN_PATH)

        // Define the selection clause to filter items based on the provided IDs.
        val selection = "$COLUMN_ID IN ($idString)"

        // Query the MediaStore to get the file paths of the items to be deleted.
        val paths = resolver.query2(
            EXTERNAL_CONTENT_URI,
            projection,
            selection,
            transform = { c ->
                List(c.count) {
                    c.moveToPosition(it);
                    // Get the file path
                    c.getString(0)
                }
            }
        )
        // Attempt to delete the items from the MediaStore.
        var count = resolver.delete(
            EXTERNAL_CONTENT_URI,
            "${MediaStore.MediaColumns._ID} IN ($idString)",
            null
        )

        // Error deleting from MediaStore
        if (count == 0) return -1 // error

        // Iterate over the file paths and attempt to delete them from the file system.
        Log.d(TAG, "delete: $count")
        paths.forEach {
            val file = File(it)
            // Decrement count if file deletion fails
            if (file.exists() && !File(it).delete())
                count--
        }
        Log.d(TAG, "delete: $count")

        // Return the number of successfully deleted items
        return count
    }

    override suspend fun delete(vararg uri: Uri): Int {
        if (uri.isEmpty()) return 0
        val ids = uri.map { ContentUris.parseId(it) }
        return delete(*ids.toLongArray())
    }

    @SuppressLint("NewApi")
    override suspend fun delete(activity: Activity, vararg uri: Uri): Int {
        // Create a delete request for the given URIs.
        val request = MediaStore.createDeleteRequest(resolver, uri.toList())
        if (activity is ComponentActivity) {
            // If the activity is a ComponentActivity, use Activity Result
            // APIs for detailed feedback.
            // Count items before deletion (including trashed)
            val before = count(true)
            // Launch the delete request and get the result.
            val result = activity.launchForResult(
                request.intentSender
            )
            // Handle the result of the deletion request.
            // Deletion was canceled by the user
            if (Activity.RESULT_CANCELED == result.resultCode) return -3
            // Count items after deletion (including trashed)
            val after = count(true)
            // Return the number of deleted items
            if (Activity.RESULT_OK == result.resultCode) return after - before
            // Log for debugging if result is unexpected
            Log.d(TAG, "delete: before: $before, after: $after")
            // Deletion failed for an unknown reason
            return -1
        }
        // If the activity is not a ComponentActivity, initiate the deletion without detailed feedback.
        activity.startIntentSenderForResult(request.intentSender, 100, null, 0, 0, 0)
        // Deletion request initiated, but the exact outcome is unknown
        return -2
    }

    override suspend fun delete(activity: Activity, vararg id: Long): Int {
        val uri = fetchContentUri(*id).toTypedArray()
        return delete(activity, *uri)
    }

    @SuppressLint("NewApi")
    override suspend fun trash(activity: Activity, vararg uri: Uri): Int {
        // Create a delete request for the given URIs.
        val request = MediaStore.createTrashRequest(resolver, uri.toList(), true)
        if (activity is ComponentActivity) {
            // If the activity is a ComponentActivity, use Activity Result
            // APIs for detailed feedback.
            // Count items before deletion (including trashed)
            val before = count(false)
            // Launch the delete request and get the result.
            val result = activity.launchForResult(
                request.intentSender
            )
            // Handle the result of the deletion request.
            // Deletion was canceled by the user
            if (Activity.RESULT_CANCELED == result.resultCode) return -3
            // Count items after deletion (including trashed)
            val after = count(false)
            // Return the number of deleted items
            if (Activity.RESULT_OK == result.resultCode) return before - after
            // Log for debugging if result is unexpected
            Log.d(TAG, "delete: before: $before, after: $after")
            // Deletion failed for an unknown reason
            return -1
        }
        // If the activity is not a ComponentActivity, initiate the deletion without detailed feedback.
        activity.startIntentSenderForResult(request.intentSender, 100, null, 0, 0, 0)
        // Deletion request initiated, but the exact outcome is unknown
        return -2
    }

    override suspend fun trash(activity: Activity, vararg id: Long): Int {
        val uri = fetchContentUri(*id).toTypedArray()
        return trash(activity, *uri)
    }

    @SuppressLint("NewApi")
    override suspend fun restore(activity: Activity, vararg uri: Uri): Int {
        // Create a delete request for the given URIs.
        val request = MediaStore.createTrashRequest(resolver, uri.toList(), false)
        if (activity is ComponentActivity) {
            // If the activity is a ComponentActivity, use Activity Result
            // APIs for detailed feedback.
            // Count items before deletion (including trashed)
            val before = count(true)
            // Launch the delete request and get the result.
            val result = activity.launchForResult(
                request.intentSender
            )
            // Handle the result of the deletion request.
            // Deletion was canceled by the user
            if (Activity.RESULT_CANCELED == result.resultCode) return -3
            // Count items after deletion (including trashed)
            val after = count(true)
            // Log for debugging if result is unexpected
            Log.d(TAG, "restored: before: $before, after: $after")
            // Return the number of deleted items
            if (Activity.RESULT_OK == result.resultCode) return before - after
            // Deletion failed for an unknown reason
            return -1
        }
        // If the activity is not a ComponentActivity, initiate the deletion without detailed feedback.
        activity.startIntentSenderForResult(request.intentSender, 100, null, 0, 0, 0)
        // Deletion request initiated, but the exact outcome is unknown
        return -2
    }

    override suspend fun restore(activity: Activity, vararg id: Long): Int {
        val uri = fetchContentUri(*id).toTypedArray()
        return restore(activity, *uri)
    }

    override suspend fun fetchFiles(
        filter: String?,
        order: String,
        ascending: Boolean,
        parent: String?,
        offset: Int,
        limit: Int,
    ): List<MediaFile> {
        // TODO - Consider allowing users to specify mediaType as a parameter to customize
        //  the query.
        // Compose selection criteria based on user's input and filter settings.
        // On Android 10 and above, remove trashed items from the query to comply with scoped storage restrictions.
        // language = SQL
        val selection =  // Select only the mediaTypes of Image and Video
        // Filter out trashed items on Android 10+
            // Select only media of type Image or Video.
            "(${COLUMN_MEDIA_TYPE} = $MEDIA_TYPE_IMAGE OR $COLUMN_MEDIA_TYPE = ${MEDIA_TYPE_VIDEO})" +
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) " AND $COLUMN_IS_TRASHED != 1" else "" +
                            // Filter by parent directory if provided.
                            if (parent != null) " AND $COLUMN_PATH LIKE ?" else "" +
                                    // Add name filter if provided.
                                    if (filter != null) " AND $COLUMN_NAME LIKE ?" else ""
        // query for files.
        return resolver.query2(
            uri = EXTERNAL_CONTENT_URI,
            projection = MEDIA_PROJECTION,
            ascending = ascending,
            selection = selection,
            // provide args if available.
            args = when {
                filter != null && parent != null -> arrayOf("$parent%", "%$filter%")
                filter == null && parent != null -> arrayOf("$parent%")
                filter != null && parent == null -> arrayOf("%$filter%")
                else -> null // when both are null
            },
            order = order,
            offset = offset,
            limit = limit,
            transform = { c ->
                List(c.count) {
                    c.moveToPosition(it);
                    MediaFile(c)
                }
            },
        )
    }

    override suspend fun fetchFolders(
        filter: String?,
        ascending: Boolean,
        offset: Int,
        limit: Int,
    ): List<Folder> {
        // The selection to fetch all folders from the MediaStore.
        // FixMe - For Android versions below API 10, consider using GroupBy, Count, etc.
        //         In Android 10 and above, we rely on this current implementation.
        //         Additionally, explore ways to optimize performance for faster results.
        // Compose the selection for folders; exclude trashed items for Android 11 and above.
        //language = SQL
        val selection =
            "(${COLUMN_MEDIA_TYPE} = $MEDIA_TYPE_IMAGE OR $COLUMN_MEDIA_TYPE = ${MEDIA_TYPE_VIDEO})" +
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) " AND $COLUMN_IS_TRASHED != 1" else "" +
                            if (filter != null) " AND $COLUMN_NAME LIKE ?" else ""
        return resolver.query2(
            EXTERNAL_CONTENT_URI,
            arrayOf(COLUMN_ID, COLUMN_PATH, COLUMN_SIZE, COLUMN_DATE_MODIFIED),
            selection = selection,
            if (filter != null) arrayOf("%$filter%") else null,
            order = COLUMN_DATE_MODIFIED,
            ascending = ascending
        ) { c ->
            val list = ArrayList<Folder>()
            while (c.moveToNext()) {
                val path = c.getString(1)
                val parent = PathUtils.parent(path).let {
                    if (PathUtils.name(it).startsWith("img", true)) PathUtils.parent(it)
                    else it
                }
                val id = c.getLong(0)
                val size = c.getInt(2)
                val lastModified = c.getLong(3) * 1000
                val index = list.indexOfFirst { it.path == parent }
                if (index == -1) {
                    list += Folder(id, parent, 1, size, lastModified)
                    continue
                }
                val old = list[index]
                val artwork = if (old.lastModified > lastModified) old.artworkID else id
                list[index] = Folder(
                    artwork,
                    parent,
                    old.count + 1,
                    old.size + size,
                    maxOf(old.lastModified, lastModified)
                )
            }
            list
        }
    }


    override suspend fun fetchFiles(
        vararg ids: Long,
        order: String,
        ascending: Boolean,
        offset: Int,
        limit: Int,
    ): List<MediaFile> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",") { "$it" }
        return resolver.query2(
            uri = EXTERNAL_CONTENT_URI,
            projection = MEDIA_PROJECTION,
            ascending = ascending,
            selection = "$COLUMN_ID IN ($idsString)",
            // provide args if available.
            args = null,
            order = order,
            offset = offset,
            limit = limit,
            transform = { c ->
                List(c.count) {
                    c.moveToPosition(it);
                    MediaFile(c)
                }
            },
        )

    }

    override suspend fun fetchTrashedFiles(offset: Int, limit: Int): List<MediaFile> {
        return resolver.query2(
            EXTERNAL_CONTENT_URI,
            MediaProvider.MEDIA_PROJECTION_WITH_EXPIRES,
            selection = "$COLUMN_IS_TRASHED = 1",
            offset = offset,
            limit = limit,
            order = MediaProvider.COLUMN_DATE_EXPIRES,
            ascending = false,
            transform = { c ->
                List(c.count) { index ->
                    c.moveToPosition(index)
                    MediaFile(c)
                }
            }
        )
    }

    override suspend fun fetchFilesFromDirectory(
        path: String,
        filter: String?,
        order: String,
        ascending: Boolean,
        offset: Int,
        limit: Int,
    ): List<MediaFile> {
        // Compose a selection to return files from a folder path.
        // TODO: Refactor the original fetchFiles function to accept the parent as an argument;
        //       I believe it will work better.
        // language = SQL
        val like = if (filter != null) " AND $COLUMN_NAME LIKE ?" else ""
        val selection = "$COLUMN_PATH LIKE ?$like" +
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) " AND $COLUMN_IS_TRASHED != 1" else ""
        val args = if (filter != null) arrayOf("$path%", "%$filter%") else arrayOf("$path%")
        return resolver.query2(
            EXTERNAL_CONTENT_URI,
            projection = MEDIA_PROJECTION,
            selection = selection,
            args,
            order,
            ascending,
            offset,
            limit,
            transform = { c ->
                val list = MutableList(c.count) {
                    c.moveToPosition(it);
                    MediaFile(c)
                }
                // This section filters the results to include only files that are direct members of the specified folder,
                // excluding files from subfolders. An exception is made for "img" subfolders, which are considered as part of the parent folder,
                // to accommodate common camera storage patterns.
                list.removeAll {
                    var parent = PathUtils.parent(it.path)
                    val folderName = PathUtils.name(parent)
                    if (folderName.startsWith("img", true))
                        parent = PathUtils.parent(parent)
                    parent != path
                }
                list
            },
        )
    }
}