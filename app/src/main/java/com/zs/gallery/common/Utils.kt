/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 03-04-2025.
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

package com.zs.gallery.common

import android.content.pm.PackageManager
import android.os.Build
import android.view.Window
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import com.zs.compose.foundation.runCatching
import com.zs.compose.theme.AppTheme

private const val TAG = "Common-Utils"

/**
 * Represents a sorting order and associated grouping or ordering action.
 *
 * @property first Specifies whether the sorting is ascending or descending.
 * @property second Specifies the action to group by or order by.
 */
typealias Filter = Pair<Boolean, Action>

/**
 * Represents a mapping from a string key to a list of items of type T.
 *
 * @param T The type of items in the list.
 */
typealias Mapped<T> = Map<CharSequence, List<T>>

/**
 * Controls the color of both the status bar and the navigation bar.
 *
 * - When setting a value, it sets the same color for both the status bar and navigation bar.
 * - The color value is converted to an ARGB format using [Color.toArgb].
 *
 * Note: Getting the current system bar color is not supported and will throw an error if accessed.
 *
 * @property value The color to be applied to both system bars.
 * @throws UnsupportedOperationException when trying to retrieve the current system bar color.
 */
var Window.systemBarsColor: Color
    set(value) {
        statusBarColor = value.toArgb()
        navigationBarColor = value.toArgb()
    }
    get() = error("Not supported!")

/**
 * Controls whether both the system status bars and navigation bars have a light appearance.
 *
 * - When `true`, both the status bar and navigation bar will use a light theme (dark icons on a light background).
 * - When `false`, both will use a dark theme (light icons on a dark background).
 *
 * Setting this property adjusts both `isAppearanceLightStatusBars` and `isAppearanceLightNavigationBars`.
 *
 * @property value `true` to apply light appearance, `false` for dark appearance.
 */
var WindowInsetsControllerCompat.isAppearanceLightSystemBars: Boolean
    set(value) {
        isAppearanceLightStatusBars = value
        isAppearanceLightNavigationBars = value
    }
    get() = isAppearanceLightStatusBars && isAppearanceLightNavigationBars

/**
 * Gets the package info of this app using the package manager.
 * @return a PackageInfo object containing information about the app, or null if an exception occurs.
 * @see android.content.pm.PackageManager.getPackageInfo
 */
fun PackageManager.getPackageInfoCompat(pkgName: String) =
    runCatching (TAG + "_review") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            getPackageInfo(pkgName, PackageManager.PackageInfoFlags.of(0))
        else
            getPackageInfo(pkgName, 0)
    }

private const val ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (…)

/**
 * Ellipsizes this CharSequence, adding a horizontal ellipsis (…) if it is longer than [after] characters.
 *
 * @param after The maximum length of the CharSequence before it is ellipsized.
 * @return The ellipsized CharSequence.
 */
fun CharSequence.ellipsize(after: Int): CharSequence =
    if (this.length > after) this.substring(0, after) + ELLIPSIS_NORMAL else this

@OptIn(ExperimentalSharedTransitionApi::class)
private val DEFULT_BOUNDS_TRANSFORM =  BoundsTransform   { _, _ -> tween(180) }
@OptIn(ExperimentalSharedTransitionApi::class)
val AppTheme.DefaultBoundsTransform get() =  DEFULT_BOUNDS_TRANSFORM