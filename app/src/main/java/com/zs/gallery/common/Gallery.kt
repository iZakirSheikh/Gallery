@file:Suppress("ClassName")

package com.zs.gallery.common

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.core.net.toUri
import com.zs.compose.foundation.OliveYellow
import com.zs.gallery.common.Gallery.PKG_MARKET_ID
import com.zs.gallery.common.Gallery.PREFIX_MARKET_FALLBACK
import com.zs.gallery.common.Gallery.PREFIX_MARKET_URL
import com.zs.gallery.common.Gallery.config.fontScale
import com.zs.gallery.common.Gallery.config.gridItemSizeMultiplier
import com.zs.gallery.common.Gallery.config.isBackgroundBlurEnabled
import com.zs.gallery.common.Gallery.config.isFileGroupingEnabled
import com.zs.gallery.common.Gallery.config.isLiveGalleryEnabled
import com.zs.gallery.common.Gallery.config.isTrashCanEnabled
import com.zs.gallery.common.Gallery.config.lockTimeoutMinutes
import com.zs.gallery.common.Gallery.keys.dynamic_colors
import com.zs.gallery.common.Gallery.keys.launch_counter
import com.zs.gallery.common.Gallery.keys.night_mode
import com.zs.gallery.common.Gallery.keys.transparent_system_bars
import com.zs.preferences.IntSaver
import com.zs.preferences.booleanPreferenceKey
import com.zs.preferences.intPreferenceKey
import com.zs.preferences.longPreferenceKey

/**
 * Represents [Gallery] app.
 *
 * Provides global constants for Play Store integration and
 * contains the [keys] object that defines persisted preferences.
 *
 * @property PREFIX_MARKET_URL Base URI for Play Store deep links (e.g. `market://details?id=`).
 * @property PREFIX_MARKET_FALLBACK HTTP fallback URI for Play Store links.
 * @property PKG_MARKET_ID Package name of the Play Store app (`com.android.vending`).
 * @property keys Global preference keys used throughout the app.
 */
object Gallery {

    // The keys for the preferences
    const val PREFIX_MARKET_URL = "market://details?id="
    const val PREFIX_MARKET_FALLBACK = "http://play.google.com/store/apps/details?id="
    const val PKG_MARKET_ID = "com.android.vending"

    @Suppress("SameParameterValue")
    private fun Intent(action:String, data: Uri) = Intent(action).apply { this.data  = data }
    // some common intents
    val PrivacyPolicyIntent = Intent(Intent.ACTION_VIEW, "https://docs.google.com/document/d/1D9wswWSrt65ol7h3HLKhk31OVTqDtN4uLJ73_Rk9hT8/edit?usp=sharing".toUri())
    val GitHubIssuesPage = Intent(Intent.ACTION_VIEW, "https://github.com/iZakirSheikh/Gallery/issues".toUri())
    val TelegramIntent = Intent(Intent.ACTION_VIEW, "https://t.me/audiofy_support".toUri())
    val GithubIntent = Intent(Intent.ACTION_VIEW, "https://github.com/iZakirSheikh/Gallery".toUri())
    val JoinBetaIntent = Intent(Intent.ACTION_VIEW, "https://play.google.com/apps/testing/com.zs.gallery/join".toUri())


    val DefaultFontFamily get() = FontFamily.Default

    // --- Default Accent colors ---
    val LightAccentColor = Color(0xFF904A42)
    val DarkAccentColor = Color.OliveYellow

    /**
     * List of permissions required to run the app.
     *
     * This list is constructed based on the device's Android version to ensure
     * compatibility with scoped storage and legacy storage access.
     */
    @SuppressLint("BuildListAdds")
    val REQUIRED_PERMISSIONS = buildList {
        // For Android Tiramisu (33) and above, use media permissions for scoped storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this += android.Manifest.permission.ACCESS_MEDIA_LOCATION
            this += android.Manifest.permission.READ_MEDIA_VIDEO
            this += android.Manifest.permission.READ_MEDIA_IMAGES
        }
        // For Android Upside Down Cake (34) and above, add permission for user-selected visual media
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            this += android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        // For Android versions below Tiramisu 10(29), request WRITE_EXTERNAL_STORAGE for
        // legacy storage access
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            this += android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            this += android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

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
    val ColorSaver = object : IntSaver<Color> {
        override fun restore(value: Int): Color = Color(value)
        override fun save(value: Color): Int = value.toArgb()
    }

    /**
     * Global preference keys for the [Gallery] app.
     *
     * Each key defines a persisted setting with its default value,
     * used to configure app behavior and UI appearance.
     *
     * @property night_mode Night mode preference (default: follow system setting).

     * @property transparent_system_bars Transparent system bars toggle (default: true on Android Q+).
     * @property secure_mode Secure mode toggle (default: false, gallery not locked down).
     * @property launch_counter App launch counter (default: 0, increments per launch).
     * @property dynamic_colors Dynamic color support (default: enabled on Android S+).
     */
    object keys {
        val night_mode = intPreferenceKey("_night_mode", NightMode.FOLLOW_SYSTEM, OrdinalEnumSaver())
        val transparent_system_bars = booleanPreferenceKey("_transparent_system_bars", defaultValue = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val launch_counter = intPreferenceKey( "_launch_counter", 0)
        val dynamic_colors = booleanPreferenceKey("_dynamic_colors", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        val dark_accent_color = intPreferenceKey("_dark_accent_color", LightAccentColor, ColorSaver)
        val light_accent_color = intPreferenceKey("_light_accent_color", DarkAccentColor, ColorSaver)
        val app_version_code = longPreferenceKey("_app_version_code", -1)
    }

    /**
     * Application-level configuration object.
     *
     * This singleton holds global settings that control app behavior and UI features.
     *
     * ### Why properties are `var`
     * - These values are mutable because they can be updated at runtime (e.g., via a settings screen).
     * - Some changes require a full app restart to take effect globally, while others may apply locally.
     * - The object can be serialized whenever a change occurs, ensuring persistence across restarts.
     *
     * ### Why this design?
     * - Provides a centralized configuration accessible from both Compose and non-Compose code.
     * - Avoids unnecessary state propagation through Compose, improving performance by reducing recompositions.
     * - Ensures consistent behavior across the app by keeping critical flags in one place.
     *
     * @property isBackgroundBlurEnabled Enables or disables background blur. Defaults to `true` on Android 12 (API 31) and above.
     * @property isTrashCanEnabled Toggles the availability of the trash can feature.
     * @property fontScale User-defined font scaling factor. `-1f` indicates default system scaling.
     * @property gridItemSizeMultiplier Multiplier applied to grid item sizes for layout customization.
     * @property isFileGroupingEnabled Enables grouping of files in views where applicable.
     * @property isLiveGalleryEnabled Live gallery toggle (default: disabled).
     * @property fontScale Font scale override (default: -1f, meaning system scale).
     * @property lockTimeoutMinutes Representing the timeout duration (in minutes) for app lock.
     *                             - This preference determines how long the app can be in the background
     *                             - before the user needs to authenticate to regain access.
     */
    object config {
        @JvmField var isBackgroundBlurEnabled: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        @JvmField var isTrashCanEnabled: Boolean = true
        @JvmField var fontScale: Float = -1f
        @JvmField var gridItemSizeMultiplier: Float = 1.0f
        @JvmField var isFileGroupingEnabled: Boolean = true
        @JvmField var lockTimeoutMinutes: Int = -1
        @JvmField var isLiveGalleryEnabled: Boolean = false
        @JvmField var isAppSecureModeEnabled: Boolean = false
    }
}