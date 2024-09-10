/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 20-07-2024.
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

package com.zs.gallery

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.primex.core.MetroGreen
import com.primex.core.getText2
import com.primex.core.runCatching
import com.primex.preferences.Key
import com.primex.preferences.Preferences
import com.primex.preferences.longPreferenceKey
import com.primex.preferences.observeAsState
import com.primex.preferences.value
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.calculateWindowSizeClass
import com.zs.foundation.toast.Toast
import com.zs.foundation.toast.ToastHostState
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.getPackageInfoCompat
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.days

private const val TAG = "MainActivity"

// In-app update and review settings

// Maximum staleness days allowed for a flexible update.
// If the app is older than this, an immediate update will be enforced.
private const val FLEXIBLE_UPDATE_MAX_STALENESS_DAYS = 2

// Minimum number of app launches before prompting for a review.
private const val MIN_LAUNCHES_BEFORE_REVIEW = 5

// Number of days to wait before showing the first review prompt.
private val INITIAL_REVIEW_DELAY = 3.days

// Minimum number of days between subsequent review prompts.
// Since we cannot confirm if the user actually left a review, we use this interval
// to avoid prompting too frequently.
private val STANDARD_REVIEW_DELAY = 5.days

private val KEY_LAST_REVIEW_TIME =
    longPreferenceKey(TAG + "_last_review_time", 0)

/**
 * Manages SplashScreen
 */
context(ComponentActivity)
private fun configureSplashScreen(isColdStart: Boolean) {
    // Installs the Splash Screen provided by the SplashScreen compat library
    installSplashScreen().let { screen ->
        // Only animate the exit if this is a cold start of the app
        if (!isColdStart) return@let
    }
}

/**
 * @property inAppUpdateProgress A simple property that represents the progress of the in-app update.
 *        The progress value is a float between 0.0 and 1.0, indicating the percentage of the
 *        update that has been completed. The Float.NaN represents a default value when no update
 *        is going on.
 */
class MainActivity : ComponentActivity(), SystemFacade {
    private val toastHostState: ToastHostState by inject()
    private val preferences: Preferences by inject()
    private val inAppUpdateProgress = mutableFloatStateOf(Float.NaN)

    override fun enableEdgeToEdge(
        hide: Boolean?,
        translucent: Boolean?,
        dark: Boolean?
    ) {
        // Get the WindowInsetsController for managing system bars
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        // ensure edge to edge is enabled
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // If hide is true, hide the system bars and return
        if (hide == true)
            return controller.hide(WindowInsetsCompat.Type.systemBars())
        // Otherwise, show the system bars
        controller.show(WindowInsetsCompat.Type.systemBars())
        // Determine translucency based on provided value or preference
        val translucent = translucent ?: !preferences.value(Settings.KEY_TRANSPARENT_SYSTEM_BARS)
        // Set the color for status and navigation bars based on translucency
        val color = when (translucent) {
            false -> Color.Transparent.toArgb()
            else -> Color(0x20000000).toArgb()
        }
        // Set the status and navigation bar colors
        window.statusBarColor = color
        window.navigationBarColor = color
        // Determine dark mode based on provided value, preference, or system setting
        val isAppearanceDark = dark ?: preferences.value(Settings.KEY_NIGHT_MODE).let {
            when (it) {
                NightMode.YES -> false
                NightMode.NO -> true
                NightMode.FOLLOW_SYSTEM -> {
                    val config = resources.configuration
                    val uiMode = config.uiMode
                    Log.d(TAG, "configureSystemBars: $uiMode")
                    (uiMode and Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES
                }
            }
        }
        // Set the appearance of system bars based on dark mode
        controller.isAppearanceLightStatusBars = isAppearanceDark
        controller.isAppearanceLightNavigationBars = isAppearanceDark
    }

    override fun showToast(
        message: CharSequence,
        icon: ImageVector?,
        accent: Color,
        action: CharSequence?,
        duration: Int
    ) {
        lifecycleScope.launch {
            toastHostState.showToast(message, action, icon, accent, duration)
        }
    }

    override fun showToast(
        message: Int,
        icon: ImageVector?,
        accent: Color,
        action: Int,
        duration: Int
    ) {
        lifecycleScope.launch {
            val action = if (action == ResourcesCompat.ID_NULL) null else resources.getText2(action)
            toastHostState.showToast(
                resources.getText2(id = message),
                action,
                icon,
                accent,
                duration
            )
        }
    }

    @Composable
    @NonRestartableComposable
    override fun <S, O> observeAsState(key: Key.Key1<S, O>) =
        preferences.observeAsState(key = key)

    @Composable
    @NonRestartableComposable
    override fun <S, O> observeAsState(key: Key.Key2<S, O>) =
        preferences.observeAsState(key = key)

    override fun launch(intent: Intent, options: Bundle?) = startActivity(intent, options)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // caused the change in config. means the system bars need to be configured again
        // but what would happen if the system bars are already configured?
        enableEdgeToEdge()
    }

    private fun initialize() {
        if (preferences.value(Settings.KEY_SECURE_MODE)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        val flow1 = preferences[Settings.KEY_NIGHT_MODE]
        val flow2 = preferences[Settings.KEY_TRANSPARENT_SYSTEM_BARS]
        flow1.combine(flow2) { _, _ -> enableEdgeToEdge() }
            .launchIn(scope = lifecycleScope)
        lifecycleScope.launch { launchUpdateFlow() }
    }

    override fun launchUpdateFlow(report: Boolean) {
        val manager = AppUpdateManagerFactory.create(this@MainActivity)
        manager.requestUpdateFlow().onEach { result ->
            when (result) {
                is AppUpdateResult.NotAvailable -> if (report) showToast(R.string.msg_update_not_available)
                is AppUpdateResult.InProgress -> {
                    val state = result.installState
                    val total = state.totalBytesToDownload()
                    val downloaded = state.bytesDownloaded()
                    val progress = when {
                        total <= 0 -> -1f
                        total == downloaded -> Float.NaN
                        else -> downloaded / total.toFloat()
                    }
                    inAppUpdateProgress.floatValue = progress
                }

                is AppUpdateResult.Downloaded -> {
                    val info = manager.requestAppUpdateInfo()
                    //when update first becomes available
                    //don't force it.
                    // make it required when staleness days overcome allowed limit
                    val isFlexible = (info.clientVersionStalenessDays()
                        ?: -1) <= FLEXIBLE_UPDATE_MAX_STALENESS_DAYS

                    // forcefully update; if it's flexible
                    if (!isFlexible) {
                        manager.completeUpdate()
                        return@onEach
                    }
                    // else show the toast.
                    val res = toastHostState.showToast(
                        message = resources.getText2(R.string.msg_new_update_downloaded),
                        action = resources.getText2(R.string.install),
                        duration = Toast.DURATION_INDEFINITE,
                        accent = Color.MetroGreen,
                        icon = Icons.Outlined.Downloading
                    )
                    // complete update when ever user clicks on action.
                    if (res == Toast.ACTION_PERFORMED) manager.completeUpdate()
                }

                is AppUpdateResult.Available -> {
                    // if user choose to skip the update handle that case also.
                    val isFlexible = (result.updateInfo.clientVersionStalenessDays()
                        ?: -1) <= FLEXIBLE_UPDATE_MAX_STALENESS_DAYS
                    if (isFlexible) result.startFlexibleUpdate(
                        activity = this@MainActivity, 1000
                    )
                    else result.startImmediateUpdate(
                        activity = this@MainActivity, 1000
                    )
                    // no message needs to be shown
                }
            }
        }.catch {
            Firebase.crashlytics.recordException(it)
            if (!report) return@catch
            showToast(R.string.msg_update_check_error)
        }.launchIn(lifecycleScope)
    }

    override fun launchReviewFlow() {
        lifecycleScope.launch {
            // Get the app launch count from preferences.
            val count = preferences.value(Settings.KEY_LAUNCH_COUNTER)
            // Check if the minimum launch count has been reached.
            if (count < MIN_LAUNCHES_BEFORE_REVIEW)
                return@launch
            // Get the first install time of the app.
            // Check if enough time has passed since the first install.
            val firstInstallTime =
                packageManager.getPackageInfoCompat(BuildConfig.APPLICATION_ID)?.firstInstallTime ?: 0
            val currentTime = System.currentTimeMillis()
            if (currentTime - firstInstallTime < INITIAL_REVIEW_DELAY.inWholeMilliseconds)
                return@launch
            // Get the last time the review prompt was shown.
            // Check if enough time has passed since the last review prompt.
            val lastAskedTime = preferences.value(KEY_LAST_REVIEW_TIME)
            if (currentTime - lastAskedTime <= STANDARD_REVIEW_DELAY.inWholeMilliseconds)
                return@launch

            // Request and launch the review flow.
            runCatching(TAG) {
                val reviewManager = ReviewManagerFactory.create(this@MainActivity)
                // Update the last asked time in preferences
                preferences[KEY_LAST_REVIEW_TIME] = System.currentTimeMillis()
                val info = reviewManager.requestReview()
                reviewManager.launchReviewFlow(this@MainActivity, info)
                // Optionally log an event to Firebase Analytics.
                // host.fAnalytics.logReviewPromptShown()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app has started from scratch if savedInstanceState is null.
        val isColdStart = savedInstanceState == null //why?

        // Manage SplashScreen
        configureSplashScreen(isColdStart)
        // init the heavy duty tasks in initialize()
        initialize()
        // Configure system bars
        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)
            CompositionLocalProvider(
                LocalWindowSize provides windowSizeClass,
                LocalElevationOverlay provides null,  // Disable absolute elevation.
                LocalSystemFacade provides this,
                content = { Home(toastHostState) }
            )
        }
    }
}