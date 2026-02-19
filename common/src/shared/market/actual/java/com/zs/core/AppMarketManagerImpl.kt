/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 02-10-2024.
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

package com.zs.core

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.review.ReviewManagerFactory
import com.zs.core.market.AppMarketManager
import kotlinx.coroutines.flow.onEach

internal class AppMarketManagerImpl() : AppMarketManager {
    override suspend fun initiateReviewFlow(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val info = reviewManager.requestReview()
        reviewManager.launchReviewFlow(activity, info)
    }

    override suspend fun initiateUpdateFlow(
        activity: Activity,
        provider: suspend (result: Float) -> Int
    ) {
        val manager = AppUpdateManagerFactory.create(activity)
        manager.requestUpdateFlow().onEach { result ->
            when(result){
                is AppUpdateResult.NotAvailable -> provider(AppMarketManager.UPDATE_NOT_AVAILABLE)
                is AppUpdateResult.InProgress -> {
                    val state = result.installState
                    val total = state.totalBytesToDownload()
                    val downloaded = state.bytesDownloaded()
                    val progress = when {
                        total <= 0 -> -1f
                        total == downloaded -> Float.NaN
                        else -> downloaded / total.toFloat()
                    }
                    provider(progress)
                }
                is AppUpdateResult.Available -> {
                    // if user choose to skip the update handle that case also.
                    val isFlexible = (result.updateInfo.clientVersionStalenessDays()
                        ?: -1) <= AppMarketManager.FLEXIBLE_UPDATE_MAX_STALENESS_DAYS
                    if (isFlexible) result.startFlexibleUpdate(
                        activity = activity, 1000
                    )
                    else result.startImmediateUpdate(
                        activity = activity, 1000
                    )

                }
                is AppUpdateResult.Downloaded -> {
                    val info = manager.requestAppUpdateInfo()
                    //when update first becomes available
                    //don't force it.
                    // make it required when staleness days overcome allowed limit
                    val isFlexible = (info.clientVersionStalenessDays()
                        ?: -1) <= AppMarketManager.FLEXIBLE_UPDATE_MAX_STALENESS_DAYS

                    // forcefully update; if it's flexible
                    if (!isFlexible) {
                        manager.completeUpdate()
                        return@onEach
                    }

                    val action = provider(AppMarketManager.UPDATE_DOWNLOADED)
                    if (action == AppMarketManager.ACTION_INSTALL)
                        manager.completeUpdate()
                }
            }
        }
    }
}