/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
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
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

package com.zs.gallery.common

import android.os.Build


object AppConfig {
    @JvmField var isBackgroundBlurEnabled: Boolean = Res.manifest.isAtLeast(Build.VERSION_CODES.S)
    @JvmField var isTrashCanEnabled: Boolean = true
    @JvmField var fontScale: Float = Float.NaN
    @JvmField var gridItemSizeMultiplier: Float = 1.0f
    @JvmField var isFileGroupingEnabled: Boolean = true
    @JvmField var lockTimeoutMinutes: Int = Int.MIN_VALUE
    @JvmField var isLiveGalleryEnabled: Boolean = false
    @JvmField var isAppSecureModeEnabled: Boolean = false


    private fun encode(): String {
        return ""
    }

    fun restore(value: String?){}
}