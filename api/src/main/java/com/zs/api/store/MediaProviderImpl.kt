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

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class MediaProviderImpl(
    context: Context
) : MediaProvider {

    val contentResolver = context.contentResolver

    override suspend fun getFiles(
        filter: String?,
        order: String,
        ascending: Boolean,
        parent: String?,
        offset: Int,
        limit: Int
    ): List<Media> = contentResolver.getMediaFiles(
        filter, order, ascending, parent, offset, limit
    )

    override suspend fun getFolders(
        filter: String?,
        ascending: Boolean,
        offset: Int,
        limit: Int
    ): List<Folder> = contentResolver.getFolders(filter, ascending, offset, limit)

    override fun register(uri: Uri, onChanged: () -> Unit): ContentObserver =
        contentResolver.register(uri, onChanged)

    override fun observer(uri: Uri) = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(selfChange)
            }
        }
        contentResolver.registerContentObserver(uri, true, observer)
        // trigger first.
        trySend(false)
        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }
}