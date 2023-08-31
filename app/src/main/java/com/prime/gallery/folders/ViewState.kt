package com.prime.gallery.folders

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.ui.graphics.vector.ImageVector
import com.prime.gallery.core.db.Folder
import com.primex.core.Text
import kotlinx.coroutines.flow.StateFlow

interface Folders {

    companion object {
        val route: String get() = "route_folder"

        const val ORDER_BY_NAME = 0
        const val ORDER_BY_DATE_MODIFIED = 1
        const val ORDER_BY_SIZE =  2

        fun direction() = route
    }

    /**
     * Gets or sets the sorting direction of the list of folders.
     * A value of true means the list is sorted in ascending order, while a value of false means the
     * list is sorted in descending order.
     * The default value is false.
     */
    var ascending: Boolean

    /**
     * Gets or sets the grouping criterion of the list of folders.
     * A value of [ORDER_BY_NAME] means the list is grouped by the folder name, while a value of [ORDER_BY_DATE_MODIFIED] means the list is grouped by the folder date modified, and a value of [ORDER_BY_SIZE] means the list is grouped by the folder size.
     * The default value is [ORDER_BY_SIZE].
     */
    var order: Int

    /**
     * A state flow that emits the list of folders in the app.
     * The list can be null, empty, or non-empty depending on the loading status and the availability of data.
     * A null value indicates that the folders are being loaded from the source and the UI should show a loading indicator.
     * An empty list indicates that there are no folders to display and the UI should show an empty state message.
     * A non-empty list indicates that the folders are successfully loaded and the UI should show them in a list view.
     * Any error that occurs during the loading process will be handled by a snackbar that shows the error message and a retry option.
     */
    val folders: StateFlow<List<Folder>?>
}