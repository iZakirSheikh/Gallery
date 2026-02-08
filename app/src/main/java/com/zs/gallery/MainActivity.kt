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

@file:OptIn(ExperimentalFoundationApi::class)

package com.zs.gallery

import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.view.WindowManager.LayoutParams
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.zs.common.analytics.Analytics
import com.zs.common.util.checkSelfPermissions
import com.zs.common.util.showPlatformToast
import com.zs.compose.foundation.getText2
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.gallery.common.AppConfig
import com.zs.gallery.common.NavController
import com.zs.gallery.common.NavKey
import com.zs.gallery.common.Navigator
import com.zs.gallery.common.Res
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.versionCodeCompat
import com.zs.preferences.Preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.minutes
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen as configSplashScreen
import com.zs.compose.theme.snackbar.SnackbarHostState as SnackbarController

class MainActivity : ComponentActivity(), SystemFacade {
    // TAG - for debugging purpose only
    private val TAG = "MainActivity"

    val preferences: Preferences by inject()
    val controller: SnackbarController by inject()
    private var navController: NavController? = null
    private val analytics: Analytics by inject()


    var inAppUpdateProgress by mutableFloatStateOf(Float.NaN)
        private set

    /**
     * Timestamp (mills) indicating when the app last went to the background.
     *
     * Possible values:
     * - `-1L`: The app has just started and hasn't been in the background yet.
     * - `0L`: The app was launched for the first time (initial launch).
     * - `> 0L`: The time in milliseconds when the app last entered the background.
     */
    private var timeAppWentToBackground = -1L

    /**
     * Checks if authentication is required.
     *
     * Authentication is not supported on Android versions below P.
     *
     * @return `true` if authentication should be shown, `false` otherwise.
     */
    private val isAuthenticationRequired: Boolean
        get() {
            // App lock is not supported on Android versions below P.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                return false
            }

            // If the timestamp is 0L, the user has recently unlocked the app,
            // so authentication is not required.
            if (timeAppWentToBackground == 0L) {
                return false
            }

            // Check the app lock timeout setting.
            return when (val timeoutValue = AppConfig.lockTimeoutMinutes) {
                -1 -> false // App lock is disabled (timeout value of -1)
                0 -> true // Immediate authentication required (timeout value of 0)
                else -> {
                    // Calculate the time elapsed since the app went to background.
                    val currentTime = System.currentTimeMillis()
                    val timeSinceBackground = currentTime - timeAppWentToBackground
                    timeSinceBackground >= timeoutValue.minutes.inWholeMilliseconds
                }
            }
        }

    override fun onPause() {
        super.onPause()
        // The time when app went to background.
        // irrespective of what value it holds update it.
        Log.d(TAG, "onPause")
        timeAppWentToBackground = System.currentTimeMillis()
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onStart")
        // Only navigate to the lock screen if authentication is required and
        // this is not a fresh app start.

        // On a fresh start, timeAppWentToBackground is -1L.
        // If authentication is required on a fresh start, the app will be
        // automatically navigated to the lock screen in onCreate().
        if (timeAppWentToBackground != -1L && isAuthenticationRequired) {
            Log.d(TAG, "onResume: navigating -> RouteLockScreen.")
            // since navController doesn't support adding new dest at the bottom of topMost dest;
            // remove current destination to insert lock screen below
            val current = navController?.active
            if (current is NavKey.Viewer && current.isIntentViewer) {
                navController?.popBackStack()
            }
            navController?.navigate(NavKey.Lockscreen)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun showToast(message: String, duration: Int) = showPlatformToast(message, duration)
    override fun showToast(message: Int, duration: Int) = showPlatformToast(message, duration)
    override fun <T> getDeviceService(name: String): T = getSystemService(name) as T
    override fun launch(intent: Intent, options: Bundle?) = startActivity(intent, options)

    @RequiresApi(Build.VERSION_CODES.P)
    override fun authenticate(
        subtitle: String?,
        desc: String?,
        onAuthenticated: () -> Unit,
    ) {
        Log.d(TAG, "preparing to show authentication dialog.")
        // Build the BiometricPrompt
        val prompt = BiometricPrompt.Builder(this).apply {
            setTitle(getString(R.string.lock_scr_title))
            if (subtitle != null) setSubtitle(subtitle)
            if (desc != null) setDescription(desc)
            // Set allowed authenticators for Android R and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            // Allow device credential fallback for Android Q
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
                setDeviceCredentialAllowed(true)
            // On Android P and below, BiometricPrompt crashes if a negative button is not set.
            // We provide a "Dismiss" button to avoid the crash, but this does not offer alternative
            // authentication (like PIN).
            // Future versions might include support for alternative authentication on older Android versions
            // if a compatibility library or API becomes available.
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                setNegativeButton(getString(R.string.dismiss), mainExecutor, { _, _ -> })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                setConfirmationRequired(false)
            /*if (Build.VERSION.SDK_INT >= 35) {
                setLogoRes(R.drawable.ic_app)
            }*/
        }.build()
        // Start the authentication process
        prompt.authenticate(
            CancellationSignal(),
            mainExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                // Implement callback methods for authentication events (success, error, etc.)
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    onAuthenticated()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast(getString(R.string.msg_auth_failed))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    showToast(getString(R.string.msg_auth_error_s, errString))
                }
            }
        )
    }

    @SuppressLint("NewApi")
    override fun unlock() = authenticate() {
        val navController = navController ?: return@authenticate
        // if it is initial app_lock update timeAppWentToBackground to 0
        if (timeAppWentToBackground == -1L) timeAppWentToBackground = 0L
        // Check if the start destination needs to be updated
        // Update the start destination to RouteTimeline
        if (navController.active == NavKey.Lockscreen) {
            Log.d(TAG, "unlock: updating start destination")
            navController.rebase(NavKey.Files())
            // return from here;
            return@authenticate
        }
        Log.d(TAG, "unlock: poping lock_screen from graph")
        // If the start destination is already RouteTimeline, just pop back
        // TODO - Keep an eye on here.
        navController.popBackStack()
    }

    override fun showSnackbar(
        message: CharSequence,
        icon: ImageVector?,
        accent: Color,
        duration: SnackbarDuration,
    ) {
        lifecycleScope.launch {
            controller.showSnackbar(
                message = message,
                icon = icon,
                accent = accent,
                duration = duration
            )
        }
    }

    override fun showSnackbar(
        message: Int,
        icon: ImageVector?,
        accent: Color,
        duration: SnackbarDuration,
    ) = showSnackbar(
        resources.getText2(id = message),
        icon = icon,
        accent = accent,
        duration = duration
    )

    override fun initiateUpdateFlow(report: Boolean) {
        TODO("Not yet implemented")
    }

    override fun initiateReviewFlow() {
        TODO("Not yet implemented")
    }

    override fun initiatePurchaseFlow(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun restart(global: Boolean) {
        // Get the launch intent for the app's main activity
        val packageManager = packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)

        // Ensure the intent is not null
        if (intent == null) {
            //analytics.logEvent()
            Log.e("AppRestart", "Unable to restart: Launch intent is null")
            return
        }

        // Get the main component for the restart task
        val componentName = intent.component
        if (componentName == null) {
            Log.e("AppRestart", "Unable to restart: Component name is null")
            return
        }
        // Create the main restart intent and start the activity
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        startActivity(mainIntent)
        // Terminate the current process to complete the restart
        if (global) Runtime.getRuntime().exit(0)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action != Intent.ACTION_VIEW) return
        lifecycleScope.launch {
            // we delay it here because on resume loads lockscreen.
            // we want this to overlay over lockscreen; hence this.

            delay(200)
            navController?.navigate(NavKey.Viewer(intent.data!!, intent.type))
        }
    }

    override fun configSystemBars(isLightAppearance: Boolean, visible: Boolean, isBgTransparent: Boolean) {
        // Obtain a controller to manage system bar visibility and appearance
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Apply light/dark icon appearance for both navigation and status bars
        controller.isAppearanceLightNavigationBars = isLightAppearance
        controller.isAppearanceLightStatusBars = isLightAppearance

        // Configure background color or contrast enforcement depending on API level
        if (Build.VERSION.SDK_INT >= 29) {
            // On Android 10+, enforce contrast if background is not transparent
            window.setStatusBarContrastEnforced(!isBgTransparent)
            window.setNavigationBarContrastEnforced(!isBgTransparent)
        } else {
            // On older versions, fallback to setting a semi-transparent scrim or fully transparent color
            val color = if (isBgTransparent) Color.Transparent else Color(0x20000000)
            window.navigationBarColor = color.toArgb()
            window.statusBarColor = color.toArgb()
        }

        if (!visible) {
            // Hide system bars and allow them to be revealed temporarily by swipe
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            return
        }

        // Show system bars with default behavior
        controller.show(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Determine if this is a fresh launch (no saved state from rotation or process recreation)
        val isFreshLaunch = savedInstanceState == null
        if (isFreshLaunch) {
            // Show splash screen only on a fresh launch
            configSplashScreen()

            // Apply secure mode if user has enabled it in settings.
            // FLAG_SECURE prevents screenshots and screen recording of the app’s UI.
            if (AppConfig.isAppSecureModeEnabled) {
                window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)
            }

            // TODO - ⚠️ NOTE: We currently rely on versionCode from PackageManager.
            // Monitor this carefully, as Play Console may raise issues related
            // to permission handling when querying app packages.
            lifecycleScope.launch {
                val info = packageManager.getPackageInfo(packageName, 0)

                // Compare stored version code with the current one.
                // If different, update preferences and notify user with release notes.
                if (info.versionCodeCompat != preferences[Res.key.app_version_code]) {
                    preferences[Res.key.app_version_code] = info.versionCodeCompat
                    // Show release notes snackbar to highlight new version changes.
                    showSnackbar(
                        message = Res.string.release_notes,
                        accent = Color.Unspecified,
                        icon = null,
                        duration = SnackbarDuration.Long
                    )

                    // Optionally log analytics event for release notes display.
                    // analytics.logEvent("release_notes")

                    return@launch
                }
            }
        }

        // Ensure window fits system windows (status bar, navigation bar).
        // This is usually configured in the app theme, but enforced here for consistency.
        WindowCompat.enableEdgeToEdge(window)

        // Create navigator controller.
        // Decide initial route based on intent, permissions, and authentication requirements.
        if (navController == null || isFreshLaunch) {
            // Decide initial route based on launch context, permissions, and authentication:
            // - Deep link or external intent → IntentViewer
            // - Missing required permissions → Onboarding
            // - Authentication required → ScreenLock
            // - Default case → Files browser
            navController = when {
                intent.action == Intent.ACTION_VIEW -> Navigator(
                    NavKey.Viewer(
                        intent.data!!, intent.type
                    )
                )

                !checkSelfPermissions(Res.app.permissions) -> Navigator(NavKey.AppIntro)
                isAuthenticationRequired -> Navigator(NavKey.Lockscreen)
                else -> Navigator(NavKey.Files())
            }
        }

        // Set the main UI content with navigator and controller
        setContent { Gallery(navController!!, controller) }
    }
}