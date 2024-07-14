/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 12-07-2024.
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

package com.zs.gallery.impl

import com.zs.api.store.Media
import com.zs.gallery.files.FilesViewState
import kotlinx.coroutines.flow.Flow

class FilesViewModel: AbstractViewModel(), FilesViewState {

    override val files: Flow<Media>
        get() = TODO("Not yet implemented")

    override fun delete(vararg file: Media, trash: Boolean) {
        TODO("Not yet implemented")
    }
}