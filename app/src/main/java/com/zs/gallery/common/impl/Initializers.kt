/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 28 of Dec 2025
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
 * Last Modified by sheik on 28 of Dec 2025
 *
 */

package com.zs.gallery.common.impl

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.AnimationConstants
import androidx.startup.Initializer
import coil3.asImage
import coil3.request.crossfade
import com.zs.common.analytics.Analytics
import com.zs.common.db.albums.Albums
import com.zs.gallery.R
import com.zs.preferences.Preferences
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import androidx.appcompat.content.res.AppCompatResources.getDrawable as Drawable
import coil3.ImageLoader.Builder as ImageLoader
import coil3.SingletonImageLoader.setUnsafe as Coil
import com.zs.compose.theme.snackbar.SnackbarHostState as SnackbarController
import com.zs.gallery.common.Gallery as G

private const val TAG = "Initializers"

private val APP_MODULES = module {
    // Define Koin modules for dependency injection.
    // Declare a singleton instance of Preferences.
    single {
        // Initialize Preferences
        val preferences = Preferences(get(), "preferences_db")
        // Retrieve the current launch counter value, defaulting to 0 if not set
        // Read current launch counter, increment for cold start, and log the updated value
        preferences[G.keys.launch_counter] += preferences[G.keys.launch_counter]
        Log.d(TAG, "Cold start counter: ${preferences[G.keys.launch_counter]}")
        // Return the preferences instance
        preferences
    }
    // Declare a ViewModel dependency (lifecycle managed by Koin).
    // viewModel { BatteryViewModel(get()) }
    singleOf(::SnackbarController)
    factory { Albums(get()) }
    factory { androidApplication().resources }
    viewModelOf(::SettingsViewModel)
    viewModelOf(::FilesViewModel)
}

// Initializes Koin dependency injection at app startup
class KoinInitializer : Initializer<Unit> {
    // No dependencies on other libraries.
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
    override fun create(context: Context) {
        startKoin {
            androidContext(context)
            modules(APP_MODULES)
        }
    }
}

// Initializes Firebase Analytics at app startup
class AnalyticsInitializer : Initializer<Unit> {
    override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
    override fun create(context: Context) {
        Log.d(TAG, "initializing firebase: ")
        Analytics.initialize(context)
    }
}

//
class CoilInitializer : Initializer<Unit> {
    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()

    override fun create(context: Context) {
        // Prepare a fallback drawable used when image loading fails
        val error = Drawable(context, R.drawable.ic_error_image_placeholder)!!.asImage()
        // Build a global ImageLoader instance with error handling and crossfade animation
        val loader = ImageLoader(context)
            .error(error)
            .crossfade(AnimationConstants.DefaultDurationMillis)
            .components {
                // Register custom fetchers or components here if needed
                // e.g., add(ThumbnailFetcher.Factory())
            }.build()
        // Set the constructed loader as the global Coil instance
        Coil(loader)
    }
}