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

package com.zs.common.analytics

import android.content.Context
import android.os.Bundle
import androidx.annotation.Size

/**
 * Interface for the analytics module. This allows for decoupling the core module from
 * the specific analytics implementation.
 */
interface Analytics {
    /**
     * Records a [Throwable] for error tracking.
     * @param throwable The throwable to record.
     */
    fun record(throwable: Throwable)

    /**
     * Logs an analytics event.
     * @param name The name of the event (must be between 1 and 40 characters).
     * @param params A [Bundle] containing the parameters for the event.
     */
    fun logEvent(@Size(min = 1L, max = 40L) name: String, params: Bundle)


    companion object {

        const val EVENT_SCREEN_VIEW: String = "screen_view"
        const val PARAM_SCREEN_NAME: String = "screen_name"

        /**
         * Factory entry point for [Analytics].
         *
         * Returns the appropriate implementation based on the current
         * build flavor. Standard builds use [AnalyticsImpl], while all
         * other flavors fall back to [FallbackAnalytics].
         *
         * @param context Application context used to initialize analytics.
         * @return An [Analytics] implementation for the active flavor.
         */
        operator fun invoke(context: Context) = AnalyticsImpl(context)
    }
}