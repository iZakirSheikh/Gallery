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

package com.zs.gallery.settings

import com.primex.preferences.StringSaver
import com.primex.preferences.booleanPreferenceKey
import com.primex.preferences.floatPreferenceKey
import com.primex.preferences.stringPreferenceKey
import com.zs.megahertz.common.NightMode

/**
 * Type alias for accessing the companion object of [SettingsViewState].
 * This provides convenient access to shared properties or functions within the [SettingsViewState] class.
 */
typealias Settings = SettingsViewState.Companion

interface SettingsViewState {
    companion object {
        const val DOMAIN = "route_settings"
        const val ROUTE = DOMAIN
        fun direction() = ROUTE
        
        private const val PREFIX = "global"
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
        val TRASH_CAN_ENABLED =
            booleanPreferenceKey(PREFIX + "_trash_can_enabled", defaultValue = true)

        /**
         * Grid Size Multiplier.
         *
         * This constant represents a value between 0.6 and 1.5f that determines the size of items in a grid.
         * Users can adjust this multiplier to make grid items appear smaller or larger based on their preferences.
         *
         * Usage:
         * ```kotlin
         * val GRID_ITEM_SIZE_MULTIPLIER = floatPreferenceKey(PREFIX + "_font_scale", defaultValue = 1.0f)
         * ```
         */
        val GRID_ITEM_SIZE_MULTIPLIER =
            floatPreferenceKey(PREFIX + "_grid_item_size_multiplier", defaultValue = 1.0f)
    }
}