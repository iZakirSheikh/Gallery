package com.prime.gallery.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.prime.gallery.core.compose.snackbar.SnackbarController
import com.prime.gallery.folders.Folders
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//FixMe: Move the ViewModel Impl to separate package.
@HiltViewModel
class FoldersViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    private val channel: SnackbarController,
) : ViewModel(), Folders, SnackbarController by channel {

}