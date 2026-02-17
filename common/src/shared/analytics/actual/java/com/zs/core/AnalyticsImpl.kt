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

package com.zs.core

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.zs.core.analytics.Analytics

/**
 * Implementation of the [Analytics] interface using Firebase Analytics and Crashlytics.
 * @property crashlytics Instance of Firebase Crashlytics for crash reporting.
 * @property analytics Instance of Firebase Analytics for event logging.
 */
internal class AnalyticsImpl : Analytics() {

    lateinit var crashlytics: FirebaseCrashlytics
    lateinit var analytics: FirebaseAnalytics

    private val TAG = "AnalyticsImpl"

    override fun record(throwable: Throwable) {
        crashlytics.recordException(throwable)
        Log.d(TAG, "record: ${throwable.stackTrace}")
    }

    override fun logEvent(name: String, params: Bundle) {
        analytics.logEvent(name, params)
        Log.d(TAG, "logEvent: name: $name, extra: $params")
    }

    override fun initialize(context: Context) {
        Log.d(TAG, "initialize")
        FirebaseApp.initializeApp(context.applicationContext)
        crashlytics = Firebase.crashlytics
        analytics = Firebase.analytics
    }
}