package com.zs.gallery.common

/**
 * Defines all navigation routes within the app.
 *
 * Using a sealed interface ensures type safety and exhaustive handling
 * when navigating between static and dynamic destinations.
 *
 * Static routes:
 * - [Settings] : Settings screen
 * - [AboutUs] : About-us screen
 * - [ScreenLock] : Screen lock feature
 *
 * Dynamic routes:
 * - [Files] : Album’s file list
 * - [Viewer] : Specific item viewer
 */
sealed interface Route {

    // Static destinations (no parameters required)
    object Settings : Route
    object AboutUs : Route
    object ScreenLock : Route
    object Onboarding: Route
    object IntentViewer: Route

    /**
     * Dynamic route for an album’s file list.
     *
     * @property albumId Identifier of the album to display.
     */
    data class Files(val albumId: String = "") : Route

    /**
     * Dynamic route for viewing a specific item.
     *
     * @property id Identifier of the item to view.
     * @property albumId Identifier of the album containing the item.
     */
    data class Viewer(val id: String, val albumId: String) : Route
}
