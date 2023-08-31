package com.prime.gallery.core.db

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.compose.runtime.Stable
import com.prime.gallery.core.db.Media.Image
import com.prime.gallery.core.db.Media.Video
import com.prime.gallery.core.util.PathUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext as using

private const val TAG = "LocalMediaProvider"

private const val DUMMY_SELECTION = "${MediaStore.Audio.Media._ID} != 0"

/**
 * An advanced version of [ContentResolver.query] with additional features.
 *
 * This function performs a query on the given [uri] using the specified parameters.
 *
 * @param uri The URI to query.
 * @param projection The list of columns to include in the result. Default is `null`, which returns all columns.
 * @param selection The selection criteria. Default is [DUMMY_SELECTION], which retrieves all rows.
 * @param args The selection arguments. Default is `null`.
 * @param order The column name to use for ordering the results. Default is [MediaStore.MediaColumns._ID].
 * @param ascending Specifies the sorting order of the results. Default is `true`, which sorts in ascending order.
 * @param offset The offset index to start retrieving the results. Default is `0`.
 * @param limit The maximum number of results to retrieve. Default is [Int.MAX_VALUE].
 * @return A [Cursor] object representing the query results.
 * @throws NullPointerException if the returned cursor is null.
 * @see ContentResolver.query
 */
private suspend fun ContentResolver.query2(
    uri: Uri,
    projection: Array<String>? = null,
    selection: String = DUMMY_SELECTION,
    args: Array<String>? = null,
    order: String = MediaStore.MediaColumns._ID,
    ascending: Boolean = true,
    offset: Int = 0,
    limit: Int = Int.MAX_VALUE
): Cursor {
    return using(Dispatchers.Default) {
        // use only above android 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // compose the args
            val args2 = Bundle().apply {
                // Limit & Offset
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)

                // order
                putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(order))
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    if (ascending) ContentResolver.QUERY_SORT_DIRECTION_ASCENDING else ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
                // Selection and groupBy
                if (args != null) putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, args)
                // add selection.
                // TODO: Consider adding group by.
                // currently I experienced errors in android 10 for groupBy and arg groupBy is supported
                // above android 10.
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            }
            query(uri, projection, args2, null)
        }
        // below android 0
        else {
            //language=SQL
            val order2 =
                order + (if (ascending) " ASC" else " DESC") + " LIMIT $limit OFFSET $offset"
            // compose the selection.
            query(uri, projection, selection, args, order2)
        }
    } ?: throw NullPointerException("Can't retrieve cursor for $uri")
}

/**
 * @see query2
 */
private suspend inline fun <T> ContentResolver.query2(
    uri: Uri,
    projection: Array<String>? = null,
    selection: String = DUMMY_SELECTION,
    args: Array<String>? = null,
    order: String = MediaStore.MediaColumns._ID,
    ascending: Boolean = true,
    offset: Int = 0,
    limit: Int = Int.MAX_VALUE,
    transform: (Cursor) -> T
): T = query2(uri, projection, selection, args, order, ascending, offset, limit).use(transform)

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
inline fun ContentResolver.register(uri: Uri, crossinline onChanged: () -> Unit): ContentObserver {
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            onChanged()
        }
    }
    registerContentObserver(uri, false, observer)
    return observer
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
fun ContentResolver.observe(uri: Uri) = callbackFlow {
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            trySend(selfChange)
        }
    }
    registerContentObserver(uri, true, observer)
    // trigger first.
    trySend(false)
    awaitClose {
        unregisterContentObserver(observer)
    }
}

/**
 * Represents a media file.
 *
 * This sealed interface defines the common properties that can be associated with a media file.
 * Implementing classes can provide specific implementations based on the type of media file.
 *
 * @property id The ID of the media file.
 * @property name The name of the media file.
 * @property mimeType The MIME type of the media file.
 * @property parent The parent directory of the media file.
 * @property path The path of the media file.
 * @property dateAdded The date when the media file was added.
 * @property dateModified The date when the media file was last modified.
 * @property size The size of the media file in bytes.
 * @property dateTaken The date when the media was taken (applicable for photos).
 * @property orientation The orientation of the media file.
 * @property height The height of the media file (applicable for photos).
 * @property width The width of the media file (applicable for photos).
 */
@Stable
sealed interface Media {
    val id: Long
    val name: String
    val mimeType: String
    val parent: String
    val path: String
    val dateAdded: Long
    val dateModified: Long
    val size: Int
    val dateTaken: Long
    val orientation: Int
    val height: Int
    val width: Int

    // TODO: Maybe include in future version the necessary permission.
    val latitude: Float get() = 0.0f
    val longitude: Float get() = 0.0f


    data class Image(
        override val id: Long,
        override val name: String,
        override val mimeType: String,
        override val parent: String,
        override val path: String,
        override val dateAdded: Long,
        override val dateModified: Long,
        override val size: Int,
        override val dateTaken: Long,
        override val orientation: Int,
        override val height: Int,
        override val width: Int
    ) : Media

    data class Video(
        override val id: Long,
        override val name: String,
        override val mimeType: String,
        override val parent: String,
        override val path: String,
        override val dateAdded: Long,
        override val dateModified: Long,
        override val size: Int,
        override val dateTaken: Long,
        override val orientation: Int,
        override val height: Int,
        override val width: Int
    ) : Media
}

/**
 * Represents a folder with associated properties.
 *
 * @property artwork The artwork associated with the folder.
 * @property path The path of the folder.
 * @property count The count of items within the folder.
 * @property size The size of the folder in bytes.
 */
@Stable
class Folder(
    @JvmField val artwork: String,
    @JvmField val path: String,
    @JvmField val count: Int,
    @JvmField val size: Int,
    @JvmField val lastModified: Long
) {
    val name: String get() = PathUtils.name(path)
}

/**
 * Column name for the unique ID of a media file.
 * @see MediaStore.Files.FileColumns._ID
 */
private const val COLUMN_ID = MediaStore.Files.FileColumns._ID

/**
 * Column name for the display name of a media file.
 * @see MediaStore.Files.FileColumns.DISPLAY_NAME
 */
private const val COLUMN_NAME = MediaStore.Files.FileColumns.TITLE

/**
 * Column name for the MIME type of a media file.
 * @see MediaStore.Files.FileColumns.MIME_TYPE
 */
private const val COLUMN_MIME_TYPE = MediaStore.Files.FileColumns.MIME_TYPE

/**
 * Column name for the file path of a media file.
 * @see MediaStore.Files.FileColumns.DATA
 */
private const val COLUMN_PATH = MediaStore.Files.FileColumns.DATA

/**
 * Column name for the date the media file was added.
 * @see MediaStore.Files.FileColumns.DATE_ADDED
 */
private const val COLUMN_DATE_ADDED = MediaStore.Files.FileColumns.DATE_ADDED

/**
 * Column name for the date the media file was last modified.
 * @see MediaStore.Files.FileColumns.DATE_MODIFIED
 */
private const val COLUMN_DATE_MODIFIED = MediaStore.Files.FileColumns.DATE_MODIFIED

/**
 * Column name for the size of a media file.
 * @see MediaStore.Files.FileColumns.SIZE
 */
private const val COLUMN_SIZE = MediaStore.Files.FileColumns.SIZE

/**
 * Column name for the orientation of an image file.
 * @see MediaStore.MediaColumns.ORIENTATION
 */
private const val COLUMN_ORIENTATION = MediaStore.Files.FileColumns.ORIENTATION

/**
 * Column name for the height of an image or video file.
 * @see MediaStore.Files.FileColumns.HEIGHT
 */
private const val COLUMN_HEIGHT = MediaStore.Files.FileColumns.HEIGHT

/**
 * Column name for the width of an image or video file.
 * @see MediaStore.Files.FileColumns.WIDTH
 */
private const val COLUMN_WIDTH = MediaStore.Files.FileColumns.WIDTH

/**
 * Column name for the date the media file was taken (applicable to images and videos).
 * @see MediaStore.Files.FileColumns.DATE_TAKEN
 */
private const val COLUMN_DATE_TAKEN = MediaStore.Files.FileColumns.DATE_TAKEN

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
private const val MEDIA_TYPE_VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

/**
 * Media type constant for image files.
 * @see MediaStore.Files.FilesColumn.MEDIA_TYPE
 */
private const val MEDIA_TYPE_IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE

private val MEDIA_PROJECTION = arrayOf(
    COLUMN_ID, // 0
    COLUMN_NAME, // 1
    COLUMN_DATE_ADDED, // 2
    COLUMN_DATE_MODIFIED, // 3
    COLUMN_SIZE, // 4
    COLUMN_MIME_TYPE, // 5
    COLUMN_ORIENTATION, // 6
    COLUMN_HEIGHT,// 7
    COLUMN_WIDTH, // 8
    COLUMN_PATH, // 9
    COLUMN_DATE_TAKEN, // 10
    COLUMN_MEDIA_TYPE, // 11
)

/**
 * Construct the [Image] from current position of cursor.
 */
private val Cursor.toImage: Image
    inline get() {
        return Image(
            id = getLong(0),
            name = getString(1),
            dateAdded = getLong(2) * 1000,
            dateModified = getLong(3) * 1000,
            size = getInt(4),
            mimeType = getString(5),
            orientation = getInt(6),
            height = getInt(7),
            width = getInt(8),
            path = getString(9),
            dateTaken = getLong(10) * 1000,
            parent = getString(11)
        )
    }

/**
 * Construct the [Video] from current position of cursor.
 */
private val Cursor.toVideo: Video
    inline get() {
        return Video(
            id = getLong(0),
            name = getString(1),
            dateAdded = getLong(2) * 1000,
            dateModified = getLong(3) * 1000,
            size = getInt(4),
            mimeType = getString(5),
            orientation = getInt(6),
            height = getInt(7),
            width = getInt(8),
            path = getString(9),
            dateTaken = getLong(10) * 1000,
            parent = getString(11)
        )
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
suspend fun ContentResolver.getMediaFiles(
    filter: String? = null,
    order: String = COLUMN_NAME,
    ascending: Boolean = true,
    parent: String? = null,
    offset: Int = 0,
    limit: Int = Int.MAX_VALUE
): List<Media> {
    // Compose selection.
    // FixMe - Maybe allow user somehow pass mediaType as parameter.
    //language = SQL
    val selection =  // Select only the mediaTypes of Image and Video
        "(${COLUMN_MEDIA_TYPE} = ${MEDIA_TYPE_IMAGE} OR ${COLUMN_MEDIA_TYPE} = ${MEDIA_TYPE_VIDEO})" + if (parent != null) " AND ${COLUMN_PATH} LIKE ?" else "" + // If parent is non-null return media from that particular directory only
                if (filter != null) " AND ${COLUMN_NAME} LIKE ?" else "" // Add filter to selection if filter param is non-null.
    // Return the Files.
    return query2(
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
                val type = c.getInt(11)
                if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) c.toVideo
                else c.toImage
            }
        },
    )
}


/**
 * Retrieves a list of folders based on the provided filter and sorting options.
 *
 * @param filter The filter string used to match specific folders. Default is `null`, which retrieves all folders.
 * @param ascending Specifies the sorting order of the folders. Default is `true`, which sorts in ascending order.
 * @param offset The offset index to start retrieving folders. Default is `0`.
 * @param limit The maximum number of folders to retrieve. Default is [Int.MAX_VALUE].
 * @return A list of [Folder] objects representing the retrieved folders.
 */
suspend fun ContentResolver.getFolders(
    filter: String? = null,
    ascending: Boolean = true,
    offset: Int = 0,
    limit: Int = Int.MAX_VALUE
): List<Folder> {
    // Compose selection.
    // FixMe - Maybe allow user somehow pass mediaType as parameter.
    //language = SQL
    val selection =
        "(${COLUMN_MEDIA_TYPE} = $MEDIA_TYPE_IMAGE OR $COLUMN_MEDIA_TYPE = ${MEDIA_TYPE_VIDEO})" + if (filter != null) " AND $COLUMN_NAME LIKE ?" else ""
    return query2(
        EXTERNAL_CONTENT_URI,
        arrayOf(COLUMN_PATH, COLUMN_SIZE, COLUMN_DATE_MODIFIED),
        selection = selection,
        if (filter != null) arrayOf("%$filter%") else null,
        order = COLUMN_DATE_MODIFIED,
        ascending = ascending
    ) { c ->
        val list = ArrayList<Folder>()
        while (c.moveToNext()) {
            val path = c.getString(0)
            val parent = PathUtils.parent(path).let {
                if (PathUtils.name(it).startsWith("img", true)) PathUtils.parent(it)
                else it
            }
            val size = c.getInt(1)
            val lastModified = c.getLong(2)
            val index = list.indexOfFirst { it.path == parent }
            if (index == -1) {
                list += Folder(path, parent, 1, size, lastModified)
                continue
            }
            val old = list[index]
            val artwork = if (old.lastModified > lastModified) old.artwork else path
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