/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 30 of Dec 2025
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
 * Last Modified by sheik on 30 of Dec 2025
 *
 */

package com.zs.common.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * Shows a platform Toast message with the given text.
 *
 * This function uses the standard Android Toast class to display a short message to the user.
 *
 * @param message The text message to display in the Toast.
 * @param priority The duration of the Toast.
 */
fun Context.showPlatformToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * @see showPlatformToast
 */
fun Context.showPlatformToast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


/**
 * Checks if a given permission is granted for the application in the current context.
 *
 * @param permission The permission string tocheck.
 * @return `true` if the permission is granted, `false` otherwise.
 */
fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * @see isPermissionGranted
 */
fun Context.checkSelfPermissions(values: List<String>) =
    values.all { isPermissionGranted(it) }
