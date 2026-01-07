/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
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
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

@file:Suppress("ClassName", "EnumEntryName")

package com.zs.gallery.common

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.zs.compose.foundation.OliveYellow
import com.zs.preferences.IntSaver
import com.zs.preferences.booleanPreferenceKey
import com.zs.preferences.intPreferenceKey
import com.zs.preferences.longPreferenceKey
import com.zs.preferences.stringPreferenceKey

// Enforce rule - all const in here are are snake_case.
@Suppress("SameParameterValue")
private fun Intent(action: String, data: Uri) = Intent(action).apply { this.data = data }

/**
 * A generic [Saver] implementation for any [Enum] type.
 *
 * This saver persists enum values by storing their [Enum.name] as a String,
 * and restores them by looking up the corresponding enum constant.
 *
 * Usage:
 * ```
 * val saver = enumSaver<MyEnum>()
 * val state = rememberSaveable(stateSaver = saver) { mutableStateOf(MyEnum.FIRST) }
 * ```
 *
 * This avoids writing custom savers for each enum class.
 */
@Suppress("FunctionName")
private inline fun <reified T : Enum<T>> OrdinalEnumSaver(): IntSaver<T> = object : IntSaver<T> {
    // Save enum as its name string
    override fun save(value: T): Int = value.ordinal

    // Restore enum constant by ordinal lookup
    override fun restore(value: Int): T =
        enumValues<T>()[value]
}

// --- Color saver utility ---
// Used to persist and restore Color values in preferences.
private val ColorSaver = object : IntSaver<Color> {
    override fun restore(value: Int): Color = Color(value)
    override fun save(value: Color): Int = value.toArgb()
}

/**
 * Common access point for app-level constants and resources.
 *
 * Mirrors the naming convention of Android's [R] class but provides a more
 * flexible, centralized extension. Inspired by Kotlin Multiplatform patterns,
 * this reduces direct dependency on the generated [R] class, which is often
 * cumbersome to access during typing.
 * @see string
 * @see drawable
 * @see mainfest
 * @see shape
 * @see action
 * @see spacing
 */
object Res {

    // Typealiases for direct access to Android resources (R.string, R.drawable, etc.)
    typealias string = com.zs.gallery.R.string
    typealias drawable = com.zs.gallery.R.drawable
    typealias raw = com.zs.gallery.R.raw
    // typealias plurals = com.zs.gallery.R.plurals

    /**
     * Manifest-related constants and intents.
     *
     * Provides URIs, package names, default colors, and permission lists
     * required for app configuration and external navigation.
     */
    object manifest {
        // Play Store URIs
        const val market_uri_prefix = "market://details?id="
        const val market_web_url_prefix = "http://play.google.com/store/apps/details?id="
        const val market_package = "com.android.vending"

        // Predefined external intents
        val intent_privacy_policy = Intent(
            Intent.ACTION_VIEW,
            "https://docs.google.com/document/d/1D9wswWSrt65ol7h3HLKhk31OVTqDtN4uLJ73_Rk9hT8/edit?usp=sharing".toUri()
        )
        val intent_github_issues =
            Intent(Intent.ACTION_VIEW, "https://github.com/iZakirSheikh/Gallery/issues".toUri())
        val intent_telegram =
            Intent(Intent.ACTION_VIEW, "https://t.me/audiofy_support".toUri())
        val intent_github =
            Intent(Intent.ACTION_VIEW, "https://github.com/iZakirSheikh/Gallery".toUri())
        val intent_join_beta = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/apps/testing/com.zs.gallery/join".toUri()
        )

        // --- Default Accent Colors ---
        val color_accent_light = Color(0xFF904A42)
        val color_accent_dark = Color.OliveYellow

        /**
         * Permissions required by the app.
         *
         * Dynamically built based on Android version to ensure compatibility
         * with scoped storage and legacy storage access.
         */
        @SuppressLint("BuildListAdds")
        val permissions = buildList {
            // Scoped storage permissions for Android 13 (Tiramisu) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this += android.Manifest.permission.ACCESS_MEDIA_LOCATION
                this += android.Manifest.permission.READ_MEDIA_VIDEO
                this += android.Manifest.permission.READ_MEDIA_IMAGES
            }
            // Visual media permission for Android 14 (Upside Down Cake) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                this += android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            // Legacy storage permissions for Android 10 (Q) and below
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
                this += android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                this += android.Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        /**
         * Utility to check if the current SDK version is at least [api].
         */
        @ChecksSdkIntAtLeast(parameter = 0)
        fun isAtLeast(api: Int) = Build.VERSION.SDK_INT >= api
    }

    /**
     * Preference keys used throughout the app.
     *
     * Provides strongly typed keys for storing and retrieving app settings.
     */
    object key {
        val night_mode_policy =
            intPreferenceKey("_night_mode", NightMode.FOLLOW_SYSTEM, OrdinalEnumSaver())
        val transparent_system_bars = booleanPreferenceKey(
            "_transparent_system_bars",
            defaultValue = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        )
        val app_launch_counter = intPreferenceKey("_launch_counter", 0)
        val dynamic_colors =
            booleanPreferenceKey("_dynamic_colors", manifest.isAtLeast(Build.VERSION_CODES.S))
        val dark_accent_color =
            intPreferenceKey("_dark_accent_color", manifest.color_accent_dark, ColorSaver)
        val light_accent_color =
            intPreferenceKey("_light_accent_color", manifest.color_accent_light, ColorSaver)
        val app_version_code = longPreferenceKey("_app_version_code", -1)
        val app_config = stringPreferenceKey("_app_config")
    }

    /**
     * Standardized spacing values for Compose layouts.
     *
     * Provides consistent [Dp] values and [Arrangement] gaps for UI components.
     */
    object spacing {
        val x_small: Dp = 4.dp
        val small: Dp = 8.dp
        val medium: Dp = 12.dp
        val normal: Dp = 16.dp
        val large: Dp = 22.dp
        val x_large: Dp = 32.dp

        // Gap arrangements for list/grid items
        val gap_x_small = Arrangement.spacedBy(x_small)
        val gap_small = Arrangement.spacedBy(small)
        val gap_medium = Arrangement.spacedBy(medium)
        val gap_large = Arrangement.spacedBy(large)
    }

    /**
     * Common access to Compose shapes.
     */
    object shape {
        val circle = CircleShape
        val rectangle = RectangleShape
    }

    /**
     * Enum representing common user actions with associated icons and labels.
     *
     * Provides a unified mapping between drawable resources and string labels.
     */
    enum class action(@DrawableRes val icon: Int, @StringRes val label: Int) {
        select_all(drawable.ic_select_all_filled, string.select_all),
        airdrop(drawable.ic_share_outline, string.airdrop),
        like(drawable.ic_star_outline, string.like),
        unlike(drawable.ic_star_filled, string.unlike),
        delete(drawable.ic_delete_outline, string.delete),
        share(drawable.ic_share_outline, string.share),
        trash(drawable.ic_remove_outline, string.trash),
        restore(drawable.ic_restore_filled, string.restore)
    }
}