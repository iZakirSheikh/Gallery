package com.zs.gallery.files

import com.zs.api.store.Media
import com.zs.api.store.MediaProvider
import kotlinx.coroutines.flow.Flow
/**
 * Type alias for accessing the companion object of [FilesViewState].
 * This provides convenient access to shared properties or functions within the [FilesViewState] class.
 */
typealias Files = FilesViewState.Companion


interface FilesViewState {

    companion object {
        const val DOMAIN = "route_files"
        const val ROUTE = DOMAIN

        const val FROM_ALL = "_from_all"
        const val FROM_FOLDER = "_from_folder"

        const val ORDER_BY_NAME = MediaProvider.COLUMN_NAME
        const val ORDER_BY_DATE = MediaProvider.COLUMN_DATE_MODIFIED
        //const val ORDER_BY_

        fun direction() = ROUTE
    }

    val files: Flow<Media>

    fun delete(vararg file: Media, trash: Boolean = true)
}