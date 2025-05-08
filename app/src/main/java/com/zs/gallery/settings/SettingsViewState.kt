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

package com.zs.gallery.settings

import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.zs.core.store.MediaProvider
import com.zs.gallery.R
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.Route
import com.zs.gallery.settings.Settings.KEY_APP_LOCK_TIME_OUT
import com.zs.gallery.settings.Settings.KEY_DYNAMIC_GALLERY
import com.zs.gallery.settings.Settings.KEY_FAVOURITE_FILES
import com.zs.gallery.settings.Settings.KEY_FONT_SCALE
import com.zs.gallery.settings.Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER
import com.zs.gallery.settings.Settings.KEY_IMMERSIVE_VIEW
import com.zs.gallery.settings.Settings.KEY_LAUNCH_COUNTER
import com.zs.gallery.settings.Settings.KEY_NIGHT_MODE
import com.zs.gallery.settings.Settings.KEY_SECURE_MODE
import com.zs.gallery.settings.Settings.KEY_TRANSPARENT_SYSTEM_BARS
import com.zs.gallery.settings.Settings.KEY_TRASH_CAN_ENABLED
import com.zs.gallery.settings.Settings.STANDARD_TILE_SIZE
import com.zs.preferences.Key
import com.zs.preferences.StringSaver
import com.zs.preferences.booleanPreferenceKey
import com.zs.preferences.floatPreferenceKey
import com.zs.preferences.intPreferenceKey
import com.zs.preferences.stringPreferenceKey

object RouteSettings : Route

private val provider by lazy {
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
}

private val OutfitFontFamily = FontFamily("Outfit")
val FontFamily.Companion.OutfitFontFamily get() = com.zs.gallery.settings.OutfitFontFamily
val DancingScriptFontFamily = FontFamily("Dancing Script")
val FontFamily.Companion.DancingScriptFontFamily get() = com.zs.gallery.settings.DancingScriptFontFamily

/**
 * Creates a [FontFamily] from the given Google Font name.
 *
 * @param name The name of theGoogle Font to use.
 * @return A [FontFamily] object
 */
@Stable
private fun FontFamily(name: String): FontFamily {
    // Create a GoogleFont object from the given name.
    val font = GoogleFont(name)
    // Create a FontFamily object with four different font weights.
    return FontFamily(
        Font(
            fontProvider = provider,
            googleFont = font,
            weight = FontWeight.Light
        ),
        Font(
            fontProvider = provider,
            googleFont = font,
            weight = FontWeight.Medium
        ),
        Font(
            fontProvider = provider,
            googleFont = font,
            weight = FontWeight.Normal
        ),
        Font(
            fontProvider = provider,
            googleFont = font,
            weight = FontWeight.Bold
        ),
    )
}

/**
 * Represents the state of the [Settings] screen.
 */
interface SettingsViewState {
    fun <S, O> set(key: Key<S, O>, value: O)
}

/**
 * ## Settings
 *
 * This object contains various preference keys and their default values used throughout the app.
 *
 * @property STANDARD_TILE_SIZE The default min size of the single cell in grid.
 * @property KEY_NIGHT_MODE Preference key for the night mode strategy [NightMode].
 * @property KEY_TRASH_CAN_ENABLED Preference key for enabling/disabling the trash can feature
 *                                (boolean). When enabled, deleted items are moved to a trashcan
 *                                instead of being permanently deleted.
 * @property KEY_GRID_ITEM_SIZE_MULTIPLIER Grid Size Multiplier. Preference key for the grid item
 *                                         size multiplier (float between 0.6 and 1.5). Controls
 *                                         the size of grid items based on user preference.
 * @property KEY_DYNAMIC_GALLERY Preference key for enabling/disabling live gallery mode (boolean).
 *                              When enabled, videos in the gallery autoplay while browsing.
 * @property KEY_FONT_SCALE Font Scale Preference. This constant represents the scaling factor
 *                          applied to the system font. Users can adjust this value to control
 *                          the size of the font, with the default being the system default font
 *                          size. The acceptable range is from 1.0f to 2.0f (twice the system
 *                          default size).
 * @property KEY_TRANSPARENT_SYSTEM_BARS Preference key for controlling the appearance of system
 *                                       bars (boolean). When enabled, system bars become
 *                                       transparent. When disabled, a dark background is applied
 *                                       to the status bar.
 * @property KEY_IMMERSIVE_VIEW Preference key for enabling/disabling immersive view mode (boolean).
 *                              When enabled, system bars are hidden to provide a more immersive
 *                              experience. When disabled, system bars are shown.
 * @property KEY_FAVOURITE_FILES Preference key for enabling/disabling immersive view mode (boolean).
 *                               When enabled, system bars are hidden to provide a more immersive
 *                               experience. When disabled, system bars are shown.
 * @property KEY_SECURE_MODE Preference key for enabling/disabling secure mode (boolean). When
 *                           enabled, the app's content is obscured in the app preview,
 *                           enhancing privacy.
 * @property KEY_LAUNCH_COUNTER The counter counts the number of times this app was launched.
 * @property KEY_APP_LOCK_TIME_OUT Representing the timeout duration (in minutes) for app lock.
 * This preference determines how long the app can be in the background
 * before the user needs to authenticate to regain access.
 *
 * Possible values:
 *
 * - `-1`: App lock is disabled.
 * - `0`:  The app locks immediately when it enters the background.
 * - `1`:  The app locks after 1 minute in the background.
 * - `30`: The app locks after 30 minutes in the background.
 * - ... and so on.
 */
object Settings {
    // The keys for the preferences
    private const val PREFIX = "global"
    val STANDARD_TILE_SIZE = 100.dp
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
    val KEY_TRASH_CAN_ENABLED =
        booleanPreferenceKey(PREFIX + "_trash_can_enabled", defaultValue = true)
    val KEY_GRID_ITEM_SIZE_MULTIPLIER =
        floatPreferenceKey(PREFIX + "_grid_item_size_multiplier", defaultValue = 1.0f)
    val KEY_DYNAMIC_GALLERY =
        booleanPreferenceKey(PREFIX + "_dynamic_gallery", defaultValue = true)
    val KEY_FONT_SCALE = floatPreferenceKey(PREFIX + "_font_scale", -1f)
    val KEY_TRANSPARENT_SYSTEM_BARS =
        booleanPreferenceKey(
            PREFIX + "_transparent_system_bars",
            defaultValue = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        )
    val KEY_IMMERSIVE_VIEW =
        booleanPreferenceKey(PREFIX + "_immersive_view", defaultValue = false)
    val KEY_FAVOURITE_FILES =
        stringPreferenceKey(
            "${PREFIX}_favourite_files",
            emptyList(),
            object : StringSaver<List<Long>> {
                val SEPARATOR = ","
                override fun restore(value: String): List<Long> {
                    if (value.isEmpty()) return emptyList()
                    return value.split(SEPARATOR).map { it.toLong() }
                }

                override fun save(value: List<Long>): String {
                    return value.joinToString(SEPARATOR)
                }
            }
        )
    val KEY_SECURE_MODE =
        booleanPreferenceKey(PREFIX + "_secure_gallery", defaultValue = false)
    val KEY_LAUNCH_COUNTER =
        intPreferenceKey(PREFIX + "_launch_counter", 0)
    val KEY_APP_LOCK_TIME_OUT =
        intPreferenceKey("${PREFIX}_app_lock_time_out", -1)
    val KEY_USE_ACCENT_IN_NAV_BAR =
        booleanPreferenceKey("use_accent_in_nav_bar", false)
    val KEY_DYNAMIC_COLORS =
        booleanPreferenceKey(
            PREFIX + "_dynamic_colors",
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )

    val DefaultFontFamily get() = FontFamily.Default

    val FeedbackIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:helpline.prime.zs@gmail.com".toUri()
        putExtra(Intent.EXTRA_SUBJECT, "Feedback/Suggestion for Audiofy")
    }
    val PrivacyPolicyIntent = Intent(Intent.ACTION_VIEW).apply {
        data =
            "https://docs.google.com/document/d/1D9wswWSrt65ol7h3HLKhk31OVTqDtN4uLJ73_Rk9hT8/edit?usp=sharing".toUri()
    }
    val GitHubIssuesPage = Intent(Intent.ACTION_VIEW).apply {
        data = "https://github.com/iZakirSheikh/Gallery/issues".toUri()
    }
    val TelegramIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://t.me/audiofy_support".toUri()
    }
    val GithubIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://github.com/iZakirSheikh/Gallery".toUri()
    }
    val JoinBetaIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://play.google.com/apps/testing/com.zs.gallery/join".toUri()
    }
    val ShareAppIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Hey, check out this cool app: [https://play.google.com/store/apps/details?id=com.googol.android.apps.photos&pcampaignid=web_share]"
        )
    }

    /**
     * Builds intent to set [uri] as wallpaper.
     */
    fun Wallpaper(uri: Uri): Intent =
        com.zs.core.Intent("android.service.wallpaper.CROP_AND_SET_WALLPAPER") {
            setDataAndType(uri, "image/*")
            putExtra("mimeType", "image/*") // Specifies the MIME type of the image
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grants temporary read permission to the wallpaper app
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

    /**
     * Builds an intent to search file with Google Lens.
     */
    fun GoogleLens(file: Uri) =
        com.zs.core.Intent(Intent.ACTION_SEND) {
            /* Extracted from manifest of Google App
                 <activity android:theme="resourceId:0x7f160e0a" android:name="com.google.android.apps.search.lens.LensShareEntryPointActivity" android:exported="true" android:process=":search">
                 <intent-filter android:label="Search image">
                 <action android:name="android.intent.action.SEND" />
                 <category android:name="android.intent.category.DEFAULT" />
                 <data android:mimeType="image/jpeg" />
                 <data android:mimeType="image/png" />
                 </intent-filter>
                 </activity>*/
            component = ComponentName(
                "com.google.android.googlequicksearchbox",
                "com.google.android.apps.search.lens.LensShareEntryPointActivity"
            )
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, file)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }


    fun NearByShare(vararg uri: Uri) =
        com.zs.core.Intent("com.google.android.gms.SHARE_NEARBY") {
//    setClassName(
//        "com.google.android.gms",
//        "com.google.android.gms.nearby.sharing.ReceiveSurfaceActivity"
//    )
// Extracted from manifest of QuickShare
// Keep an eye on it.
//    <activity android:theme="resourceId:0x7f160c16" android:label="Quick Share" android:icon="res/782.xml" android:name="com.google.android.gms.nearby.sharing.send.SendActivity" android:enabled="true" android:exported="true" android:process="com.google.android.gms.ui" android:taskAffinity="" android:documentLaunchMode="2" android:maxRecents="1" android:resizeableActivity="true">
//    <intent-filter>
//    <action android:name="android.intent.action.SEND"/>
//    <action android:name="android.intent.action.SEND_MULTIPLE"/>
//    <action android:name="com.google.android.gms.SHARE_NEARBY"/>
//    <category android:name="android.intent.category.DEFAULT"/>
//    <data android:mimeType="*/*"/>
//    </intent-filter>
//    <intent-filter>
//    <action android:name="com.google.android.gms.nearby.SEND_FOLDER"/>
//    <category android:name="android.intent.category.DEFAULT"/>
//    </intent-filter>
//    <meta-data android:name="android.service.chooser.chip_label" android:resource="Quick Share"/>
//    <meta-data android:name="android.service.chooser.chip_icon" android:resource="res/H2T.xml"/>
//    <meta-data android:name="android.service.chooser.chooser_target_service" android:value=".nearby.sharing.DirectShareService"/>
//    </activity>
            component = ComponentName(
                "com.google.android.gms",
                "com.google.android.gms.nearby.sharing.send.SendActivity"
            )
            // Add the URIs as extras.
            putParcelableArrayListExtra(
                Intent.EXTRA_STREAM,
                uri.toMutableList() as ArrayList<Uri>
            )
            // Set the MIME type to allow sharing of various file types.
            type = "*/*"
            // Specify supported MIME types.
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

    fun Share(vararg uri: Uri) = Intent.createChooser(
        com.zs.core.Intent(Intent.ACTION_SEND_MULTIPLE) {
            // Map selected IDs to content URIs.
            // TODO - Construct custom content uri.
            // Set the action to send multiple items.
            action = Intent.ACTION_SEND_MULTIPLE
            // Add the URIs as extras.
            putParcelableArrayListExtra(
                Intent.EXTRA_STREAM,
                uri.toMutableList() as ArrayList<Uri>
            )
            // Grant read permission to the receiving app.
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Set the MIME type to allow sharing of various file types.
            type = "*/*"
            // Specify supported MIME types.
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        },
        "Share files..."
    )
}