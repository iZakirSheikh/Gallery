package com.prime.gallery.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.prime.gallery.core.NightMode
import com.prime.gallery.core.StringResolver
import com.prime.gallery.core.compose.snackbar.SnackbarController
import com.prime.gallery.settings.Preference
import com.prime.gallery.settings.Settings
import com.primex.preferences.Key
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//FixMe: Move the ViewModel Impl to separate package.
@HiltViewModel
class SettingsViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    resolver: StringResolver,
    channel: SnackbarController,
) : ViewModel(), Settings, SnackbarController by channel, StringResolver by resolver {

    override val nightMode: Preference<NightMode>
        get() = TODO("Not yet implemented")
    override val colorSystemBars: Preference<Boolean>
        get() = TODO("Not yet implemented")
    override val hideStatusBar: Preference<Boolean>
        get() = TODO("Not yet implemented")
    override val dynamicColors: Preference<Boolean>
        get() = TODO("Not yet implemented")
    override val numberGroupSeparator: Preference<Char>
        get() = TODO("Not yet implemented")

    override fun <S, O> set(key: Key<S, O>, value: O) {
        TODO("Not yet implemented")
    }
}