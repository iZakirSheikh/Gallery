package com.zs.core.appstore

import android.app.Activity
import kotlinx.coroutines.flow.Flow

/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by shaikh on 17 of Feb 2026
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
 *
 */

interface AppStoreManager {



    fun launchReviewFlow(activity: Activity)


    fun requestUpdateFlow(activity: Activity): Flow<Float>

    fun startFlexibleUpdate(activity: Activity, requestCode: Int):  Boolean
    fun startImmediateUpdate(activity: Activity, requestCode: Int): Boolean

    /**
     * Returns the number of days since the app's last update or -1
     */
    fun clientVersionStalenessDays(): Int

    /**
     * Starts updating the app.
     */
    fun completeUpdate()

    companion object {

        const val UPDATE_NOT_AVAILABLE = -1
        const val UPDATE_DOWNLOADED = -2
        const val UPDATE_AVAILABLE = -3

    }

}