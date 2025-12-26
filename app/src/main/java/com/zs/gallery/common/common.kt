/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 26 of Dec 2025
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
 * Last Modified by sheik on 26 of Dec 2025
 */

package com.zs.gallery.common

import com.zs.gallery.common.const.Registry

/** A short-hand for [Registry]. */
typealias Reg = Registry

/**
 * Defines the strategies for extracting a source color accent
 * to construct the application theme.
 */
enum class ColorPalette { MANUAL, DEFAULT, WALLPAPER }

/**
 * Represents the available options for applying dark theme
 * behavior within the application.
 */
enum class NightMode { YES, NO, FOLLOW_SYSTEM }

private const val ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (…)

/**
 * Ellipsizes this CharSequence, adding a horizontal ellipsis (…) if it is longer than [after] characters.
 *
 * @param after The maximum length of the CharSequence before it is ellipsized.
 * @return The ellipsized CharSequence.
 */
fun CharSequence.ellipsize(after: Int): CharSequence =
    if (this.length > after) this.substring(0, after) + ELLIPSIS_NORMAL else this

