package com.zs.domain.ads

import android.content.Context
import com.zs.domain.AdInitializerImpl

interface AdManager {


    companion object {
        /**
         * Initializes the third-party ad SDK.
         *
         * @param context The application context.
         * @param id The unique application ID for the ad SDK.
         */
         fun initialize(context: Context, id: String): Unit =
             AdInitializerImpl().initialize(context, id)
    }
}