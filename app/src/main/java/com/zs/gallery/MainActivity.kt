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

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import com.primex.core.MetroGreen
import com.primex.core.getText2
import com.primex.core.runCatching
import com.primex.preferences.Key
import com.primex.preferences.Preferences
import com.primex.preferences.longPreferenceKey
import com.primex.preferences.observeAsState
import com.primex.preferences.value
import com.zs.foundation.toast.Toast
import com.zs.foundation.toast.ToastHostState
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.domain
import com.zs.gallery.common.getPackageInfoCompat
import com.zs.gallery.files.RouteTimeline
import com.zs.gallery.lockscreen.RouteLockScreen
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import android.widget.Toast as PlatformToast

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
 * @property inAppUpdateProgress A simple property that represents the progress of the in-app update.
 *        The progress value is a float between 0.0 and 1.0, indicating the percentage of the
 *        update that has been completed. The Float.NaN represents a default value when no update
 *        is going on.
 * @property timeAppWentToBackground The time in mills until the app was in background state. default value -1L
 * @property isAuthenticationRequired A boolean flag indicating whether authentication is required.
 */
class MainActivity : ComponentActivity(), SystemFacade, NavController.OnDestinationChangedListener {
    private val toastHostState: ToastHostState by inject()
    private val preferences: Preferences by inject()
    private var navController: NavHostController? = null
    private val inAppUpdateProgress = mutableFloatStateOf(Float.NaN)

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
            return when (val timeoutValue = preferences.value(Settings.KEY_APP_LOCK_TIME_OUT)) {
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
            navController?.navigate(RouteLockScreen()) {
                launchSingleTop = true
            }
            unlock()
        }
    }

    override fun showPlatformToast(string: Int) =
        PlatformToast.makeText(this, getString(string), PlatformToast.LENGTH_SHORT).show()

    override fun showPlatformToast(string: String) =
        PlatformToast.makeText(this, string, PlatformToast.LENGTH_SHORT).show()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun authenticate(subtitle: String?, desc: String?, onAuthenticated: () -> Unit) {
        Log.d(TAG, "preparing to show authentication dialog.")
        // Build the BiometricPrompt
        val prompt = BiometricPrompt.Builder(this).apply {
            setTitle(getString(R.string.scr_lock_screen_title))
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
                setNegativeButton("Dismiss", mainExecutor, { _, _ -> })
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
                    showPlatformToast("Authentication Successful! Welcome back.")
                    onAuthenticated()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showPlatformToast("Authentication Failed! Please try again or use an alternative method.")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    showPlatformToast("Authentication Error: $errString")
                }
            }
        )
    }

    override fun <T> getDeviceService(name: String): T =
        getSystemService(name) as T

    @SuppressLint("NewApi")
    override fun unlock() = authenticate() {
        val navController = navController ?: return@authenticate
        // if it is initial app_lock update timeAppWentToBackground to 0
        if (timeAppWentToBackground == -1L)
            timeAppWentToBackground = 0L
        // Check if the start destination needs to be updated
        // Update the start destination to RouteTimeline
        if (navController.graph.startDestinationRoute == RouteLockScreen()) {
            Log.d(TAG, "unlock: updating start destination")
            navController.graph.setStartDestination(RouteTimeline())
            navController.navigate(RouteTimeline()) {
                popUpTo(RouteLockScreen()) {
                    inclusive = true
                }
            }
            // return from here;
            return@authenticate
        }
        Log.d(TAG, "unlock: poping lock_screen from graph")
        // If the start destination is already RouteTimeline, just pop back
        navController.popBackStack()
    }

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
        val translucent =
            translucent ?: !preferences.value(Settings.KEY_TRANSPARENT_SYSTEM_BARS)
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
            val action =
                if (action == ResourcesCompat.ID_NULL) null else resources.getText2(action)
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
        Log.d(TAG, "onConfigurationChanged: ${navController?.currentDestination?.route}")
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
        if (isAuthenticationRequired)
            lifecycleScope.launch {
                delay(1000)
                unlock()
            }
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
                packageManager.getPackageInfoCompat(BuildConfig.APPLICATION_ID)?.firstInstallTime
                    ?: 0
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

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        // Log the event.
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            // create params for the event.
            val domain = destination.domain ?: "unknown"
            Log.d(TAG, "onNavDestChanged: $domain")
            param(FirebaseAnalytics.Param.SCREEN_NAME, domain)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        // Manage SplashScreen
        installSplashScreen()
        // init the heavy duty tasks in initialize()
        initialize()
        // Set the content of the activity
        setContent {
            val navController = rememberNavController()
            // if authentication is required move to lock screen
            Gallery(toastHostState, navController)
            DisposableEffect(Unit) {
                Log.d(TAG, "onCreate - DisposableEffect: $timeAppWentToBackground")
                navController.addOnDestinationChangedListener(this@MainActivity)
                // since auth is required; we will cover the scrren with lock_screen
                // only when user authenticate can remove this veil.
                if (isAuthenticationRequired) {
                    navController.navigate(RouteLockScreen()) {
                        launchSingleTop = true
                    }
                }
                this@MainActivity.navController = navController
                onDispose {
                    navController.removeOnDestinationChangedListener(this@MainActivity)
                    this@MainActivity.navController = null
                }
            }
        }
    }
}