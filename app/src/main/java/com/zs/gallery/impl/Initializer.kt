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
import androidx.lifecycle.SavedStateHandle
import androidx.startup.Initializer
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.zs.api.store.MediaProvider
import com.zs.compose_ktx.toast.ToastHostState
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private const val TAG = "Initializer"

private val appModules = module {
    // Define Koin modules for dependency injection.
    // Declare a singleton instance of Preferences.
    single { com.primex.preferences.Preferences(get(), "shared_preferences") }
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
        return emptyList()
    }
}