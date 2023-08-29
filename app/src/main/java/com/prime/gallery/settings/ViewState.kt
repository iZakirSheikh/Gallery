package com.prime.gallery.settings

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.prime.gallery.R
import com.prime.gallery.core.NightMode
import com.primex.preferences.Key
import com.primex.preferences.StringSaver
import com.primex.preferences.booleanPreferenceKey
import com.primex.preferences.stringPreferenceKey
import com.primex.preferences.stringSetPreferenceKey


private const val TAG = "Settings_ViewState"
private const val PREFIX = "pref_key"

/**
 * Immutable data class representing a preference.
 *
 * @property value The value of the preference.
 * @property title The title text of the preference.
 * @property vector The optional vector image associated with the preference.
 * @property summary The optional summary text of the preference.
 * @param P The type of the preference value.
 */
@Stable
data class Preference<out P>(
    val value: P,
    @JvmField val title: CharSequence,
    val vector: ImageVector? = null,
    @JvmField val summery: CharSequence? = null,
)

private val provider = GoogleFont.Provider(
    "com.google.android.gms.fonts",
    "com.google.android.gms",
    R.array.com_google_android_gms_fonts_certs
)

@Stable
private fun FontFamily(name: String) = FontFamily(
    Font(GoogleFont(name), provider, FontWeight.Light),
    Font(GoogleFont(name), provider, FontWeight.Medium),
    Font(GoogleFont(name), provider, FontWeight.Normal),
    Font(GoogleFont(name), provider, FontWeight.Bold),
)

@Stable
interface Blacklist {
    val values: Set<String>?
    fun unblock(path: String)
}

/**
 * Interface representing the settings.
 *
 * @property nightMode The state of the night mode preference.
 * @property colorSystemBars The state of the color system bars preference.
 * @property hideStatusBar The state of the hide status bar preference.
 * @property dynamicColors The state of the dynamic colors preference.
 * @property numberGroupSeparator The state of the number group separator preference.
 */
@Stable
interface Settings : Blacklist {
    companion object {
        val route = "route_settings"

        /**
         * Provides the direction for the settings route.
         *
         * @return the route for settings
         */
        fun direction() = route

        /**
         * The Default font family of the App.
         */
        val DefaultFontFamily = FontFamily("Roboto")

        /**
         * Retrieves/Sets The [NightMode] Strategy
         */
        val NIGHT_MODE = stringPreferenceKey(
            "${PREFIX}_night_mode",
            NightMode.FOLLOW_SYSTEM,
            object : StringSaver<NightMode> {
                override fun save(value: NightMode): String = value.name
                override fun restore(value: String): NightMode = NightMode.valueOf(value)
            },
        )
        val COLOR_SYSTEM_BARS = booleanPreferenceKey(PREFIX + "_color_system_bars", false)
        val HIDE_SYSTEM_BARS = booleanPreferenceKey(PREFIX + "_hide_system_bars", false)
        val DYNAMIC_COLORS = booleanPreferenceKey(PREFIX + "_dynamic_colors", true)
        val TRASH_CAN_ENABLED =
            booleanPreferenceKey(PREFIX + "_trash_can_enabled", defaultValue = true)

        /**
         * The set of files/ folders that have been excluded from media scanning.
         */
        val BLACKLISTED_FILES = stringSetPreferenceKey(PREFIX + "_blacklisted_files")
    }

    val darkModeStrategy: Preference<NightMode>
    val colorSystemBars: Preference<Boolean>
    val hideSystemBars: Preference<Boolean>
    val dynamicColors: Preference<Boolean>
    val isTrashCanEnabled: Preference<Boolean>
    val excludedFiles: Preference<Set<String>?>

    /**
     * Sets the value for the specified key.
     *
     * @param key the key for the setting
     * @param value the value to be set
     * @param S the type of setting
     * @param O the type of value
     */
    fun <S, O> set(key: Key<S, O>, value: O)
}