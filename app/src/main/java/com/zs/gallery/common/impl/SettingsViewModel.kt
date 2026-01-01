package com.zs.gallery.common.impl

import android.util.Log
import androidx.lifecycle.ViewModel
import com.zs.gallery.settings.SettingsViewState

class SettingsViewModel(): KoinViewModel(), SettingsViewState {
    private val TAG = "SettingsViewModel"
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: ${this::class.simpleName}")
    }
}