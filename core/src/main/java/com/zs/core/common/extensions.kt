/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 02-04-2025.
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

package com.zs.core.common

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import java.util.regex.Matcher
import java.util.regex.Pattern

private const val TAG = "common-extensions"

/**
 * Executes the given [block] function within a try-catch block, logging any exceptions that occur.
 *
 * This function provides a convenient way to run code that might throw exceptions without crashing the application.
 * If an exception is caught, it will be logged using the provided [tag] and the exception's stack trace,
 * and the function will return `null`. If the block executes successfully, it returns the result of the block.
 *
 * @param tag The tag used for logging any exceptions. Typically, this should be a string representing the class or module
 *            where this function is being called.
 * @param block The function block to execute. This block can be any function that operates on the receiver object [T] and returns a value of type [R].
 * @return The result of executing the [block] function, or `null` if an exception was caught.
 *
 * Example Usage:
 * ```kotlin
 * val result = someObject.runCatching("MyClass", {
 *     // Code that might throw an exception
 *     someMethodThatMightFail()
 * })
 *
 * if (result != null) {
 *     // Process the successful result
 * } else {
 *   // Handle the exception (it was already logged)
 * }
 * ```
 */
internal inline fun <T, R> T.runCatching(tag: String, block: T.() -> R): R? {
    return try {
        block()
    } catch (e: Throwable) {
        Log.e(tag, "runCatching: $e")
        null
    }
}

//language=RegExp
private val ISO6709LocationPattern = Pattern.compile("([+\\-][0-9.]+)([+\\-][0-9.]+)")

/**
 * This method parses the given string representing a geographic point location by coordinates in ISO 6709 format
 * and returns the latitude and the longitude in float. If `location` is not in ISO 6709 format,
 * this method returns `null`
 *
 * @param location a String representing a geographic point location by coordinates in ISO 6709 format
 * @return `null` if the given string is not as expected, an array of floats with size 2,
 * where the first element represents latitude and the second represents longitude, otherwise.
 */
val MediaMetadataRetriever.latLong: DoubleArray?
    get() = runCatching(TAG) {
        val location =
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION) ?: return@runCatching null
        val m: Matcher = ISO6709LocationPattern.matcher(location)
        if (m.find() && m.groupCount() == 2) {
            val latstr: String = m.group(1) ?: return@runCatching null
            val lonstr: String = m.group(2) ?: return@runCatching null
            val lat = latstr.toDouble()
            val lon = lonstr.toDouble()
            doubleArrayOf(lat, lon)
        } else null
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
