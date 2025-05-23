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


import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import android.view.Window
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDestination
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.ZoomableState

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
 * Extracts the domain portion from a [NavDestination]'s route.
 *
 * The domain is considered to be the part of the route before the first '/'.
 * For example, for the route "settings/profile", the domain would be "settings".
 *
 * @return The domain portion of the route, or null if the route is null or does not contain a '/'.
 */
val NavDestination.domain: String?
    get() {
        // Get the route, or return null if it's not available.
        val route = route ?: return null

        // Find the index of the first '/' character.
        val index = route.indexOf('/')

        // Return the substring before the '/' if it exists, otherwise return the entire route.
        return if (index == -1) route else route.substring(0, index)
    }

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

private const val ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (…)

/**
 * Ellipsizes this CharSequence, adding a horizontal ellipsis (…) if it is longer than [after] characters.
 *
 * @param after The maximum length of the CharSequence before it is ellipsized.
 * @return The ellipsized CharSequence.
 */
fun CharSequence.ellipsize(after: Int): CharSequence =
    if (this.length > after) this.substring(0, after) + ELLIPSIS_NORMAL else this

/**
 * Operator function to retrieve a value from [SavedStateHandle] using a specified key.
 * It supports type-safe retrieval for common types such as String, Int, Long, Float, Boolean, and Uri.
 *
 * This function uses [SavedStateHandle.get] to retrieve the value and then casts or converts it
 * to the specified type [T]. If the value cannot be converted, it returns null.
 *
 * @param T The reified type to which the retrieved value should be converted.
 * @param key The key under which the value is stored in [SavedStateHandle].
 * @return The value associated with the key, cast to type [T], or null if the value is not found or cannot be cast.
 * @throws IllegalArgumentException if an unsupported type is requested.
 */
inline operator fun <reified T : Any> SavedStateHandle.invoke(key: String): T? {
    // Retrieve the value from SavedStateHandle as String.
    val value = get<String>(key) ?: return null
    // Check the requested type.
    val result = when (T::class) {
        // If String, return the value directly.
        String::class -> value
        Int::class -> value.toInt()
        Long::class -> value.toLong()
        Float::class -> value.toFloat()
        Boolean::class -> value.toBoolean()
        Uri::class -> Uri.parse(value)
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
    // Safely cast the result to the requested type.
    return result as T
}

/**
 * Retrieves the trimmed, non-empty string representation of the text field's content.
 * If the trimmed text is empty, returns `null`.
 *
 * @return The trimmed text as a [String], or `null` if the trimmed text is empty.
 */
val TextFieldState.raw get() = text.trim().toString().ifEmpty { null }

@OptIn(FlowPreview::class)

        /**
         * Returns a flow that mirrors the original flow, but debounces emissions after the first one.
         *
         * The first emission from the original flow is emitted immediately. Subsequent emissions
         * are debounced by the specified [delayMillis].
         *
         * @param delayMillis The duration in milliseconds to debounce subsequent emissions.
         * @return A flow that debounces emissions after the first one.
         *
         * @see [debounce]
         */
fun <T> Flow<T>.debounceAfterFirst(delayMillis: Long): Flow<T> {
    var firstEmission = true
    return this
        .debounce {
            if (firstEmission) {
                firstEmission = false
                0L
            } else {
                delayMillis
            }
        }
}

/**
 * Formats a file size in bytes to a human-readable string.
 *
 * @param bytes Thefile size in bytes.
 * @return The formatted file size string.
 */
fun Context.fileSizeFormatted(bytes: Long) =
    Formatter.formatFileSize(this, bytes)

/**  Scales and centers content based on size. */
fun ZoomableState.scaledInsideAndCenterAlignedFrom(size: Size) {
    // Do nothing if intrinsic size is unknown
    if (size.isUnspecified) return

    // Scale and center content based on intrinsic size
    // TODO - Make this suspend fun instead of runBlocking
    setContentLocation(
        ZoomableContentLocation.scaledInsideAndCenterAligned(
            size
        )
    )
}