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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.settings.Settings
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

    override fun showToast(
        message: CharSequence, icon: ImageVector?, action: CharSequence?, duration: Int
    ) {
        lifecycleScope.launch {
            toastHostState.showToast(message, action, icon, duration = duration)
        }
    }

    override fun showToast(message: Int, icon: ImageVector?, action: Int, duration: Int) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app has started from scratch if savedInstanceState is null.
        val isColdStart = savedInstanceState == null //why?
        if (preferences.value(Settings.KEY_SECURE_MODE)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        // Manage SplashScreen
        configureSplashScreen(isColdStart)
        enableEdgeToEdge()
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