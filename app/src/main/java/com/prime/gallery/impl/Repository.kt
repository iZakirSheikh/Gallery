package com.prime.gallery.impl

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.prime.gallery.core.db.getFolders
import com.prime.gallery.core.db.getMediaFiles
import com.prime.gallery.core.db.observe
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * Repository class for managing Albums and related Photo files. This class is annotated with
 * `@ActivityRetainedScoped` and is designed to be used in Android app development.
 *
 * @property resolver An instance of the Android `ContentResolver` class used to access content providers,
 * such as the device's media store, to retrieve audio files.
 *
 * @constructor Creates a new `Repository2` object with the given `resolver` objects.
 */
@ActivityRetainedScoped
class Repository @Inject constructor(
    private val resolver: ContentResolver
) {
    /**
     * @see ContentResolver.observe
     */
    fun observe(uri: Uri) = resolver.observe(uri)

    /**
     * @see ContentResolver.getMediaFiles
     */
    suspend fun getMediaFiles(
        query: String? = null,
        order: String = MediaStore.Audio.Media.DATE_MODIFIED,
        ascending: Boolean = true,
        offset: Int = 0,
        parent: String? = null,
        limit: Int = Int.MAX_VALUE
    ) = resolver.getMediaFiles(query, order, ascending, parent, offset, limit)

    /**
     * @see MediaProvider.getFolders
     */
    suspend fun getFolders(
        filter: String? = null,
        ascending: Boolean = true,
    ) = resolver.getFolders(filter, ascending)
}
