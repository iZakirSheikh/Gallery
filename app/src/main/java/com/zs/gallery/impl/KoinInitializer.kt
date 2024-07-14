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

package com.zs.gallery.impl

import android.content.Context
import androidx.startup.Initializer
import com.zs.api.store.MediaProvider
import com.zs.compose_ktx.toast.ToastHostState
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module



private val appModules = module {
    // Define Koin modules for dependency injection.
    // Declare a singleton instance of Preferences.
    single { com.primex.preferences.Preferences(get(), "shared_preferences") }
    // Declare a ViewModel dependency (lifecycle managed by Koin).
    // viewModel { BatteryViewModel(get()) }
    factory { ToastHostState() }
    factory { MediaProvider(get()) }
    factory { androidContext().resources }
    viewModel { FilesViewModel() }
    viewModel { FoldersViewModel(get()) }
}

class KoinInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        startKoin {
            androidContext(context)
            modules(appModules)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}