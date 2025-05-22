/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 05-05-2025.
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

package com.zs.core

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.view.WindowInsetsControllerCompat
import com.zs.core.common.runCatching

private const val TAG = "CoreUtils"

fun PackageManager.findActivity(value: Intent, pkg: String): Intent? {
    val info = queryIntentActivities(value, 0).find {
        it.activityInfo.packageName.contains(pkg)
    }
    if (info == null)
        return null
   return Intent(value).apply {
        component = ComponentName(
            info.activityInfo.packageName,
            info.activityInfo.name
        )
    }
}

/**
 * Creates an [Intent] with the given [action] and applies the [block] to it.
 *
 * @param action The action to perform, such as [Intent.ACTION_VIEW].
 * @param block A lambda expression that configures the intent.
 * @return The created [Intent].
 */
inline fun Intent(action: String, block: Intent.() -> Unit) =
    Intent(action).apply(block)

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
