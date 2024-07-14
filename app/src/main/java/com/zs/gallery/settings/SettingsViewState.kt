/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 23-07-2024.
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

package com.zs.gallery.settings

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import com.primex.preferences.Key
import com.zs.gallery.common.Route
import com.primex.preferences.StringSaver
import com.primex.preferences.booleanPreferenceKey
import com.primex.preferences.floatPreferenceKey
import com.primex.preferences.stringPreferenceKey
import com.zs.gallery.common.NightMode

object RouteSettings : Route

object Settings {

    // The keys for the preferences
    private const val PREFIX = "global"

    /**
     * Preference key for the night mode setting (String).
     * Possible values: "on", "off","system"
     */
    val KEY_NIGHT_MODE =
        stringPreferenceKey(
            "${PREFIX}_night_mode",
            NightMode.FOLLOW_SYSTEM,
            object : StringSaver<NightMode> {
                override fun restore(value: String): NightMode {
                    return NightMode.valueOf(value)
                }

                override fun save(value: NightMode): String {
                    return value.name
                }
            }
        )

    /**
     * Preference key for enabling/disabling the trash can feature (boolean).
     * When enabled, deleted items are moved to a trashcan instead of being permanently deleted.
     */
    val KEY_TRASH_CAN_ENABLED =
        booleanPreferenceKey(PREFIX + "_trash_can_enabled", defaultValue = true)

    /**
     * Grid Size Multiplier.
     * Preference key for the grid item size multiplier (float between 0.6 and 1.5).
     * Controls the size of grid items based on user preference.
     */
    val KEY_GRID_ITEM_SIZE_MULTIPLIER =
        floatPreferenceKey(PREFIX + "_grid_item_size_multiplier", defaultValue = 1.0f)

    /**
     * Preference key for enabling/disabling live gallery mode (boolean).
     * When enabled, videos in the gallery autoplay while browsing.
     */
    val KEY_DYNAMIC_GALLERY =
        booleanPreferenceKey(PREFIX + "_dynamic_gallery", defaultValue = true)

    /**
     * Font Scale Preference.
     *
     * This constant represents the scaling factor applied to the system font.
     * Users can adjust this value to control the size of the font, with the default being the system default font size.
     * The acceptable range is from 1.0f to 2.0f (twice the system default size).
     */
    val KEY_FONT_SCALE = floatPreferenceKey(PREFIX + "_font_scale", -1f)

    /**
     * Preference key for controlling the appearance of system bars (boolean).
     * When enabled, system bars become transparent.
     * When disabled, a dark background is applied to the status bar.
     */
    val KEY_TRANSPARENT_SYSTEM_BARS =
        booleanPreferenceKey(PREFIX + "_transparent_system_bars", defaultValue = false)

    val KEY_IMMERSIVE_VIEW =
        booleanPreferenceKey(PREFIX + "_immersive_view", defaultValue = false)

    /**
     * Preference key for enabling/disabling immersive view mode (boolean).
     * When enabled, system bars are hidden to provide a more immersive experience.
     * When disabled, system bars are shown.
     */
    val KEY_FAVOURITE_FILES =
        stringPreferenceKey(
            "${PREFIX}_favourite_files",
            emptyList(),
            object : StringSaver<List<Long>> {
                val SEPARATOR = ","
                override fun restore(value: String): List<Long> {
                    return value.split(SEPARATOR).map { it.toLong() }
                }

                override fun save(value: List<Long>): String {
                    return value.joinToString(SEPARATOR)
                }
            }
        )

    /**
     * Preference key for enabling/disabling secure mode (boolean).
     * When enabled, the app's content is obscured in theapp preview, enhancing privacy.
     */
    val KEY_SECURE_MODE =
        booleanPreferenceKey(PREFIX + "_secure_gallery", defaultValue = false)
}

/**
 * Immutable data class representing a preference.
 *
 * @property value The value of the preference.
 * @property title The title text of the preference.
 * @property vector The optional vector image associated with the preference.
 * @property summery The optional summary text of the preference.
 * @param P The type of the preference value.
 */
@Stable
data class Preference<out P>(
    val value: P,
    @JvmField val title: CharSequence,
    val vector: ImageVector? = null,
    @JvmField val summery: CharSequence? = null,
)

interface SettingsViewState {
    val nightMode: Preference<NightMode>
    val trashCanEnabled: Preference<Boolean>
    val gridItemSizeMultiplier: Preference<Float>
    val liveGallery: Preference<Boolean>
    val fontScale: Preference<Float>

    val isSystemBarsTransparent: Preference<Boolean>
    val immersiveView: Preference<Boolean>
    val secureMode: Preference<Boolean>

    fun <S, O> set(key: Key<S, O>, value: O)
}