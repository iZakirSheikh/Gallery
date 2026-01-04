/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
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
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

package com.zs.gallery.common.impl

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.animation.core.AnimationConstants
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.startup.Initializer
import coil3.asImage
import coil3.request.crossfade
import com.zs.common.analytics.Analytics
import com.zs.common.db.album.MediaProvider
import com.zs.compose.theme.snackbar.SnackbarHostState
import com.zs.gallery.common.AppConfig
import com.zs.gallery.common.Res
import com.zs.preferences.Preferences
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import androidx.appcompat.content.res.AppCompatResources.getDrawable as Drawable
import coil3.ImageLoader.Builder as ImageLoader
import coil3.SingletonImageLoader.setUnsafe as Coil

private const val TAG = "Initializers"
private const val PREFERENCES_STORE_NAME = "preferences_db"

// CoilInitializer sets up a global Coil ImageLoader at app startup.
// It defines how images are loaded, what happens on errors, and which components are available.
class CoilInitializer : Initializer<Unit> {

    // No other initializers are required before this one runs
    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()

    override fun create(context: Context) {
        // --- Error Handling Setup ---
        // Define a fallback drawable that will be shown whenever image loading fails.
        // This ensures the UI never shows a blank or broken image.
        val error = Drawable(context, Res.drawable.ic_error_image_placeholder)!!.asImage()

        // --- Global ImageLoader Configuration ---
        // Build a single ImageLoader instance with:
        //   • Error drawable support (defined above)
        //   • Crossfade animation for smoother transitions
        //   • Optional custom fetchers/components (extensible for future needs)
        val loader = ImageLoader(context)
            .error(error)
            .crossfade(AnimationConstants.DefaultDurationMillis)
            .components {
                // Example: register custom fetchers or decoders here
                // add(ThumbnailFetcher.Factory())
            }
            .build()

        // --- Coil Singleton Registration ---
        // Assign the configured loader as the global Coil instance.
        // From this point onward, all Coil image requests in the app will use this loader.
        Coil(loader)
    }
}

// AppInitializer configures core services and sets up Koin dependency injection at app startup.
// It ensures analytics, media handling, and DI modules are ready before the app runs.
class AppInitializer : Initializer<Unit> {

    // This initializer does not depend on any other initializers.
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    override fun create(context: Context) {
        // --- Core Service Initialization ---
        // Prepare essential services that the app relies on:
        //   • Analytics: sets up event tracking and reporting
        //   • MediaProvider: configures access to media files and streams
        Analytics.initialize(context)
        MediaProvider.initialize(context)
        Preferences.initialize(context)

        val preferences = Preferences.getInstance()  // Initialize Preferences
        // Retrieve the current launch counter value, defaulting to 0 if not set
        // Read current launch counter, increment for cold start, and log the updated value
        val key = Res.key.app_launch_counter
        preferences[key] += preferences[key]
        Log.d(TAG, "Cold start counter: ${preferences[key]}")
        // --- Configuration Restore ---
        // Restore application configuration flags and settings from persisted preferences.
        AppConfig.restore(preferences[Res.key.app_config])

        // --- Dependency Injection Setup (Koin) ---
        // Start the Koin DI framework with:
        //   • Android context for resource access
        //   • Application modules that define bindings and providers
        // This makes dependencies available throughout the app.
        startKoin {
            androidContext(context)
            modules(AppModules)
        }
    }
}
// TODO - Move this logic of initializing pref to Preference DataStore.
// Volatile ensures visibility of changes across threads
@Volatile
private var INSTANCE: Preferences? = null

/**
 * Initialize the Preferences singleton.
 *
 * This method sets up the INSTANCE if it hasn't been created yet.
 * It does not return the instance — use [getInstance] to access it.
 *
 * @param context Context used to obtain the Application reference.
 */
fun Preferences.Companion.initialize(context: Context) {
    // If INSTANCE already exists, do nothing.
    if (INSTANCE != null) return
    synchronized(this) {
        if (INSTANCE == null) {
            // Ensure we are working with an Application context.
            val appContext = context.applicationContext
            require(appContext is Application) {
                "Context must be an Application"
            }

            // Create and assign the singleton instance.
            INSTANCE = Preferences(appContext, PREFERENCES_STORE_NAME)
        }
    }
}

/**
 * Retrieve the Preferences instance.
 * Throws if [initialize] has not been called yet.
 */
fun Preferences.Companion.getInstance(): Preferences {
    return INSTANCE ?: throw IllegalStateException(
        "Preferences must be initialized before use"
    )
}

// AppModules defines the dependency injection graph for core application services.
// Each binding specifies how instances are created and shared across the app.
private val AppModules = module {

    // --- Preferences Store ---
    // Provide a single shared Preferences instance backed by the app's preference database.
    // This allows consistent access to persisted settings and state throughout the app.
    factory { Preferences.getInstance() }

    // --- Android Resources ---
    // Expose the Android Resources object via DI.
    // Useful for accessing strings, drawables, dimensions, and other resource values.
    factory { androidApplication().resources }

    // --- Snackbar Host State ---
    // Provide a single SnackbarHostState instance.
    // This manages the state of snackbars shown in Compose UI.
    singleOf(::SnackbarHostState)

    // --- Media Provider ---
    // Provide a singleton MediaProvider instance.
    // Handles access to media files, streams, and related functionality.
    factory { MediaProvider.getInstance() }

    // --- ViewModels ---
    viewModel() {params ->
        FilesViewModel(get(), if (params.isEmpty()) null else params.get())
    }
}