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

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.res.ResourcesCompat
import com.primex.preferences.Key
import com.zs.foundation.toast.Duration
import com.zs.foundation.toast.Toast
import com.zs.gallery.BuildConfig

interface SystemFacade {

    fun showToast(
        message: CharSequence,
        icon: ImageVector? = null,
        action: CharSequence? = null,
        @Duration duration: Int = if (action == null) Toast.DURATION_SHORT else Toast.DURATION_INDEFINITE,
    )

    fun showToast(
        @StringRes message: Int,
        icon: ImageVector? = null,
        @StringRes action: Int = ResourcesCompat.ID_NULL,
        @Duration duration: Int = if (action == ResourcesCompat.ID_NULL) Toast.DURATION_SHORT else Toast.DURATION_INDEFINITE,
    )

    /**
     * Configures the system bars (status bar and navigation bar) for the current screen.
     * Uses system settings by default, but can be overridden with the provided parameters.
     *
     * @param hide Hides system bars if true, shows if false, uses system setting if null.
     * @param translucent Draws a translucent veil behind system bars if true else none,
     *                    uses system setting if null. Ignored if `hide` is true.
     * @param dark Uses a dark theme for system bars if true, light theme if false,
     *             system setting if null. Ignored if `hide` is true.
     */
    fun enableEdgeToEdge(
        hide: Boolean? = null,
        translucent: Boolean? = null,
        dark: Boolean? = null
    )

    /**
     * @see com.primex.preferences.Preferences.observeAsState
     */
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
        val result = runCatching {
            launch(AppStoreIntent(pkg))
        }
        if (result.isFailure)
            launch(FallbackAppStoreIntent(pkg))
    }

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
