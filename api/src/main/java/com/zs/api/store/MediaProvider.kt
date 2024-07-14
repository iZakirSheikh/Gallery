package com.zs.api.store

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow

interface MediaProvider {

    companion object {
        /**
         * Column name for the unique ID of a media file.
         * @see MediaStore.Files.FileColumns._ID
         */
        const val COLUMN_ID = MediaStore.Files.FileColumns._ID

        /**
         * Column name for the display name of a media file.
         * @see MediaStore.Files.FileColumns.DISPLAY_NAME
         */
        const val COLUMN_NAME = MediaStore.Files.FileColumns.TITLE

        /**
         * Column name for the MIME type of a media file.
         * @see MediaStore.Files.FileColumns.MIME_TYPE
         */
        const val COLUMN_MIME_TYPE = MediaStore.Files.FileColumns.MIME_TYPE

        /**
         * Column name for the file path of a media file.
         * @see MediaStore.Files.FileColumns.DATA
         */
        const val COLUMN_PATH = MediaStore.Files.FileColumns.DATA

        /**
         * Column name for the date the media file was added.
         * @see MediaStore.Files.FileColumns.DATE_ADDED
         */
        const val COLUMN_DATE_ADDED = MediaStore.Files.FileColumns.DATE_ADDED

        /**
         * Column name for the date the media file was last modified.
         * @see MediaStore.Files.FileColumns.DATE_MODIFIED
         */
        const val COLUMN_DATE_MODIFIED = MediaStore.Files.FileColumns.DATE_MODIFIED

        /**
         * Column name for the size of a media file.
         * @see MediaStore.Files.FileColumns.SIZE
         */
        const val COLUMN_SIZE = MediaStore.Files.FileColumns.SIZE

        /**
         * Column name for the orientation of an image file.
         * @see MediaStore.MediaColumns.ORIENTATION
         */
        const val COLUMN_ORIENTATION = MediaStore.Files.FileColumns.ORIENTATION

        /**
         * Column name for the height of an image or video file.
         * @see MediaStore.Files.FileColumns.HEIGHT
         */
        const val COLUMN_HEIGHT = MediaStore.Files.FileColumns.HEIGHT

        /**
         * Column name for the width of an image or video file.
         * @see MediaStore.Files.FileColumns.WIDTH
         */
        const val COLUMN_WIDTH = MediaStore.Files.FileColumns.WIDTH

        /**
         * Column name for the date the media file was taken (applicable to images and videos).
         * @see MediaStore.Files.FileColumns.DATE_TAKEN
         */
        const val COLUMN_DATE_TAKEN = MediaStore.Files.FileColumns.DATE_TAKEN

        /**
         * Content URI for accessing media files stored externally.
         * @see MediaStore.Images.Media.EXTERNAL_CONTENT_URI
         */
        val EXTERNAL_CONTENT_URI = MediaStore.Files.getContentUri("external")

        /**
         * Column name for the Media Type of the file.
         */
        const val COLUMN_MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE

        /**
         * Media type private constant for video files.
         * @see MediaStore.Files.FilesColumn.MEDIA_TYPE
         */
        const val MEDIA_TYPE_VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

        /**
         * Media type constant for image files.
         * @see MediaStore.Files.FilesColumn.MEDIA_TYPE
         */
        const val MEDIA_TYPE_IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
    }

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
     * @throws SecurityException if the required storage permissions ([READ_EXTERNAL_STORAGE] or
     * [WRITE_EXTERNAL_STORAGE]) are not granted.
     * @throws NullPointerException if the returned cursor is null.
     */
    suspend fun getFiles(
        filter: String? = null,
        order: String = MediaStore.MediaColumns.TITLE,
        ascending: Boolean = true,
        parent: String? = null,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<Media>


    /**
     * Retrieves a list of folders based on the provided filter and sorting options.
     *
     * @param filter The filter string used to match specific folders. Default is `null`, which retrieves all folders.
     * @param ascending Specifies the sorting order of the folders. Default is `true`, which sorts in ascending order.
     * @param offset The offset index to start retrieving folders. Default is `0`.
     * @param limit The maximum number of folders to retrieve. Default is [Int.MAX_VALUE].
     * @return A list of [Folder] objects representing the retrieved folders.
     */
    suspend fun getFolders(
        filter: String? = null,
        ascending: Boolean = true,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<Folder>

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
}


/**
 * Provides an instance of [MediaProvider].
 *
 * @param context The application context.
 * @return An instance of[MediaProviderImpl].
 */
fun MediaProvider(context: Context): MediaProvider =
    MediaProviderImpl(context)