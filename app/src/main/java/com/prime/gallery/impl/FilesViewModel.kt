package com.prime.gallery.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.prime.gallery.core.compose.snackbar.SnackbarController
import com.prime.gallery.files.Files
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//FixMe: Move the ViewModel Impl to separate package.
@HiltViewModel
class FilesViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    channel: SnackbarController,
) : ViewModel(), Files, SnackbarController by channel {

}