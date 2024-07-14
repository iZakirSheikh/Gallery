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

import com.zs.api.util.PathUtils

/**
 * Represents a folder with associated properties.
 *
 * @property artwork The artwork associated with the folder.
 * @property path The path of the folder.
 * @property count The count of items within the folder.
 * @property size The size of the folder in bytes.
 */
data class Folder(
    @JvmField val artwork: String,
    @JvmField val path: String,
    @JvmField val count: Int,
    @JvmField val size: Int,
    @JvmField val lastModified: Long
) {
    val name: String get() = PathUtils.name(path)
}