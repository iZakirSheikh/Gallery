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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.view.WindowCompat
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
import com.zs.compose.foundation.Amber
import com.zs.compose.foundation.getText2
import com.zs.compose.foundation.runCatching
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarHostState
import com.zs.compose.theme.snackbar.SnackbarResult
import com.zs.core.BuildConfig
import com.zs.core.billing.Paymaster
import com.zs.core.billing.Product
import com.zs.core.billing.Purchase
import com.zs.core.billing.purchased
import com.zs.core.common.showPlatformToast
import com.zs.core.getPackageInfoCompat
import com.zs.gallery.common.IAP_BUY_ME_COFFEE
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.WindowStyle
import com.zs.gallery.common.domain
import com.zs.gallery.common.products
import com.zs.gallery.files.RouteFiles
import com.zs.gallery.lockscreen.RouteLockScreen
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.RouteIntentViewer
import com.zs.preferences.Key
import com.zs.preferences.Key.Key1
import com.zs.preferences.Key.Key2
import com.zs.preferences.Preferences
import com.zs.preferences.intPreferenceKey
import com.zs.preferences.longPreferenceKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen as configSplashScreen
import androidx.navigation.NavController.OnDestinationChangedListener as NavDestListener

private const val TAG = "MainActivity"

// In-app update and review settings
// Maximum staleness days allowed for a flexible update.
// If the app is older than this, an immediate update will be enforced.
private const val FLEXIBLE_UPDATE_MAX_STALENESS_DAYS = 2

// Minimum number of app launches before prompting for a review.
private const val MIN_LAUNCHES_BEFORE_REVIEW = 5

// Number of days to wait before showing the first review prompt.
private val INITIAL_REVIEW_DELAY = 3.days

// The maximum number of distinct promotional messages to display to the user.
private val MAX_PROMO_MESSAGES = 2

// The number of app launches to skip between showing consecutive promotional messages.
// After each promotional message is shown, the app will skip this many launches before
// potentially showing another promotional message.
private val PROMO_SKIP_LAUNCHES = 10


// Minimum number of days between subsequent review prompts.
// Since we cannot confirm if the user actually left a review, we use this interval
// to avoid prompting too frequently.
private val STANDARD_REVIEW_DELAY = 5.days
private val KEY_LAST_REVIEW_TIME = longPreferenceKey(TAG + "_last_review_time", 0)
private val KEY_APP_VERSION_CODE = intPreferenceKey(TAG + "_app_version_code", -1)

@Composable
private inline fun <S, O> Preferences.observeAsState(key: Key<S, O>): State<O?> {
    val flow = when (key) {
        is Key1 -> observe(key)
        is Key2 -> observe(key)
    }

    val first = remember(key.name) {
        runBlocking { flow.first() }
    }
    return flow.collectAsState(initial = first)
}

/**
 * @property inAppUpdateProgress A simple property that represents the progress of the in-app update.
 *        The progress value is a float between 0.0 and 1.0, indicating the percentage of the
 *        update that has been completed. The Float.NaN represents a default value when no update
 *        is going on.
 * @property timeAppWentToBackground The time in mills until the app was in background state. default value -1L
 * @property isAuthenticationRequired A boolean flag indicating whether authentication is required.
 */
class MainActivity : ComponentActivity(), SystemFacade, NavDestListener {

    private val snackbarHostState: SnackbarHostState by inject()
    private val preferences: Preferences by inject()
    private var navController: NavHostController? = null

    private val paymaster by lazy {
        Paymaster(this, BuildConfig.PLAY_CONSOLE_APP_RSA_KEY, Paymaster.products)
    }

    var _style: Int by mutableIntStateOf(WindowStyle.FLAG_STYLE_AUTO)
    override var style: WindowStyle
        get() = WindowStyle(_style)
        set(value) { _style = value.value }
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
            return when (val timeoutValue = preferences[Settings.KEY_APP_LOCK_TIME_OUT]) {
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
        paymaster.sync()
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
            if (navController?.currentDestination?.domain == RouteIntentViewer.domain) {
                navController?.popBackStack()
            }
            navController?.navigate(RouteLockScreen()) {
                launchSingleTop = true
            }
        }
    }

    override fun onDestroy() {
        paymaster.release()
        super.onDestroy()
    }

    override fun showToast(message: String, duration: Int) =
        showPlatformToast(message, duration)

    override fun showToast(message: Int, duration: Int) =
        showPlatformToast(message, duration)

    override fun <T> getDeviceService(name: String): T =
        getSystemService(name) as T

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
        if (timeAppWentToBackground == -1L)
            timeAppWentToBackground = 0L
        // Check if the start destination needs to be updated
        // Update the start destination to RouteTimeline
        if (navController.graph.startDestinationRoute == RouteLockScreen()) {
            Log.d(TAG, "unlock: updating start destination")
            navController.graph.setStartDestination(RouteFiles())
            navController.navigate(RouteFiles()) {
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

    override fun showSnackbar(
        message: CharSequence,
        icon: ImageVector?,
        accent: Color,
        duration: SnackbarDuration,
    ) {
        lifecycleScope.launch {
            snackbarHostState.showSnackbar(
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

    @Composable
    @NonRestartableComposable
    override fun <S, O> observeAsState(key: Key1<S, O>) =
        preferences.observeAsState(key = key)

    @Composable
    @NonRestartableComposable
    override fun <S, O> observeAsState(key: Key2<S, O>) =
        preferences.observeAsState(key = key) as State<O>

    @Composable
    @NonRestartableComposable
    override fun observePurchaseAsState(id: String): State<Purchase?> {
        return produceState(remember(id) { paymaster.purchases.value.find { it.id == id } }) {
            paymaster.purchases.map { it.find { it.id == id } }.collect {
                value = it  // updating purchase
            }
        }
    }

    override fun launch(intent: Intent, options: Bundle?) =
        startActivity(intent, options)

    override fun initiateUpdateFlow(report: Boolean) {
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
                    inAppUpdateProgress = progress
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
                    val res = snackbarHostState.showSnackbar(
                        message = resources.getText2(R.string.msg_new_update_downloaded),
                        action = resources.getText2(R.string.install),
                        duration = SnackbarDuration.Long,
                        icon = Icons.Outlined.NewReleases
                    )
                    // complete update when ever user clicks on action.
                    if (res == SnackbarResult.ActionPerformed) manager.completeUpdate()
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

    override fun initiateReviewFlow() {
        lifecycleScope.launch {
            // Get the app launch count from preferences.
            val count = preferences[Settings.KEY_LAUNCH_COUNTER]
            // Check if the minimum launch count has been reached.
            if (count < MIN_LAUNCHES_BEFORE_REVIEW)
                return@launch
            // Get the first install time of the app.
            // Check if enough time has passed since the first install.
            val firstInstallTime =
                packageManager.getPackageInfoCompat(packageName)?.firstInstallTime
                    ?: 0
            val currentTime = System.currentTimeMillis()
            if (currentTime - firstInstallTime < INITIAL_REVIEW_DELAY.inWholeMilliseconds)
                return@launch
            // Get the last time the review prompt was shown.
            // Check if enough time has passed since the last review prompt.
            val lastAskedTime = preferences[KEY_LAST_REVIEW_TIME]
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

    override fun initiatePurchaseFlow(id: String) =
        paymaster.initiatePurchaseFlow(this, id)

    override fun getProductInfo(id: String): Product? =
        paymaster.details.value.find { it.id == id }

    override fun onDestinationChanged(
        cont: NavController,
        dest: NavDestination,
        args: Bundle?,
    ) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {  // Log the event.
            // create params for the event.
            val domain = dest.domain ?: "unknown"
            Log.d(TAG, "onNavDestChanged: $domain")
            param(FirebaseAnalytics.Param.SCREEN_NAME, domain)
        }
    }

    private fun showPromoToast(
        index: Int,
        delay: Long = 5_000,
    ) {
        // This function is designed to display promotional messages identified by index.
        // - An index of 0 indicates the "What's New" message.
        // - An index of 1 is used to promote the media player.
        // - An index of 2 prompts the user to buy a coffee.
        // If a message cannot be displayed for any reason, the index is incremented by 1 until the
        // maximum index is reached.
        lifecycleScope.launch {
            if (delay > 0) delay(delay) // delay at least some
            when (index) {
                // What's new
                0 -> showSnackbar(
                    R.string.what_s_new_latest,
                    duration = SnackbarDuration.Indefinite,
                    icon = Icons.Outlined.NewReleases
                )
                // Media player
                1 -> {
                    val result = snackbarHostState.showSnackbar(
                        message = resources.getText2(R.string.msg_media_player_promotion),
                        icon = Icons.Outlined.GetApp,
                        duration = SnackbarDuration.Indefinite,
                        action = resources.getText2(R.string.get),
                        accent = Color.Amber
                    )
                    if (result == SnackbarResult.ActionPerformed)
                        launchAppStore("com.prime.player")
                }
                // Buy me a coffee.
                2 -> {
                    val purchase =
                        paymaster.purchases.value.find() { it.id == Paymaster.IAP_BUY_ME_COFFEE }
                    if (purchase.purchased)
                        return@launch
                    val result = snackbarHostState.showSnackbar(
                        resources.getText2(R.string.msg_support_gallery),
                        duration = SnackbarDuration.Indefinite,
                        icon = Icons.Outlined.Coffee,
                        action = getString(R.string.fuel)
                    )
                    if (result == SnackbarResult.ActionPerformed)
                        initiatePurchaseFlow(Paymaster.IAP_BUY_ME_COFFEE)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action != Intent.ACTION_VIEW)
            return
        lifecycleScope.launch {
            // we delay it here because on resume loads lockscreen.
            // we want this to overlay over lockscreen; hence this.

            delay(200)
            navController?.navigate(RouteIntentViewer(intent.data!!, intent.type ?: "image/*")) {
                launchSingleTop = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        // This is determined by checking if savedInstanceState is null.
        // If null, it's a cold start (first time launch or activity recreated from scratch)
        val isColdStart = savedInstanceState == null
        // Configure the splash screen for the app
        configSplashScreen()
        // Initialize
        if (isColdStart) {
            // Trigger update flow
            initiateUpdateFlow()

            // Enable secure mode if required by user settings
            if (preferences[Settings.KEY_SECURE_MODE])
                window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)

            // Promote media player on every 5th launch
            // TODO - properly handle promotional content.
            lifecycleScope.launch {
                // Show "What's New" message if the app version has changed
                val versionCode = packageManager.getPackageInfoCompat(packageName)?.versionCode ?: 0
                val savedVersionCode = preferences[KEY_APP_VERSION_CODE]
                if (savedVersionCode != versionCode) {
                    preferences[KEY_APP_VERSION_CODE] = versionCode
                    showPromoToast(0) // What's new
                    return@launch
                }
                // Promotional messages are displayed only after the app has been launched
                // more than 5 times (MIN_LAUNCHES_BEFORE_REVIEW).
                // This ensures that users have had a chance to familiarize themselves with the app
                // before being presented with these messages.
                // An index of 0 is reserved for the "What's New" message and is handled separately.
                // Promotional messages start with index 1.
                // The index is calculated using the formula: (counter % MAX_PROMO_MESSAGES).coerceAtLeast(1).
                // Each message is skipped by PROMO_SKIP_LAUNCHES number of launches.
                val counter = preferences[Settings.KEY_LAUNCH_COUNTER]
                if (counter < MIN_LAUNCHES_BEFORE_REVIEW)
                    return@launch
                val newCounter = counter - MIN_LAUNCHES_BEFORE_REVIEW
                val interval = PROMO_SKIP_LAUNCHES + 1
                // This line calculates which promotional message to show from a rotating set.
                Log.d(
                    TAG,
                    "Promo(counter=$counter," +
                            " interval=$interval," +
                            " newCounter=$newCounter," +
                            " skip = ${newCounter % interval}," +
                            " index = ${(newCounter / interval) % MAX_PROMO_MESSAGES + 1} ) "
                )
                if (newCounter % interval == 0) {
                    val index = (newCounter / interval) % MAX_PROMO_MESSAGES + 1
                    Log.d(TAG, "onCreate: $index")
                    showPromoToast(index)
                }
            }
        }
        // Set up the window to fit the system windows
        // This setting is usually configured in the app theme, but is ensured here
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Set the content of the activity
        setContent {
            val navController = rememberNavController()
            // If the action is VIEW, load the content first, regardless
            // of whether the app is currently locked or not. This allows
            // users to view shared media directly.
            // else If authentication is required, move to the lock screen
            Home(
                when {
                    intent.action == Intent.ACTION_VIEW -> RouteIntentViewer
                    isAuthenticationRequired -> RouteLockScreen
                    else -> RouteFiles
                },
                snackbarHostState,
                navController
            )
            // Manage lifecycle-related events and listeners
            DisposableEffect(Unit) {
                Log.d(TAG, "onCreate - DisposableEffect: $timeAppWentToBackground")
                navController.addOnDestinationChangedListener(this@MainActivity)
                // Cover the screen with lock_screen if authentication is required
                // Only remove this veil when the user authenticates
                if (isAuthenticationRequired) unlock()
                this@MainActivity.navController = navController
                onDispose {
                    navController.removeOnDestinationChangedListener(this@MainActivity)
                    this@MainActivity.navController = null
                }
            }
        }
    }
}