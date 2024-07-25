/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 20-07-2024.
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

package com.zs.api.store

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow

/**
 * Provides access to media files on the device.
 *
 * This interface defines methods for fetching, searching, and managing media files.
 * Implementations of this interface can be used to interact with different media sources,
 * such as the local filesystem, cloud storage, or external devices.
 */
interface MediaProvider {

    /**
     * @property COLUMN_ID Column name for the unique ID of a media file.
     * @see MediaStore.Files.FileColumns._ID
     *
     * @property COLUMN_NAME Column name for the display name of a media file.
     * @see MediaStore.Files.FileColumns.DISPLAY_NAME
     *
     * @property COLUMN_MIME_TYPE Column name for the MIME type of a media file.
     * @see MediaStore.Files.FileColumns.MIME_TYPE
     *
     * @property COLUMN_PATH Column name for the file path of a media file.
     * @see MediaStore.Files.FileColumns.DATA
     *
     * @property COLUMN_DATE_ADDED Column name for the date the media file was added.
     * @see MediaStore.Files.FileColumns.DATE_ADDED
     *
     * @property COLUMN_DATE_MODIFIED Column name for the date the media file was last modified.
     * @see MediaStore.Files.FileColumns.DATE_MODIFIED
     *
     * @property COLUMN_SIZE Column name for the size of a media file.
     * @see MediaStore.Files.FileColumns.SIZE
     *
     * @property COLUMN_ORIENTATION Column name for the orientation of an image file.
     * @see MediaStore.MediaColumns.ORIENTATION
     *
     * @property COLUMN_HEIGHT Column name for the height of an image or video file.
     * @see MediaStore.Files.FileColumns.HEIGHT
     *
     * @property COLUMN_WIDTH Column name for the width of an image or video file.
     * @see MediaStore.Files.FileColumns.WIDTH
     *
     * @property COLUMN_DATE_TAKEN Column name for the date the media file was taken (applicable to images and videos).
     * @see MediaStore.Files.FileColumns.DATE_TAKEN
     *
     * @property EXTERNAL_CONTENT_URI Content URI for accessing media files stored externally.
     * @see MediaStore.Images.Media.EXTERNAL_CONTENT_URI
     *
     * @property COLUMN_MEDIA_TYPE Column name for the Media Type of the file.
     *
     * @property MEDIA_TYPE_VIDEO Media type constant for video files.
     * @see MediaStore.Files.FileColumns.MEDIA_TYPE
     *
     * @property MEDIA_TYPE_IMAGE Media type constant for image files.
     * @see MediaStore.Files.FileColumns.MEDIA_TYPE
     * @property COLUMN_DURATION Column name for the duration of a video file in (ms).
     */
    companion object {
        const val COLUMN_ID = MediaStore.Files.FileColumns._ID
        const val COLUMN_NAME = MediaStore.Files.FileColumns.TITLE
        const val COLUMN_MIME_TYPE = MediaStore.Files.FileColumns.MIME_TYPE
        const val COLUMN_PATH = MediaStore.Files.FileColumns.DATA
        const val COLUMN_DATE_ADDED = MediaStore.Files.FileColumns.DATE_ADDED
        const val COLUMN_DATE_MODIFIED = MediaStore.Files.FileColumns.DATE_MODIFIED
        const val COLUMN_SIZE = MediaStore.Files.FileColumns.SIZE
        const val COLUMN_ORIENTATION = MediaStore.Files.FileColumns.ORIENTATION
        const val COLUMN_HEIGHT = MediaStore.Files.FileColumns.HEIGHT
        const val COLUMN_WIDTH = MediaStore.Files.FileColumns.WIDTH
        const val COLUMN_DATE_TAKEN = MediaStore.Files.FileColumns.DATE_TAKEN
        val EXTERNAL_CONTENT_URI = MediaStore.Files.getContentUri("external")
        const val COLUMN_MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE
        const val MEDIA_TYPE_VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        const val MEDIA_TYPE_IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        const val COLUMN_DURATION = MediaStore.Files.FileColumns.DURATION
        internal const val COLUMN_IS_TRASHED = MediaStore.Files.FileColumns.IS_TRASHED
        internal const val COLUMN_DATE_EXPIRES = MediaStore.Files.FileColumns.DATE_EXPIRES

        /**
         * @return The content URI for [id] provider.
         */
        fun contentUri(id: Long) = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id)
    }

    /**
     * Observes changes in the data identified by the given [uri] and emits the change events as a flow of booleans.
     *
     * This function registers a [ContentObserver] with the specified [uri] and emits a boolean value indicating whether
     * the observed data has changed. The flow will emit `false` immediately upon registration and subsequently emit `true`
     * whenever a change occurs.
     *
     * @param uri The content URI to observe for changes.
     * @return A flow of boolean values indicating whether the observed data has changed.
     * @see ContentResolver.registerContentObserver
     */
    fun observer(uri: Uri): Flow<Boolean>

    /**
     * Registers a [ContentObserver] to receive notifications for changes in the specified [uri].
     *
     * This function registers a [ContentObserver] with the given [uri] and invokes the [onChanged]
     * callback whenever a change occurs.
     *
     * @param uri The URI to monitor for changes.
     * @param onChanged The callback function to be invoked when a change occurs.
     * @return The registered [ContentObserver] instance.
     * @see ContentResolver.registerContentObserver
     */
    fun register(uri: Uri, onChanged: () -> Unit): ContentObserver

    /**
     * Retrieves media files of type [Image] or [Video] from the [MediaStore].
     *
     * @param filter The filter string used to match specific media files. Default is `null`, which retrieves all media files.
     * @param order The column name to sort the media files. Default is [File.COLUMN_NAME].
     * @param ascending Specifies the sorting order of the media files. Default is `true`, which sorts in ascending order.
     * @param parent The parent directory of the media files. Default is `null`, which retrieves media files from all directories.
     * @param offset The offset index to start retrieving media files. Default is `0`.
     * @param limit The maximum number of media files to retrieve. Default is [Int.MAX_VALUE].
     * @return A list of [File] objects representing the retrieved media files.
     */
    suspend fun fetchFiles(
        filter: String? = null,
        order: String = COLUMN_NAME,
        ascending: Boolean = true,
        parent: String? = null,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<MediaFile>

    /**
     * Retrieves a list of folders based on the provided filter and sorting options.
     *
     * @param filter The filter string used to match specific folders. Default is `null`, which retrieves all folders.
     * @param ascending Specifies the sorting order of the folders. Default is `true`, which sorts in ascending order.
     * @param offset The offset index to start retrieving folders. Default is `0`.
     * @param limit The maximum number of folders to retrieve. Default is [Int.MAX_VALUE].
     * @return A list of [Folder] objects representing the retrieved folders.
     */
    suspend fun fetchFolders(
        filter: String? = null,
        ascending: Boolean = true,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<Folder>


    /**
     * Retrieves a list of trashed files.
     *
     * @param offset The number of files to skip (for pagination).
     * @param limit The maximum number of files to return (for pagination).
     * @return A list of [Trashed] objects representing the trashed files.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun fetchTrashedFiles(
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<Trashed>

    /**
     * Retrieves a list of media files based on their IDs.
     *
     * @param ids The IDs of the media files to fetch.
     * @param order The column to order the results by (default is [COLUMN_NAME]).
     * @param ascending Whether to sort the results in ascending order (default is true).
     * @param offset The number of files to skip (for pagination).
     * @param limit The maximum number of files to return (for pagination).
     * @return A list of [MediaFile] objects representing the fetched files.
     */
    suspend fun fetchFiles(
        vararg ids: Long,
        order: String = COLUMN_NAME,
        ascending: Boolean = true,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<MediaFile>

    /**
     * Retrieves a list of media files from a specific directory based on the provided filter and sorting options.
     *
     * @param filter The filter to apply to the media files.
     * @param sortOrder The order in which to sort the media files.
     * @return A list of mediafiles that match the filter and sort order.
     *
     * FIXME - This function is deprecated and will be removed in a future release. Use [fetchFiles] instead,
     *          providing the parent directory as an argument.
     */
    suspend fun fetchFilesFromDirectory(
        path: String,
        filter: String? = null,
        order: String = COLUMN_NAME,
        ascending: Boolean = true,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<MediaFile>

    /**
     * Deletes the specified URIs from the device's persistent storage permanently.
     *
     *  Note hat this fun works only unto android 10.
     * @param uri The URIs to delete.
     * @return The number of items that were deleted, or -1 if an error occurred.
     */
    suspend fun delete(vararg uri: Uri): Int

    /**
     * Permanently deletes content from the device at the specified URIs.
     *
     * @param activity The Activity used to request deletion. If it is a [ComponentActivity],
     * it reports back information about the deletion. Otherwise, it simply performs the deletion.
     * @param uri The URIs of the content to delete.
     * @return The number of items deleted, or -1 if an error occurred, or -3 ifcanceled by the user.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun delete(activity: Activity, vararg uri: Uri): Int

    /**
     * @see delete
     */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun trash(activity: Activity, vararg uri: Uri): Int

    /**
     * @see delete
     */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun restore(activity: Activity, vararg uri: Uri): Int
}

/**
 * Provides an instance of [MediaProvider].
 *
 * @param context The application context.
 * @return An instance of[MediaProviderImpl].
 */
fun MediaProvider(context: Context): MediaProvider = MediaProviderImpl(context)