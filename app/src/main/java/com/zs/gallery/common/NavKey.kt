package com.zs.gallery.common

import android.net.Uri
import com.zs.gallery.common.NavKey.Files.Companion.SRC_FAVOURITES
import com.zs.gallery.common.NavKey.Files.Companion.SRC_TIMELINE
import com.zs.gallery.common.NavKey.Files.Companion.SRC_TRASH

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
sealed interface NavKey {

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
    object Settings : NavKey
    object AboutUs : NavKey
    object Lockscreen : NavKey
    object AppIntro : NavKey
    object Folders : NavKey
    object Albums : NavKey

    // Dynamic destinations (parameters required)

    /**
     * Dynamic navigation route representing the file list of an album, folder, or special collection.
     *
     * The route is parameterized by a `key` that determines the source of files:
     * - [SRC_TIMELINE] → the global timeline (all media items).
     * - A numeric string (parsable to `Long`) → an album ID.
     * - A valid file system path string → a folder.
     * - A special keyword (e.g., [SRC_FAVOURITES], [SRC_TRASH]) → a predefined collection.
     *
     * This abstraction allows navigation to different file sources using a single entry point.
     *
     * @param key Identifier used to resolve the file list. Defaults to [SRC_TIMELINE].
     */
    data class Files(val key: String = SRC_TIMELINE) : NavKey {

        val isTimeline get() = key == SRC_TIMELINE

        companion object {
            const val SRC_TIMELINE = "_src_timeline"
            const val SRC_FAVOURITES = "_src_favourites"
            const val SRC_TRASH = "_src_trash"
        }
    }

    /**
     * Dynamic navigation route representing a viewer for a specific media item.
     *
     * This route is used to open a full-screen viewer for a photo or video,
     * while maintaining the context of the collection it belongs to.
     * This allows for swiping between items in the same album, folder, or special collection.
     *
     * @property id The unique identifier of the media item to display initially.
     * @property key The source key defining the collection of media items. This can be an
     *               album ID, a folder path, or a special keyword like [Files.SRC_TIMELINE],
     *               [Files.SRC_FAVOURITES], or [Files.SRC_TRASH]. Defaults to [Files.SRC_TIMELINE].
     * @property uri The URI of the media item to display initially.
     * @property mimeType The MIME type of the media item to display initially.
     */
    @ConsistentCopyVisibility
    data class Viewer private constructor(
        val id: Long,
        val uri: Uri?,
        val mimeType: String?,
        val key: String
    ): NavKey {

        /**
         * Creates a normal viewer for a media item by ID.
         *
         * @param id The unique identifier of the media item.
         * @param mimeType The MIME type of the media item.
         * @param key The source key of the collection. Defaults to [Files.SRC_TIMELINE].
         */
        constructor(id: Long, mimeType: String?, key: String = Files.SRC_TIMELINE) :
                this(id = id, uri = null, mimeType = mimeType, key = key)

        /**
         * Creates an intent-based viewer for a media item using its URI.
         *
         * @param uri The URI of the media item.
         * @param mimeType The MIME type of the media item.
         */
        constructor(uri: Uri?, mimeType: String?) :
                this(id = -1, uri = uri, mimeType = mimeType, key = "")

        val isIntentViewer get() = uri != null
    }
}