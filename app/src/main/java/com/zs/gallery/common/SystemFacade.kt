/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 12-07-2024.
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

@file:Suppress("NOTHING_TO_INLINE")

package com.zs.gallery.common

import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.core.billing.Product
import com.zs.core.billing.Purchase
import com.zs.gallery.BuildConfig
import com.zs.gallery.R
import com.zs.preferences.Key

private const val PREFIX_MARKET_URL = "market://details?id="
private const val PREFIX_MARKET_FALLBACK = "http://play.google.com/store/apps/details?id="
private const val PKG_MARKET_ID = "com.android.vending"

/**
 * An interface defining the methods and properties needed for common app functionality,
 * such as in-app updates, showing ads, and launching the app store.
 *
 * This interface is intended to be implemented by a class that is scoped to the entire app,
 * and is accessible from all parts of the app hierarchy.
 *
 * @property style The current [WindowStyle] as requested by the current screen. Defaults to `Automatic`
 */
interface SystemFacade {

    var style: WindowStyle

    /**
     * @see Context.showToast
     */
    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT)

    /**
     * @see Context.showToast
     */
    fun showToast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT)

    /**
     * @see com.zs.compose.theme.snackbar.SnackbarHostState.showSnackbar
     */
    fun showSnackbar(
        message: CharSequence,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration = SnackbarDuration.Short,
    )

    /**
     * @see com.zs.compose.theme.snackbar.SnackbarHostState.showSnackbar
     */
    fun showSnackbar(
        @StringRes message: Int,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration = SnackbarDuration.Short,
    )

    @Composable
    @NonRestartableComposable
    fun <S, O> observeAsState(key: Key.Key1<S, O>): State<O?>

    @Composable
    @NonRestartableComposable
    fun <S, O> observeAsState(key: Key.Key2<S, O>): State<O>

    /**
     * Launches the provided [intent] with the specified [options].
     */
    fun launch(intent: Intent, options: Bundle? = null)

    /**
     * Launches the App Store to open the app details page for a given package.
     *
     * This function first attempts to open the App Store directly using [AppStoreIntent].
     * If this fails, it falls back to using [FallbackAppStoreIntent] as an alternative.
     *
     * @param pkg the package name of the app to open on the App Store.
     */
    fun launchAppStore(pkg: String = BuildConfig.APPLICATION_ID) {
        val url = "$PREFIX_MARKET_URL$pkg"
        // Create an Intent to open the Play Store app.
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            // Set the package to explicitly target the Play Store app.
            // Don't add this activity to the history stack.
            // Open in a new document (tab or window).
            // Allow multiple instances of the task.
            setPackage(PKG_MARKET_ID)
            addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }
        // Try launching the Play Store app.
        val res = kotlin.runCatching { launch(intent) }
        // If launching the app fails, use the fallback URL to open in a web browser.
        if (res.isFailure) {
            val fallback = "${PREFIX_MARKET_FALLBACK}$pkg"
            launch(Intent(Intent.ACTION_VIEW, fallback.toUri()))
        }
    }

    /**
     * A utility method to launch the in-app update flow, with an option to report low-priority
     * issues to the user via a Toast.
     *
     * @param report If `true`, low-priority issues will be reported to the user using the
     *               ToastHostState channel.
     */
    fun launchUpdateFlow(report: Boolean = false)

    /**
     * Launches an in-app review process if appropriate.
     *
     * This method ensures the review dialog is shown only at suitable intervals based on launch count and time since last prompt.
     * It considers [MIN_LAUNCH_COUNT], [MAX_DAYS_BEFORE_FIRST_REVIEW], and [MAX_DAYS_AFTER_FIRST_REVIEW] to prevent excessive prompting.
     */
    fun launchReviewFlow()

    /**
     * Unlocks the app, granting access to its content.
     *
     * This function initiates the authentication process, typically using biometric verification or
     * other security measures, to unlock the app and allow the user to access its features.
     */
    fun unlock()

    /**
     * Initiates biometric authentication.
     *
     * Displays a biometric prompt to the user. If authentication is successful, the [onAuthenticated]
     * callback will be executed. This function requires Android P (API level 28) or higher.
     *
     * @param onAuthenticated A callback function that will be executed upon successful authentication.
     * @param desc A description of the authentication process. This description will be displayed
     * @param subtitle A subtitle for the biometric prompt.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun authenticate(subtitle: String? = null, desc: String? = null, onAuthenticated: () -> Unit)

    /**
     * Guides the user to the system settings for biometric enrollment.
     *
     * This function helps users set up biometric authentication (e.g., fingerprint, face recognition)
     * by directing them to the appropriate settings on their device. This is necessary before the app
     * can use biometric authentication features.
     *
     * Requires Android P (API level 28) or higher.
     */
    fun enroll() {
        // Create an intent based on the Android version
        val intent = when {
            // For Android 11 (API level 30) and above, use the biometric enrollment action
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                    .putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
            // For Android 9 (API level 28) and Android 10 (API level 29), use the fingerprint enrollment action
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ->
                Intent(Settings.ACTION_FINGERPRINT_ENROLL)
            // For versions below Android 9, open the general security settings
            else -> Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        // Show a toast message to inform the user to enable biometric authentication
        showToast(R.string.msg_enroll_biometric)
        // Attempt to launch the intent and handle any potential failures
        val result = kotlin.runCatching {
            launch(intent)
        }
        // If the intent fails to resolve, fall back to opening the general settings
        if (result.isFailure)
            launch(Intent(Settings.ACTION_SETTINGS))
    }

    /**
     * Returns the handle to a system-level service by name.
     *
     * @param name The name of the desired service.
     * @return The service object, or null if the name does not exist.
     * @throws ClassCastException if the service is not of the expected type.
     * @see Context.BIOMETRIC_SERVICE
     * @see android.app.Activity.getSystemService
     */
    fun <T> getDeviceService(name: String): T


    /**
     * Checks if biometric authentication is available on the device.
     */
    fun canAuthenticate(): Boolean = when {
        // Biometric authentication is not available on devices below Android P.
        Build.VERSION.SDK_INT < Build.VERSION_CODES.P -> false
        // For Android P, check if fingerprints are enrolled using FingerprintManager.
        Build.VERSION.SDK_INT == Build.VERSION_CODES.P -> {
            val manager = getDeviceService<FingerprintManager>(Context.FINGERPRINT_SERVICE)
            manager.hasEnrolledFingerprints()
        }
        // For Android Q and above, check if any biometric authentication is available using BiometricManager.
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val manager = getDeviceService(Context.BIOMETRIC_SERVICE) as BiometricManager
            manager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        }
        // For other Android versions, check if strong biometric or device credential authentication is available.
        else -> {
            val manager = getDeviceService(Context.BIOMETRIC_SERVICE) as BiometricManager
            val authenticators =
                BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG
            manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }


    /** Launches billing flow for the provided product [id]. */
    fun initiatePurchaseFlow(id: String): Boolean

    /**/
    @Composable
    @NonRestartableComposable
    fun observePurchaseAsState(id: String): State<Purchase?>

    /**
     * Retrieves information about a product with the given [id].
     */
    // FIX_ME - Using Product as nullable make it lost the performance that might have been gained
    //          through value class; instead make unspecified case of these.
    // This will be null when the lib has not been represhed otherwise this must not be null.
    fun getProductInfo(id: String): Product?
}

/**
 * A [staticCompositionLocalOf] variable that provides access to the [SystemFacade] interface.
 *
 * The [SystemFacade] interface defines common methods that can be implemented by an activity that
 * uses a single view with child views.
 * This local composition allows child views to access the implementation of the [SystemFacade]
 * interface provided by their parent activity.
 *
 * If the [SystemFacade] interface is not defined, an error message will be thrown.
 */
val LocalSystemFacade =
    staticCompositionLocalOf<SystemFacade> {
        error("Provider not defined.")
    }

/**
 * A composable function that uses the [LocalSystemFacade] to fetch [Preference] as state.
 * @param key A key to identify the preference value.
 * @return A [State] object that represents the current value of the preference identified by the provided key.
 * The value can be null if no preference value has been set for the given key.
 */
@Composable
inline fun <S, O> preference(key: Key.Key1<S, O>): State<O?> {
    val provider = LocalSystemFacade.current
    return provider.observeAsState(key = key)
}

/**
 * @see [preference]
 */
@Composable
inline fun <S, O> preference(key: Key.Key2<S, O>): State<O> {
    val provider = LocalSystemFacade.current
    return provider.observeAsState(key = key)
}

/**
 * A composable function that retrieves the purchase state of a product using the [LocalSystemFacade].
 *
 * This function leverages the `LocalSystemFacade` to access the purchase information for a given product ID.
 * In preview mode, it returns a `null` purchase state as the activity context is unavailable.
 *
 * @param id The ID of the product to check the purchase state for.
 * @return A [State] object representing the current purchase state of the product.
 * The state value can be `null` if there is no purchase associated with the given product ID or if the function
 * is called in preview mode.
 */
@Composable
@NonRestartableComposable
@Stable
fun purchase(id: String): State<Purchase?> = LocalSystemFacade.current.observePurchaseAsState(id)


