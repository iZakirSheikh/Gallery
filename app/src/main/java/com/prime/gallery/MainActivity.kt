package com.prime.gallery


import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.animation.doOnEnd
import androidx.core.app.ShareCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.Purchase
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.review.ReviewManagerFactory
import com.prime.gallery.core.billing.Advertiser
import com.prime.gallery.core.billing.BillingManager
import com.prime.gallery.core.billing.get
import com.prime.gallery.core.billing.observeAsState
import com.prime.gallery.core.billing.purchased
import com.prime.gallery.core.compose.LocalSystemFacade
import com.prime.gallery.core.compose.LocalWindowSizeClass
import com.prime.gallery.core.compose.SystemFacade
import com.prime.gallery.core.compose.snackbar.SnackbarDuration
import com.prime.gallery.core.compose.snackbar.SnackbarHostState2
import com.prime.gallery.core.compose.snackbar.SnackbarResult
import com.primex.core.MetroGreen
import com.primex.preferences.Key
import com.primex.preferences.Preferences
import com.primex.preferences.longPreferenceKey
import com.primex.preferences.observeAsState
import com.primex.preferences.value
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "MainActivity"

private const val MIN_LAUNCH_COUNT = 5
private val MAX_DAYS_BEFORE_FIRST_REVIEW = TimeUnit.DAYS.toMillis(3)
private val MAX_DAY_AFTER_FIRST_REVIEW = TimeUnit.DAYS.toMillis(5)

private val KEY_LAST_REVIEW_TIME =
    longPreferenceKey(TAG + "_last_review_time")

private const val FLEXIBLE_UPDATE_MAX_STALENESS_DAYS = 2
private const val RESULT_CODE_APP_UPDATE = 1000

/**
 * Manages SplashScreen
 */
context(ComponentActivity)
private fun initSplashScreen(isColdStart: Boolean) {
    // Install Splash Screen and Play animation when cold start.
    installSplashScreen().let { screen ->
        // Animate entry of content
        if (!isColdStart)
            return@let
        screen.setOnExitAnimationListener { provider ->
            val splashScreenView = provider.view
            // Create your custom animation.
            val alpha = ObjectAnimator.ofFloat(
                splashScreenView, View.ALPHA, 1f, 0f
            )
            alpha.interpolator = AnticipateInterpolator()
            alpha.duration = 700L
            // Call SplashScreenView.remove at the end of your custom animation.
            alpha.doOnEnd { provider.remove() }
            // Run your animation.
            alpha.start()
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SystemFacade {
    private val advertiser by lazy { Advertiser(this) }
    private val billingManager by lazy { BillingManager(this, arrayOf(BuildConfig.IAP_NO_ADS)) }

    private val _inAppUpdateProgress = mutableFloatStateOf(Float.NaN)
    override val inAppUpdateProgress: Float
        get() = _inAppUpdateProgress.floatValue

    // injectable code.
    @Inject
    lateinit var preferences: Preferences

    @Inject
    lateinit var channel: SnackbarHostState2

    override fun onResume() {
        super.onResume()
        billingManager.refresh()
    }

    override fun onDestroy() {
        billingManager.release()
        super.onDestroy()
    }

    override fun showAd(force: Boolean, action: (() -> Unit)?) {
        val isAdFree = billingManager[BuildConfig.IAP_NO_ADS].purchased
        if (isAdFree) return // don't do anything
        advertiser.show(this, force, action)
    }

    override fun showSnackbar(
        message: CharSequence,
        action: CharSequence?,
        icon: Any?,
        accent: Color,
        duration: SnackbarDuration
    ) {
        lifecycleScope.launch {
            channel.showSnackbar(message, action, icon, accent, duration)
        }
    }

    override fun launchAppStore() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Gallery.GOOGLE_STORE)).apply {
            setPackage(Gallery.PKG_GOOGLE_PLAY_STORE)
            addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }
        val res = kotlin.runCatching { startActivity(intent) }
        if (res.isFailure)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Gallery.FALLBACK_GOOGLE_STORE)))
    }

    override fun launchBillingFlow(id: String) {
        billingManager.launchBillingFlow(this, id)
    }

    @Composable
    @NonRestartableComposable
    override fun <S, O> observeAsState(key: Key.Key1<S, O>): State<O?> =
        preferences.observeAsState(key = key)

    @Composable
    @NonRestartableComposable
    override fun <S, O> observeAsState(key: Key.Key2<S, O>): State<O> =
        preferences.observeAsState(key = key)

    @Composable
    @NonRestartableComposable
    override fun observeAsState(product: String): State<Purchase?> =
        billingManager.observeAsState(id = product)

    override fun launchReviewFlow() {
        lifecycleScope.launch {
            val count = preferences.value(Gallery.KEY_LAUNCH_COUNTER) ?: 0
            // the time when lastly asked for review
            val lastAskedTime = preferences.value(KEY_LAST_REVIEW_TIME)
            // obtain teh first install time.
            val firstInstallTime =
                com.primex.core.runCatching(TAG + "_review") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        packageManager.getPackageInfo(
                            packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        )
                    else
                        packageManager.getPackageInfo(packageName, 0)
                }?.firstInstallTime
            // obtain the current time.
            val currentTime = System.currentTimeMillis()
            // Only first time we should not ask immediately
            // however other than this whenever we do some thing of appreciation.
            // we should ask for review.
            var ask =
                (lastAskedTime == null && firstInstallTime != null
                        && count >= MIN_LAUNCH_COUNT
                        && currentTime - firstInstallTime >= MAX_DAYS_BEFORE_FIRST_REVIEW)
            // check for other condition as well
            // if this is not the first review; ask only if after time passed.
            ask =
                ask || (lastAskedTime != null
                        && count >= MIN_LAUNCH_COUNT
                        && currentTime - lastAskedTime >= MAX_DAY_AFTER_FIRST_REVIEW)
            // return from here if not required to ask
            if (!ask) return@launch
            // The flow has finished. The API does not indicate whether the user
            // reviewed or not, or even whether the review dialog was shown. Thus, no
            // matter the result, we continue our app flow.
            com.primex.core.runCatching(TAG) {
                val reviewManager = ReviewManagerFactory.create(this@MainActivity)
                // update the last asking
                preferences[KEY_LAST_REVIEW_TIME] = System.currentTimeMillis()
                val info = reviewManager.requestReview()
                reviewManager.launchReviewFlow(this@MainActivity, info)
                //host.fAnalytics.
            }
        }
    }

    override fun launchUpdateFlow(report: Boolean) {
        lifecycleScope.launch {
            com.primex.core.runCatching(TAG) {
                val manager = AppUpdateManagerFactory.create(this@MainActivity)
                manager.requestUpdateFlow().collect { result ->
                    when (result) {
                        AppUpdateResult.NotAvailable ->
                            if (report) channel.showSnackbar("The app is already updated to the latest version.")

                        is AppUpdateResult.InProgress -> {
                            val state = result.installState
                            val total = state.totalBytesToDownload()
                            val downloaded = state.bytesDownloaded()
                            val progress = when {
                                total <= 0 -> -1f
                                total == downloaded -> Float.NaN
                                else -> downloaded / total.toFloat()
                            }
                            _inAppUpdateProgress.value = progress
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
                                return@collect
                            }
                            // else show the toast.
                            val res = channel.showSnackbar(
                                msg = "Update \nAn update has just been downloaded.",
                                action = "RESTART",
                                duration = SnackbarDuration.Indefinite,
                                accent = Color.MetroGreen
                            )
                            // complete update when ever user clicks on action.
                            if (res == SnackbarResult.ActionPerformed) manager.completeUpdate()
                        }

                        is AppUpdateResult.Available -> {
                            // if user choose to skip the update handle that case also.
                            val isFlexible = (result.updateInfo.clientVersionStalenessDays()
                                ?: -1) <= FLEXIBLE_UPDATE_MAX_STALENESS_DAYS
                            if (isFlexible) result.startFlexibleUpdate(
                                activity = this@MainActivity, RESULT_CODE_APP_UPDATE
                            )
                            else result.startImmediateUpdate(
                                activity = this@MainActivity, RESULT_CODE_APP_UPDATE
                            )
                            // no message needs to be shown
                        }
                    }
                }
            }
        }
    }

    override fun shareApp() {
        ShareCompat.IntentBuilder(this).setType("text/plain")
            .setChooserTitle(getString(R.string.app_name))
            .setText("Let me recommend you this application ${Gallery.GOOGLE_STORE}").startChooser()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        /*TODO: Not Implemented yet!*/
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app has started from scratch if savedInstanceState is null.
        val isColdStart = savedInstanceState == null //why?
        // show splash screen
        initSplashScreen(isColdStart)
        // only run this piece of code if cold start.
        if (isColdStart) {
            val counter = preferences.value(Gallery.KEY_LAUNCH_COUNTER) ?: 0
            // update launch counter if
            // cold start.
            preferences[Gallery.KEY_LAUNCH_COUNTER] = counter + 1
            // check for updates on startup
            // don't report
            // check silently
            launchUpdateFlow()
            // TODO: Try to reconcile if it is any good to ask for reviews here.
            // launchReviewFlow()
        }
        // Manually handle decor.
        // I think I am handling this in AppTheme Already.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // pass intent to onNewIntent
        onNewIntent(intent)
        // Set the content.
        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)
            CompositionLocalProvider(
                LocalSystemFacade provides this,
                LocalWindowSizeClass provides windowSizeClass,
                content = { Home(channel = channel) }
            )
        }
    }
}
