package com.prime.gallery.impl

import android.content.ContentResolver
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class Repository  @Inject constructor(
    private val resolver: ContentResolver
) {

}
