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
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

/**
 * Implementation of the [Analytics] interface using Firebase Analytics and Crashlytics.
 * @property crashlytics Instance of Firebase Crashlytics for crash reporting.
 * @property analytics Instance of Firebase Analytics for event logging.
 */
class AnalyticsImpl : Analytics {
    // Tag used for logging.
    private val TAG = "AnalyticsImpl"

    val crashlytics: FirebaseCrashlytics
    val analytics: FirebaseAnalytics

    /**
     * Constructs an [AnalyticsImpl] instance.
     * Initializes Firebase App, Analytics, and Crashlytics.
     *
     * @param context The application context.
     */
    constructor(context: Context) {
        FirebaseApp.initializeApp(context.applicationContext)
        Log.d(TAG, "Firebase initialized with Application context")
        Log.d(TAG, "${FirebaseApp.getInstance()}")
        analytics = Firebase.analytics
        crashlytics = Firebase.crashlytics.also {
            it.isCrashlyticsCollectionEnabled = true
        }
    }

    override fun record(throwable: Throwable) {
        // Record the exception using Crashlytics.
        crashlytics.recordException(throwable)
        // Log the exception message.
        Log.d(TAG, "record: ${throwable.message}")
    }

    override fun logEvent(name: String, params: Bundle) {
        analytics.logEvent(name, params)
        // Firebase Analytics automatically logs the event.
    }
}