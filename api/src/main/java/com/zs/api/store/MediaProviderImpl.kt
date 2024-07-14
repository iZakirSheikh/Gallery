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
import android.content.Context
import android.content.IntentSender
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.zs.api.store.MediaProvider.Companion.COLUMN_DATE_ADDED
import com.zs.api.store.MediaProvider.Companion.COLUMN_DATE_EXPIRES
import com.zs.api.store.MediaProvider.Companion.COLUMN_DATE_MODIFIED
import com.zs.api.store.MediaProvider.Companion.COLUMN_DATE_TAKEN
import com.zs.api.store.MediaProvider.Companion.COLUMN_HEIGHT
import com.zs.api.store.MediaProvider.Companion.COLUMN_ID
import com.zs.api.store.MediaProvider.Companion.COLUMN_IS_TRASHED
import com.zs.api.store.MediaProvider.Companion.COLUMN_MEDIA_TYPE
import com.zs.api.store.MediaProvider.Companion.COLUMN_MIME_TYPE
import com.zs.api.store.MediaProvider.Companion.COLUMN_NAME
import com.zs.api.store.MediaProvider.Companion.COLUMN_ORIENTATION
import com.zs.api.store.MediaProvider.Companion.COLUMN_PATH
import com.zs.api.store.MediaProvider.Companion.COLUMN_SIZE
import com.zs.api.store.MediaProvider.Companion.COLUMN_WIDTH
import com.zs.api.store.MediaProvider.Companion.EXTERNAL_CONTENT_URI
import com.zs.api.store.MediaProvider.Companion.MEDIA_TYPE_IMAGE
import com.zs.api.store.MediaProvider.Companion.MEDIA_TYPE_VIDEO
import com.zs.api.util.PathUtils
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "MediaProviderImpl"

/**
 * Construct the [MediaFile] from current position of cursor.
 */
private val Cursor.toMediaFile: MediaFile
    inline get() {
        return MediaFile(
            id = getLong(0),
            name = getString(1),
            dateAdded = getLong(2) * 1000,
            dateModified = getLong(3) * 1000,
            size = getLong(4),
            mimeType = getString(5),
            orientation = getInt(6),
            height = getInt(7),
            width = getInt(8),
            path = getString(9),
            dateTaken = getLong(10) * 1000,
            duration = getInt(11)
        )
    }

private val Cursor.toTrashedFile: Trashed
    inline get() = Trashed(
        id = getLong(0),
        name = getString(1),
        expires = getLong(2) * 1000,
        path = getString(3),
        size = getLong(4),
        mimeType = getString(5)
    )

private val MEDIA_PROJECTION =
    arrayOf(
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
        //  COLUMN_MEDIA_TYPE, // 11
        MediaProvider.COLUMN_DURATION // 11
    )

private val TRASHED_PROJECTION =
    arrayOf(
        COLUMN_ID, // 0
        COLUMN_NAME, // 1
        COLUMN_DATE_EXPIRES, // 2
        COLUMN_PATH, // 3
        COLUMN_SIZE, // 4
        COLUMN_MIME_TYPE, // 5
    )

internal class MediaProviderImpl(context: Context) : MediaProvider {
    private val resolver = context.contentResolver
    override fun observer(uri: Uri): Flow<Boolean> = resolver.observe(uri)
    override fun register(uri: Uri, onChanged: () -> Unit): ContentObserver =
        resolver.register(uri, onChanged)

    override suspend fun fetchFiles(
        filter: String?,
        order: String,
        ascending: Boolean,
        parent: String?,
        offset: Int,
        limit: Int
    ): List<MediaFile> {
        // Compose selection.
        // FixMe - Maybe allow user somehow pass mediaType as parameter.
        // Remove trashed items from the query if build version is Android 10 or above.
        //language = SQL
        val selection =  // Select only the mediaTypes of Image and Video
        // Filter out trashed items on Android 10+
            // Select only media of type Image or Video.
            "(${COLUMN_MEDIA_TYPE} = $MEDIA_TYPE_IMAGE OR $COLUMN_MEDIA_TYPE = ${MEDIA_TYPE_VIDEO})" +
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "AND $COLUMN_IS_TRASHED != 1" else "" +
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
                    c.toMediaFile
                }
            },
        )
    }

    override suspend fun fetchFiles(
        vararg ids: Long,
        order: String,
        ascending: Boolean,
        offset: Int,
        limit: Int
    ): List<MediaFile> {
        val idsString = ids.joinToString(",") { it.toString() }
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
                    c.toMediaFile
                }
            },
        )
    }

    override suspend fun fetchFolders(
        filter: String?,
        ascending: Boolean,
        offset: Int,
        limit: Int
    ): List<Folder> {
        // Compose selection.
        // FixMe - Maybe allow user somehow pass mediaType as parameter.
        //language = SQL
        val selection =
            "(${COLUMN_MEDIA_TYPE} = $MEDIA_TYPE_IMAGE OR $COLUMN_MEDIA_TYPE = ${MEDIA_TYPE_VIDEO})" + if (filter != null) " AND $COLUMN_NAME LIKE ?" else ""
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
                val lastModified = c.getLong(3)
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

    override suspend fun fetchTrashedFiles(offset: Int, limit: Int): List<Trashed> {
        TODO("Not yet implemented")
    }


    override suspend fun delete(vararg uri: Uri): Int {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override suspend fun delete(activity: Activity, vararg uri: Uri): Int {
        val activity = (activity as? ComponentActivity) ?: return -1
        val result = activity.launchForResult(
            MediaStore.createDeleteRequest(resolver, uri.toList()).intentSender
        )
        return if (Activity.RESULT_OK == result.resultCode) 1 else 0
    }

    override suspend fun trash(activity: Activity, vararg uri: Uri): Int {
        TODO("Not yet implemented")
    }

    override suspend fun restore(activity: Activity, vararg uri: Uri): Int {
        TODO("Not yet implemented")
    }
}

private suspend fun ComponentActivity.launchForResult(request: IntentSender): ActivityResult =
    suspendCoroutine { cont ->
        var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
        // Assign result to launcher in such a way tha it allows us to
        // unregister later.
        val contract = ActivityResultContracts.StartIntentSenderForResult()
        val key = UUID.randomUUID().toString()
        launcher = activityResultRegistry.register(key, contract) { it ->
            // unregister launcher
            launcher?.unregister()
            Log.d(TAG, "launchForResult: $it")
            cont.resume(it)
        }
        // Create an IntentSenderRequest object from the IntentSender object
        val intentSenderRequest = IntentSenderRequest.Builder(request).build()
        // Launch the activity for result using the IntentSenderRequest object
        launcher.launch(intentSenderRequest)
    }