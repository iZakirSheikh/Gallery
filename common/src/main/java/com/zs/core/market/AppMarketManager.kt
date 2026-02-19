package com.zs.core.market

import android.app.Activity
import com.zs.core.AppMarketManagerImpl

interface AppMarketManager {

    companion object {
        const val UPDATE_NOT_AVAILABLE = -1f
        const val UPDATE_DOWNLOADED = -2f
        const val UPDATE_NOT_SUPPORTED = -4f

        // In-app update and review settings
        // Maximum staleness days allowed for a flexible update.
        // If the app is older than this, an immediate update will be enforced.
        const val FLEXIBLE_UPDATE_MAX_STALENESS_DAYS = 2

        const val ACTION_IGNORE = 0
        const val ACTION_INSTALL = 1

        operator fun invoke(): AppMarketManager = AppMarketManagerImpl()
    }


    suspend fun initiateReviewFlow(activity: Activity)
    suspend fun initiateUpdateFlow(activity: Activity, provider: suspend (result: Float) -> Int)
}