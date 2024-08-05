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
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.primex.core.getText2
import com.primex.preferences.Key
import com.primex.preferences.Preferences
import com.primex.preferences.observeAsState
import com.primex.preferences.value
import com.zs.compose_ktx.LocalWindowSize
import com.zs.compose_ktx.calculateWindowSizeClass
import com.zs.compose_ktx.toast.ToastHostState
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val TAG = "MainActivity"

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

class MainActivity : ComponentActivity(), SystemFacade {
    private val toastHostState: ToastHostState by inject()
    private val preferences: Preferences by inject()

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
        action: CharSequence?,
        duration: Int
    ) {
        lifecycleScope.launch {
            toastHostState.showToast(message, action, icon, duration = duration)
        }
    }

    override fun showToast(
        message: Int,
        icon: ImageVector?,
        action: Int,
        duration: Int
    ) {
        lifecycleScope.launch {
            toastHostState.showToast(
                resources.getText2(id = message),
                if (action == ResourcesCompat.ID_NULL) null else resources.getText2(action),
                icon,
                duration = duration
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // caused the change in config. means the system bars need to be configured again
        // but what would happen if the system bars are already configured?
        enableEdgeToEdge()
    }


    private fun initialize(){
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