package com.prime.gallery.files


interface Files {

    companion object {
        val route: String = "route_photos"
        fun direction() = route
    }
}