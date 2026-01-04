package com.zs.gallery.common

import android.net.Uri

/**
 * Defines all navigation routes within the app.
 *
 * Using a sealed interface ensures type safety and exhaustive handling
 * when navigating between static and dynamic destinations.
 *
 * Static routes: [Settings], [AboutUs], [Lockscreen], [AppIntro], [Folders], [Albums]
 *
 * Dynamic routes: [Files], [Viewer]
 */
sealed interface Route {

    /**
     * Domain identifier for the route, derived from the class name.
     * Useful for logging, analytics, or debugging.
     */
    val domain: String
        get() {
            val name = this::class.java.simpleName
            return "_route_${name.lowercase()}"
        }

    // Static destinations (no parameters required)
    object Settings : Route
    object AboutUs : Route
    object Lockscreen : Route
    object AppIntro : Route
    object Folders : Route
    object Albums : Route
    object Timeline: Route
    /**
     * Dynamic route for an album’s file list.
     *
     * @property albumId Identifier of the album to view.
     *                   Uses -1 as a sentinel when no album is specified.
     * @property path    Path of the folder to view.
     *                   An empty string "" is used as a sentinel to represent the timeline.
     *
     * @see isTimeline Convenience property to check if this route points to the timeline.
     */
    class Files private constructor(
        val albumId: Long = -1,
        val path: String? = null
    ) : Route {
        /** Represents Album Media */
        constructor(albumId: Long) : this(albumId, null)

        /** Represents Folder Media */
        constructor(path: String) : this(albumId = -1, path = path)
    }

    /**
     * Dynamic route for viewing a specific item.
     *
     * @property id       Identifier of the item to view.
     *                    Uses -1 as a sentinel when no ID is specified.
     * @property albumId  Identifier of the album containing the item.
     *                    Uses -1 as a sentinel when no album is specified.
     * @property path     Path of the folder containing the item.
     *                    An empty string "" is used as a sentinel to represent the timeline.
     * @property uri      Direct URI reference to the item (optional).
     * @property mimeType MIME type of the item when using a URI (optional).
     */
    class Viewer private constructor(
        val id: Long = -1,
        val albumId: Long = -1,
        val path: String? = null,
        val uri: Uri? = null,
        val mimeType: String? = null
    ) : Route {
        /** Represents Album Viewer */
        constructor(id: Long, albumId: Long) : this(id, albumId, null, null)
        /** Represents Folder Viewer */
        constructor(id: Long, path: String = "") : this(id, albumId = -1, path = path, null)
        /** Represents Intent Viewer */
        constructor(uri: Uri, mimeType: String? = null) : this(-1, -1, uri = uri, mimeType = mimeType)

        val isIntentViewer get() = uri != null
    }
}