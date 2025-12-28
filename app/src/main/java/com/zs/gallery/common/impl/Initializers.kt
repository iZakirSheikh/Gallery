/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 28 of Dec 2025
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
 * Last Modified by sheik on 28 of Dec 2025
 *
 */

package com.zs.gallery.common.impl

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.zs.common.analytics.Analytics

private const val TAG = "Initializers"

class AnalyticsInitializer : Initializer<Unit> {
    override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
    override fun create(context: Context) {
        Log.d(TAG, "initializing firebase: ")
        Analytics.initialize(context)
    }
}