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

package com.zs.gallery.common.impl

import androidx.paging.PagingData
import com.zs.common.db.media.MediaProvider
import com.zs.common.db.media.Snapshot
import com.zs.gallery.common.NavKey
import com.zs.gallery.files.FilesViewState
import kotlinx.coroutines.flow.Flow

class FilesViewModel(val key: NavKey.Files, val provider: MediaProvider) : KoinViewModel(), FilesViewState{
    override val data: Flow<PagingData<Snapshot>>
        get() = TODO("Not yet implemented")


}