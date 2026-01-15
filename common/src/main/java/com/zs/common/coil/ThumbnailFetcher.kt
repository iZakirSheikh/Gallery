/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 15 of Jan 2026
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
 * Last Modified by sheik on 15 of Jan 2026
 */

package com.zs.common.coil

import coil3.ImageLoader
import coil3.Uri
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.request.Options

class ThumbnailFetcher(
    private val data: Uri,
    private val options: Options
) : Fetcher {
    companion object:  Fetcher.Factory<Uri>{

        fun buildThumbnailUri(mediaId: String, mimeType: String): Uri {
            return Uri("fileID", "local", mediaId, mimeType, null)
        }

        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            TODO("Not yet implemented")
        }
    }

    override suspend fun fetch(): FetchResult? {
        TODO("Not yet implemented")
    }
}