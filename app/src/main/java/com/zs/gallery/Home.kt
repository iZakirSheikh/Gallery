/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 23-07-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.gallery

import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.SelectableChipColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.primex.core.textResource
import com.primex.material2.Label
import com.primex.material2.OutlinedButton
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.LocalWindowSize
import com.zs.compose_ktx.Range
import com.zs.compose_ktx.WindowSize
import com.zs.compose_ktx.isPermissionGranted
import com.zs.compose_ktx.navigation.BottomNavItem
import com.zs.compose_ktx.navigation.NavRailItem
import com.zs.compose_ktx.navigation.NavigationItemDefaults
import com.zs.compose_ktx.navigation.NavigationSuiteScaffold
import com.zs.compose_ktx.toast.ToastHostState
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.Route
import com.zs.gallery.common.composable
import com.zs.gallery.common.current
import com.zs.gallery.common.domain
import com.zs.gallery.common.preference
import com.zs.gallery.files.Album
import com.zs.gallery.files.Folder
import com.zs.gallery.files.RouteAlbum
import com.zs.gallery.files.RouteFolder
import com.zs.gallery.files.RouteTimeline
import com.zs.gallery.files.Timeline
import com.zs.gallery.folders.Folders
import com.zs.gallery.folders.RouteFolders
import com.zs.gallery.impl.AlbumViewModel
import com.zs.gallery.impl.FolderViewModel
import com.zs.gallery.impl.FoldersViewModel
import com.zs.gallery.impl.SettingsViewModel
import com.zs.gallery.impl.TimelineViewModel
import com.zs.gallery.impl.ViewerViewModel
import com.zs.gallery.preview.RouteViewer
import com.zs.gallery.preview.Viewer
import com.zs.gallery.settings.RouteSettings
import com.zs.gallery.settings.Settings
import org.koin.androidx.compose.koinViewModel

private const val TAG = "Home"

/**
 * Observes whether the app is in light mode based on the user's preference and system settings.
 *
 * @return `true` if the app is in light mode, `false` otherwise.
 */
@Composable
@NonRestartableComposable
private fun isPreferenceDarkTheme(): Boolean {
    val mode by preference(key = Settings.KEY_NIGHT_MODE)
    return when (mode) {
        NightMode.YES -> true
        NightMode.NO -> false
        NightMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
    }
}

/**
 * The route to permission screen.
 */
private object RoutePermission : Route

/**
 * List of required storage permissions, adapted for different Android versions.
 */
private val ADAPTIVE_STORAGE_PERMISSIONS =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) listOf(
        android.Manifest.permission.READ_MEDIA_VIDEO,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
    )
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(
        android.Manifest.permission.READ_MEDIA_VIDEO,
        android.Manifest.permission.READ_MEDIA_IMAGES
    )
    else listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

/**
 * Represents the permission screen
 * @see ADAPTIVE_STORAGE_PERMISSIONS
 * @see PERMISSION_ROUTE
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Permission() {
    val controller = LocalNavController.current
    // Compose the permission state.
    // Once granted set different route like folders as start.
    // Navigate from here to there.
    val permission = rememberMultiplePermissionsState(permissions = ADAPTIVE_STORAGE_PERMISSIONS) {
        if (!it.all { (_, state) -> state }) return@rememberMultiplePermissionsState
        controller.graph.setStartDestination(RouteTimeline())
        controller.navigate(RouteTimeline()) {
            popUpTo(RoutePermission()) {
                inclusive = true
            }
        }
    }
    com.zs.gallery.common.Placeholder(
        iconResId = R.raw.lt_permission,
        title = stringResource(R.string.permission_screen_title),
        message = textResource(R.string.permission_screen_desc),
        vertical = LocalWindowSize.current.widthRange == Range.Compact
    ) {
        OutlinedButton(
            onClick = { permission.launchMultiplePermissionRequest() },
            modifier = Modifier.size(width = 200.dp, height = 46.dp),
            elevation = null,
            label = stringResource(R.string.allow),
            border = ButtonDefaults.outlinedBorder,
            shape = CircleShape,
            colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent)
        )
    }
}

/**
 * return the navigation type based on the window size.
 */
private inline val WindowSize.navTypeRail get() = widthRange > Range.Medium

private val NAV_RAIL_WIDTH = 96.dp

/**
 * Calculates an returns newWindowSizeClass after consuming sapce occupied by  [navType].
 *
 * @return consumed window class.
 * @see [navType]
 */
private inline val WindowSize.remaining
    get() = when {
        !navTypeRail -> consume(height = 56.dp)
        else -> consume(width = NAV_RAIL_WIDTH)
    }

/**
 *Navigates to the specified route, managing the back stack for a seamless experience.
 * Pops up to the start destination and uses launchSingleTop to prevent duplicate destinations.
 *
 * @param route The destination route.
 */
private fun NavController.toRoute(route: String) {
    navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // re-selecting the same item
        launchSingleTop = true
        // Restore state when re-selecting a previously selected item
        restoreState = true
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavItem(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    typeRail: Boolean = false,
    colors: SelectableChipColors = NavigationItemDefaults.navigationItemColors(),
) {
    when (typeRail) {
        true -> NavRailItem(onClick, icon, label, modifier, checked, colors = colors)
        else -> BottomNavItem(onClick, icon, label, modifier, checked, colors = colors)
    }
}

/**
 * A composable function that represents a navigation bar, combining both rail and bottom bar elements.
 *
 * @param type Specifies whether the navigation bar includes a [NavigationRail] or [BottomNavigation] component.
 * @param navController The NavController to manage navigation within the navigation bar.
 * @param modifier The modifier for styling and layout customization of the navigation bar.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
@NonRestartableComposable
private fun NavigationBar(
    typeRail: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val routes = remember {
        movableContentOf {
            // Get the current navigation destination from NavController
            val current by navController.currentBackStackEntryAsState()
            val colors = NavigationItemDefaults.navigationItemColors()
            val domain = current?.destination?.domain

            // Timeline
            NavItem(
                label = { Label(text = textResource(R.string.photos)) },
                icon = { Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null) },
                checked = domain == RouteTimeline.domain,
                onClick = { navController.toRoute(RouteTimeline()) },
                typeRail = typeRail,
                colors = colors
            )

            // Folders
            NavItem(
                label = { Label(text = textResource(R.string.folders)) },
                icon = { Icon(imageVector = Icons.Filled.FolderCopy, contentDescription = null) },
                checked = domain == RouteFolders.domain,
                onClick = { navController.toRoute(RouteFolders()) },
                typeRail = typeRail,
                colors = colors
            )

            // Settings
            NavItem(
                label = { Label(text = textResource(R.string.settings)) },
                icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = null) },
                checked = domain == RouteSettings.domain,
                onClick = { navController.toRoute(RouteSettings()) },
                typeRail = typeRail,
                colors = colors
            )
        }
    }

    when (typeRail) {
        true -> NavigationRail(
            modifier = modifier.width(NAV_RAIL_WIDTH),
            windowInsets = WindowInsets.systemBars,
            contentColor = AppTheme.colors.onBackground,
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            content = {
                // Display routes at the top of the navRail.
                routes()
                // Some Space between naves and Icon.
                Spacer(modifier = Modifier.weight(1f))
            },
        )

        else -> BottomAppBar(
            modifier = modifier.height(110.dp),
            windowInsets = WindowInsets.navigationBars,
            backgroundColor = Color.Transparent,
            contentColor = AppTheme.colors.onBackground,
            elevation = 0.dp,
            contentPadding = PaddingValues(
                horizontal = AppTheme.padding.normal,
                vertical = AppTheme.padding.medium
            ),
            content = {
                Spacer(modifier = Modifier.weight(1f))
                // Display routes at the contre of available space
                routes()
                Spacer(modifier = Modifier.weight(1f))
            }
        )
    }
}

/**
 * The shape of the content inside the scaffold.
 */
private val CONTENT_SHAPE = RoundedCornerShape(8)

/**
 * The set of domains that require the navigation bar to be shown.
 * For other domains, the navigation bar will be hidden.
 */
private val DOMAINS_REQUIRING_NAV_BAR =
    arrayOf(RouteTimeline.domain, RouteFolders.domain, RouteSettings.domain)

/**
 * Defines the navigation graph for the application.
 */
private val NavGraphBuilder: NavGraphBuilder.() -> Unit = {
    // Route for handling storage permissions.
    composable(RoutePermission) { Permission() }

    // Route for the Timeline screen.
    composable(RouteTimeline) {
        val state = koinViewModel<TimelineViewModel>()
        Timeline(viewState = state)
    }

    // Folders
    composable(RouteFolders) {
        val state = koinViewModel<FoldersViewModel>()
        Folders(viewState = state)
    }

    // Settings
    composable(RouteSettings) {
        val state = koinViewModel<SettingsViewModel>()
        Settings(viewState = state)
    }

    // Viewer
    composable(RouteViewer){
        val state = koinViewModel<ViewerViewModel>()
        Viewer(viewState = state)
    }

    // Folder - That displays files
    composable(RouteFolder){
        val state = koinViewModel<FolderViewModel>()
        Folder(viewState = state)
    }

    // Album
    composable(RouteAlbum){
        val state = koinViewModel<AlbumViewModel>()
        Album(viewState = state)
    }
}

// Default Enter/Exit Transitions.
private val GlobalEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
    {
        scaleIn(tween(220, 90), 0.98f) + fadeIn(tween(700))
    }
private val GlobalExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
    {
        fadeOut(animationSpec = tween(700))
    }

@Composable
fun Home(toastHostState: ToastHostState) {
    val isLight = !isPreferenceDarkTheme()
    val navController = rememberNavController()
    val immersiveView by preference(key = Settings.KEY_IMMERSIVE_VIEW)
    val isSystemBarsTransparent by preference(key = Settings.KEY_TRANSPARENT_SYSTEM_BARS)
    AppTheme(
        isLight = isLight,
        immersive = immersiveView,
        fontFamily = Settings.DefaultFontFamily,
        isSystemBarsTransparent = isSystemBarsTransparent,
    ) {
        // Get the window size class
        val clazz = LocalWindowSize.current
        // Provide the navController, newWindowClass through LocalComposition.
        CompositionLocalProvider(
            LocalNavController provides navController,
            LocalWindowSize provides clazz.remaining,
            content = {
                // Determine the navigation type based on the window size class and access the system facade
                val facade = LocalSystemFacade.current
                // Determine whether to hide the navigation bar based on the current destination
                val current = navController.current
                Log.d(TAG, "Home: ${current?.domain}")
                val hideNavigationBar = current?.domain !in DOMAINS_REQUIRING_NAV_BAR
                NavigationSuiteScaffold(
                    vertical = clazz.widthRange < Range.Medium,
                    toastHostState = toastHostState,
                    hideNavigationBar = hideNavigationBar,
                    //progress = facade.inAppUpdateProgress,
                    background = AppTheme.colors.background(elevation = 1.dp),
                    // Set up the navigation bar using the NavBar composable
                    navBar = { NavigationBar(clazz.navTypeRail, navController) },
                    // Display the main content of the app using the NavGraph composable
                    content = {
                        val context = LocalContext.current
                        // Load start destination based on if storage permission is set or not.
                        val granted =
                            context.isPermissionGranted(ADAPTIVE_STORAGE_PERMISSIONS[0])
                        val startDestination =
                            if (granted) RouteTimeline else RoutePermission
                        NavHost(
                            navController = navController,
                            startDestination = startDestination.route,
                            builder = NavGraphBuilder,
                            modifier = Modifier
                                .clip(CONTENT_SHAPE)
                                .background(AppTheme.colors.background)
                                .fillMaxSize(),
                            enterTransition = GlobalEnterTransition,
                            exitTransition = GlobalExitTransition,
                        )
                    }
                )
            }
        )
    }
}