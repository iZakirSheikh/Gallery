package com.prime.gallery.impl.vms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.prime.gallery.folders.Folders
import com.prime.gallery.impl.Repository
import com.prime.gallery.impl.SystemDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//FixMe: Move the ViewModel Impl to separate package.
@HiltViewModel
class FoldersViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    private val delegate: SystemDelegate,
) : ViewModel(), Folders, SystemDelegate by delegate {

}