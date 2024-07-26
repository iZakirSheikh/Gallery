/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 18-07-2024.
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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesomeMotion
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primex.preferences.Key
import com.primex.preferences.Preferences
import com.zs.gallery.R
import com.zs.gallery.common.NightMode
import com.zs.gallery.settings.Preference
import com.zs.gallery.settings.Settings
import com.zs.gallery.settings.SettingsViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

context (ViewModel) @Suppress("NOTHING_TO_INLINE")
@Deprecated("find new solution.")
inline fun <T> Flow<T>.asComposeState(initial: T): State<T> {
    val state = mutableStateOf(initial)
    onEach { state.value = it }.launchIn(viewModelScope)
    return state
}

context (Preferences, ViewModel)
private fun <T> Flow<T>.asComposeState(): State<T> = asComposeState(runBlocking { first() })

class SettingsViewModel() : KoinViewModel(), SettingsViewState {
    override val nightMode: Preference<NightMode> by with(preferences) {
        preferences[Settings.KEY_NIGHT_MODE].map {
            Preference(
                value = it,
                title = getText(R.string.pref_app_theme),
                summery = getText(R.string.pref_app_theme_summery),
                vector = Icons.Outlined.Lightbulb
            )
        }.asComposeState()
    }

    override val trashCanEnabled: Preference<Boolean> by with(preferences) {
        preferences[Settings.KEY_TRASH_CAN_ENABLED].map {
            Preference(
                value = it,
                title = getText(R.string.pref_trash_enabled),
                summery = getText(R.string.pref_trash_can_summery),
                vector = Icons.Outlined.Recycling
            )
        }.asComposeState()
    }

    override val gridItemSizeMultiplier by with(preferences) {
        preferences[Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER].map {
            Preference(
                value = it,
                title = getText(R.string.pref_grid_item_size_multiplier),
                summery = getText(R.string.pref_grid_item_size_multiplier_summery),
                vector = Icons.Outlined.GridView
            )
        }.asComposeState()
    }
    override val liveGallery by with(preferences) {
        preferences[Settings.KEY_DYNAMIC_GALLERY].map {
            Preference(
                value = it,
                title = getText(R.string.pref_live_gallery),
                summery = getText(R.string.pref_live_gallery_summery),
                vector = Icons.Outlined.AutoAwesomeMotion
            )
        }.asComposeState()
    }
    override val fontScale by with(preferences) {
        preferences[Settings.KEY_FONT_SCALE].map {
            Preference(
                value = it,
                title = getText(R.string.pref_font_scale),
                summery = getText(R.string.pref_font_scale_summery),
                vector = Icons.Outlined.TextFields
            )
        }.asComposeState()
    }
    override val isSystemBarsTransparent by with(preferences) {
        preferences[Settings.KEY_TRANSPARENT_SYSTEM_BARS].map {
            Preference(
                value = it,
                title = getText(R.string.pref_transparent_system_bars),
                summery = getText(R.string.pref_transparent_system_bars_summery),
                vector = Icons.Outlined.VisibilityOff
            )
        }.asComposeState()
    }
    override val immersiveView: Preference<Boolean> by with(preferences) {
        preferences[Settings.KEY_IMMERSIVE_VIEW].map {
            Preference(
                value = it,
                title = getText(R.string.pref_immersive_view),
                summery = getText(R.string.pref_immersive_view_summery),
                vector = Icons.Outlined.Fullscreen
            )
        }.asComposeState()
    }

    override val secureMode: Preference<Boolean> by with(preferences) {
        preferences[Settings.KEY_SECURE_MODE].map {
            Preference(
                value = it,
                title = getText(R.string.pref_secure_mode),
                summery = getText(R.string.pref_secure_mode_summery),
                vector = Icons.Outlined.Security
            )
        }.asComposeState()
    }

    override fun <S, O> set(key: Key<S, O>, value: O) {
        preferences[key] = value
    }

}