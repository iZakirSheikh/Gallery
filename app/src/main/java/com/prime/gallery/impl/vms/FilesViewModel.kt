package com.prime.gallery.impl.vms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.prime.gallery.files.Files
import com.prime.gallery.impl.Repository
import com.prime.gallery.impl.SystemDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//FixMe: Move the ViewModel Impl to separate package.
@HiltViewModel
class FilesViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    delegate: SystemDelegate,
) : ViewModel(), Files, SystemDelegate by delegate {

}