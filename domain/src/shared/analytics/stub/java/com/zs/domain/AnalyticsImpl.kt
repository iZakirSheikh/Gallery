package com.zs.domain

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.zs.domain.analytics.Analytics

internal class AnalyticsImpl : Analytics() {
    private val TAG = "AnalyticsImpl"
    override fun record(throwable: Throwable) {
        Log.e(TAG, "record: ${throwable.stackTrace}")
    }

    override fun logEvent(name: String, params: Bundle) {
        Log.i(TAG, "name:$name, params = $params")
    }

    override fun initialize(context: Context) {
        Log.i(TAG, "Analytics fallback initialized (no telemetry backend)")
        /* STUB: No initialization required */
    }
}