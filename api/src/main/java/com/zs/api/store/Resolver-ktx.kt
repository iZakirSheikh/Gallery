/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 11-07-2024.
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

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.zs.api.store.Media.Image
import com.zs.api.store.Media.Video
import com.zs.api.store.MediaProvider.Companion.COLUMN_DATE_ADDED
import com.zs.api.store.MediaProvider.Companion.COLUMN_DATE_MODIFIED
import com.zs.api.store.MediaProvider.Companion.COLUMN_DATE_TAKEN
import com.zs.api.store.MediaProvider.Companion.COLUMN_HEIGHT
import com.zs.api.store.MediaProvider.Companion.COLUMN_ID
import com.zs.api.store.MediaProvider.Companion.COLUMN_MEDIA_TYPE
import com.zs.api.store.MediaProvider.Companion.COLUMN_MIME_TYPE
import com.zs.api.store.MediaProvider.Companion.COLUMN_NAME
import com.zs.api.store.MediaProvider.Companion.COLUMN_ORIENTATION
import com.zs.api.store.MediaProvider.Companion.COLUMN_PATH
import com.zs.api.store.MediaProvider.Companion.COLUMN_SIZE
import com.zs.api.store.MediaProvider.Companion.COLUMN_WIDTH
import com.zs.api.store.MediaProvider.Companion.MEDIA_TYPE_IMAGE
import com.zs.api.store.MediaProvider.Companion.MEDIA_TYPE_VIDEO
import com.zs.api.util.PathUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext as using

private const val TAG = "Content-Resolver-ktx"

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
internal suspend fun ContentResolver.query2(
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
internal suspend inline fun <T> ContentResolver.query2(
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
internal inline fun ContentResolver.register(
    uri: Uri,
    crossinline onChanged: () -> Unit
): ContentObserver {
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
internal fun ContentResolver.observe(uri: Uri) = callbackFlow {
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
internal suspend fun ContentResolver.getMediaFiles(
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
        uri = MediaProvider.EXTERNAL_CONTENT_URI,
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
internal suspend fun ContentResolver.getFolders(
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
        MediaProvider.EXTERNAL_CONTENT_URI,
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

