package com.prime.gallery.viewer

interface Viewer {

    companion object {
        val route: String = "route_viewer"

        fun direction() = route
    }

}