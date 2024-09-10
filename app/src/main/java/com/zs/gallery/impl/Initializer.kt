/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 21-07-2024.
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

package com.zs.gallery.impl

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.startup.Initializer
import coil.Coil
import coil.ImageLoader
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.primex.preferences.value
import com.zs.domain.store.MediaProvider
import com.zs.foundation.toast.ToastHostState
import com.zs.gallery.common.ThumbnailFetcher
import com.zs.gallery.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private const val TAG = "Initializer"

private val appModules = module {
    // Define Koin modules for dependency injection.
    // Declare a singleton instance of Preferences.
    single {
        // Initialize Preferences
        val preferences = com.primex.preferences.Preferences(get(), "shared_preferences")
        // Retrieve the current launch counter value, defaulting to 0 if not set
        val counter = preferences.value(Settings.KEY_LAUNCH_COUNTER) ?: 0
        // Increment the launch counter for cold starts
        preferences[Settings.KEY_LAUNCH_COUNTER] = counter + 1
        Log.d(TAG, "Cold start counter: ${preferences.value(Settings.KEY_LAUNCH_COUNTER)}")
        // Return the preferences instance
        preferences
    }
    // Declare a ViewModel dependency (lifecycle managed by Koin).
    // viewModel { BatteryViewModel(get()) }
    singleOf(::ToastHostState)
    factory { MediaProvider(get()) }
    factory { androidContext().resources }
    // ViewModels
    viewModel { SettingsViewModel() }
    viewModel { TimelineViewModel(get()) }
    viewModel { FoldersViewModel(get()) }
    viewModel { (handle: SavedStateHandle) -> ViewerViewModel(handle, get()) }
    viewModel { (handle: SavedStateHandle) -> FolderViewModel(handle, get()) }
    viewModel { AlbumViewModel(get()) }
    viewModel { TrashViewModel(get()) }
}

class KoinInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        startKoin {
            androidContext(context)
            modules(appModules)
        }
    }

    // No dependencies on other libraries.
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

class FirebaseInitializer : Initializer<Unit> {
    override fun create(context: Context): Unit {
        // Initialize Firebase
        FirebaseApp.initializeApp(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}

class CrashlyticsInitializer : Initializer<Unit> {
    override fun create(context: Context): Unit {
        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(FirebaseInitializer::class.java)
    }
}

class CoilInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Coil.setImageLoader(
            ImageLoader.Builder(context)
                .components {
                    add(ThumbnailFetcher.Factory())
                }.build()
        )
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> =
        mutableListOf()
}