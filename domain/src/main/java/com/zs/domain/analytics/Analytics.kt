package com.zs.domain.analytics

import android.content.Context
import android.os.Bundle
import androidx.annotation.Size
import com.zs.domain.AnalyticsImpl
import com.zs.domain.analytics.Analytics.Companion.getInstance

/**
 * Analytics interface for application event and error tracking.
 *
 * Provides a flavor‑specific contract for logging events and recording
 * exceptions. This abstraction decouples the core module from concrete
 * analytics implementations (e.g., Firebase Analytics, Crashlytics, or
 * a fallback logger).
 *
 * <p>Usage notes:</p>
 * - Call [initialize] once during application startup.
 * - Use [logEvent] to record user interactions and custom events.
 * - Use [record] to capture non‑fatal exceptions or errors.
 * - Access the singleton via [getInstance]; only one instance is active
 *   per process.
 *
 * <p>Flavor behavior:</p>
 * - **Standard / Plus ** → Backed by Firebase Analytics and Crashlytics.
 * - **Community (FOSS) / Premium** → Uses Android [Log] as a fallback, with no telemetry.
 *
 * This design ensures privacy‑respecting builds while maintaining
 * consistent APIs across all editions.
 */
abstract class Analytics {
    /**
     * Records a [Throwable] for error tracking.
     * @param throwable The throwable to record.
     */
    abstract fun record(throwable: Throwable)

    /**
     * Logs an analytics event.
     * @param name The name of the event (must be between 1 and 40 characters).
     * @param params A [Bundle] containing the parameters for the event.
     */
    abstract fun logEvent(@Size(min = 1L, max = 40L) name: String, params: Bundle)

    /**
     * Initializes the analytics implementation.
     *
     * @param context The application [Context] used for setup.
     */
    internal abstract fun initialize(context: Context)

    companion object {
        const val EVENT_SCREEN_VIEW: String = "screen_view"
        const val PARAM_SCREEN_NAME: String = "screen_name"

        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: Analytics? = null

        /**
         * Returns the singleton [Analytics] instance.
         * If the instance is not yet created, it initializes a new [AnalyticsImpl].
         **/
        fun getInstance(): Analytics {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = AnalyticsImpl()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        /**
         * Initializes the singleton [Analytics] instance.
         *
         * @param context The application [Context] used for setup.
         */
        fun initialize(context: Context) = getInstance().initialize(context)
    }
}