package com.prime.gallery.impl.vms

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prime.gallery.R
import com.prime.gallery.core.util.PathUtils
import com.prime.gallery.core.util.asComposeState
import com.prime.gallery.impl.SystemDelegate
import com.prime.gallery.settings.Preference
import com.prime.gallery.settings.Settings
import com.primex.preferences.Key
import com.primex.preferences.Preferences
import com.primex.preferences.value
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

context (Preferences, ViewModel)
private fun <T> Flow<T>.asComposeState(): State<T> = asComposeState(runBlocking { first() })

//FixMe: Move the ViewModel Impl to separate package.
@HiltViewModel
class SettingsViewModel @Inject constructor(
    delegate: SystemDelegate,
    private val preferences: Preferences
) : ViewModel(), Settings, SystemDelegate by delegate {

    override val darkModeStrategy by with(preferences) {
        preferences[Settings.NIGHT_MODE].map {
            Preference(
                it,
                getText(R.string.pref_app_theme),
                Icons.Outlined.LightMode,
                getText(R.string.pref_app_theme_summery),
            )
        }.asComposeState()
    }

    override val colorSystemBars by with(preferences) {
        preferences[Settings.COLOR_SYSTEM_BARS].map {
            Preference(
                it,
                getText(R.string.pref_color_system_bars),
                Icons.Outlined.Palette,
                getText(R.string.pref_color_system_bars_summery),
            )
        }.asComposeState()
    }

    override val dynamicColors: Preference<Boolean> by with(preferences) {
        preferences[Settings.DYNAMIC_COLORS].map {
            Preference(
                it,
                getText(R.string.pref_dynamic_colors),
                Icons.Outlined.Colorize,
                getText(R.string.pref_dynamic_colors_summery),
            )
        }.asComposeState()
    }
    override val hideSystemBars by with(preferences) {
        preferences[Settings.HIDE_SYSTEM_BARS].map {
            Preference(
                it,
                getText(R.string.pref_hide_system_bars),
                null,
                getText(R.string.pref_hide_system_bars_summery),
            )
        }.asComposeState()
    }

    override val isTrashCanEnabled by with(preferences) {
        preferences[Settings.TRASH_CAN_ENABLED].map {
            Preference(
                it,
                getText(R.string.pref_enable_trash_can),
                Icons.Outlined.RestoreFromTrash,
                getText(R.string.pref_enable_trash_can_summery),
            )
        }.asComposeState()
    }

    override val excludedFiles: Preference<Set<String>?> by with(preferences) {
        preferences[Settings.BLACKLISTED_FILES].map {
            Preference(
                title = getText(R.string.pref_blacklisted_files),
                summery = getText(R.string.pref_blacklisted_files_summery),
                value = it
            )
        }.asComposeState()
    }

    override fun <S, O> set(key: Key<S, O>, value: O) {
        viewModelScope.launch {
            preferences[key] = value
        }
    }

    override val values: Set<String>? get() = excludedFiles.value

    override fun unblock(path: String) {
        viewModelScope.launch {
            val blacklist =
                preferences.value(Settings.BLACKLISTED_FILES)?.toMutableSet() ?: return@launch
            val removed = blacklist.remove(path)
            val msg = if (!removed)
                resources.getString(R.string.msg_unknown_error)
            else
                resources.getString(
                    R.string.msg_file_removed_from_blacklist_s,
                    PathUtils.name(path)
                )
            showToast(msg, Toast.LENGTH_SHORT)
            preferences[Settings.BLACKLISTED_FILES] = blacklist
        }
    }
}

suspend fun Preferences.block(vararg path: String): Int {
    val preferences = this
    val list = preferences.value(Settings.BLACKLISTED_FILES)
    if (path.isEmpty()) return 0
    // it will automatically remove duplicates.
    if (list == null) {
        val items = path.toSet()
        preferences[Settings.BLACKLISTED_FILES] = path.toSet()
        return items.size
    }
    val items = list + path
    preferences[Settings.BLACKLISTED_FILES] = items
    return items.size
}