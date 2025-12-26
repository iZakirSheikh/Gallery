/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 26 of Dec 2025
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
 * Last Modified by sheik on 26 of Dec 2025
 */

package com.zs.gallery.common

import android.app.Activity
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import com.zs.common.BuildConfig
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.gallery.R

interface SystemFacade {

    /** @see Context.showToast  */
    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        val activity = (this as? Activity) ?: return
        Toast.makeText(activity, message, duration).show()
    }

    /**  @see Context.showToast  */
    fun showToast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
        val activity = (this as? Activity) ?: return
        Toast.makeText(activity, message, duration).show()
    }

    /** @see com.zs.compose.theme.snackbar.SnackbarHostState.showSnackbar */
    fun showSnackbar(
        message: CharSequence,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration = SnackbarDuration.Short,
    )

    /** @see com.zs.compose.theme.snackbar.SnackbarHostState.showSnackbar */
    fun showSnackbar(
        @StringRes message: Int,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration = SnackbarDuration.Short,
    )

    /**
     * Launches the provided [intent] with the specified [options].
     */
    fun launch(intent: Intent, options: Bundle? = null) {
        val activity = (this as? Activity) ?: return
        activity.startActivity(intent, options)
    }

    /**
     * Launches the App Store to open the app details page for a given package.
     *
     * This function first attempts to open the App Store directly using [AppStoreIntent].
     * If this fails, it falls back to using [FallbackAppStoreIntent] as an alternative.
     *
     * @param pkg the package name of the app to open on the App Store.
     */
    fun launchAppStore(pkg: String = (this as? Activity)?.packageName ?: "") {
        val url = "${Reg.PREFIX_MARKET_URL}$pkg"
        // Create an Intent to open the Play Store app.
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            // Set the package to explicitly target the Play Store app.
            // Don't add this activity to the history stack.
            // Open in a new document (tab or window).
            // Allow multiple instances of the task.
            setPackage(Reg.PKG_MARKET_ID)
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
            val fallback = "${Reg.PREFIX_MARKET_FALLBACK}$pkg"
            launch(Intent(Intent.ACTION_VIEW, fallback.toUri()))
        }
    }


    fun <T> getDeviceService(name: String): T {
        val activity = (this as? Activity) ?: error("this is not instance of ${Activity::class}")
        @Suppress("UNCHECKED_CAST")
        return activity.getSystemService(name) as T
    }

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


}