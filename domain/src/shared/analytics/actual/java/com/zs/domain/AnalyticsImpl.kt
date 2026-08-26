package com.zs.domain

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.zs.domain.analytics.Analytics

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