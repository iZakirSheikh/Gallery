package com.prime.gallery.impl.vms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.prime.gallery.impl.Repository
import com.prime.gallery.impl.SystemDelegate
import com.prime.gallery.viewer.Viewer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//FixMe: Move the ViewModel Impl to separate package.
@HiltViewModel
class ViewerViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    delegate: SystemDelegate
) : ViewModel(), Viewer, SystemDelegate by delegate {

}