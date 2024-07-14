/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 28-06-2024.
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

package com.zs.megahertz.common

/**
 * Enum class representing different night modes.
 *
 * Night mode determines how the app's UI should appear during night time.
 * The mode can be set using AppCompatDelegate.setLocalNightMode(int).
 */
enum class NightMode {
    /**
     * Night mode which uses always uses a light mode, enabling {@code notnight} qualified
     * resources regardless of the time.
     *
     * @see #setLocalNightMode(int)
     */
    YES,

    /**
     * Night mode which uses always uses a dark mode, enabling {@code night} qualified
     * resources regardless of the time.
     *
     * @see #setLocalNightMode(int)
     */
    NO,

    /**
     * Mode which uses the system's night mode setting to determine if it is night or not.
     *
     * @see #setLocalNightMode(int)
     */
    FOLLOW_SYSTEM,
}