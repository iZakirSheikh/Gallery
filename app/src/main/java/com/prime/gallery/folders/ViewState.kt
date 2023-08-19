package com.prime.gallery.folders

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.ui.graphics.vector.ImageVector
import com.primex.core.Text

interface Folders {

    companion object {
        val route: String get() = "route_folder"

        fun direction() = route
    }

}