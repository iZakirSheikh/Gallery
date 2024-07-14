/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 12-07-2024.
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

import NavigationBarItem
import NavigationDrawerItem
import NavigationItemDefaults
import NavigationRailItem
import NavigationSuiteScaffold
import android.os.Build
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.SelectableChipColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.zs.compose_ktx.toast.ToastHostState
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.current
import com.zs.gallery.common.domain
import com.zs.gallery.common.preference
import com.zs.gallery.files.Files
import com.zs.gallery.files.FilesViewState
import com.zs.gallery.folders.Folders
import com.zs.gallery.folders.FoldersViewState
import com.zs.gallery.impl.FilesViewModel
import com.zs.gallery.impl.FoldersViewModel
import com.zs.gallery.impl.SettingsViewModel
import com.zs.gallery.settings.Settings
import com.zs.megahertz.common.NightMode
import org.koin.androidx.compose.koinViewModel

private const val TAG = "MainContent"

/**
 * A simple composable that helps in resolving the current app theme as suggested by the [Gallery.NIGHT_MODE]
 */
@Composable
@NonRestartableComposable
private fun requiresLightTheme(): Boolean {
    val mode by preference(key = Settings.KEY_NIGHT_MODE)
    return when (mode) {
        NightMode.YES -> false
        NightMode.FOLLOW_SYSTEM -> !androidx.compose.foundation.isSystemInDarkTheme()
        else -> true
    }
}

/**
 * The route to permission screen.
 */
private const val ROUTE_STORAGE_PERMISSIONS = "route_storage_permission"

/**
 * List of required storage permissions, adapting to different Android versions.
 *
 * For Android Tiramisu and above, it includes granularmedia permissions:
 *  * [android.Manifest.permission.READ_MEDIA_VIDEO]
 *  * [android.Manifest.permission.READ_MEDIA_IMAGES]
 *
 * For older Android versions, it uses the broader:
 *  * [android.Manifest.permission.WRITE_EXTERNAL_STORAGE]
 */
private val ADAPTIVE_STORAGE_PERMISSIONS =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        listOf(
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    else
        listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

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
        controller.graph.setStartDestination(FilesViewState.ROUTE)
        controller.navigate(FilesViewState.direction()) {
            popUpTo(ROUTE_STORAGE_PERMISSIONS) {
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
 * Represents the different types of navigation components that a navigation item can belong to.
 */
private enum class NavType { NavRail, Drawer, BottomBar }

/**
 * Represents a navigation item either in a [NavigationRail] (when [isNavRail] is true)
 * or a [BottomNavigation] (when [isNavRail] is false).
 *
 * @param label The text label associated with the navigation item.
 * @param icon The vector graphic icon representing the navigation item.
 * @param onClick The callback function to be executed when the navigation item is clicked.
 * @param modifier The modifier for styling and layout customization of the navigation item.
 * @param checked Indicates whether the navigation item is currently selected.
 * @param isNavRail Specifies whether the navigation item is intended for a [NavigationRail].
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
@NonRestartableComposable
private fun NavigationItem(
    label: CharSequence,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    type: NavType = NavType.BottomBar,
    colors: SelectableChipColors = NavigationItemDefaults.navigationItemColors()
) {
    val icon = @Composable {
        Icon(
            imageVector = icon,
            contentDescription = label.toString()
        )
    }
    val label = @Composable {
        Label(
            text = label,
        )
    }
    when (type) {
        NavType.NavRail -> NavigationRailItem(
            onClick = onClick,
            icon = icon,
            label = label,
            modifier = modifier.scale(0.83f),
            checked = checked,
            colors = colors,
        )

        NavType.Drawer -> NavigationDrawerItem(
            onClick = onClick,
            icon = icon,
            label = label,
            modifier = modifier.scale(0.85f),
            checked = checked,
            colors = colors,
            shape = AppTheme.shapes.compact
        )

        NavType.BottomBar -> NavigationBarItem(
            onClick = onClick,
            icon = icon,
            label = label,
            modifier = modifier.scale(0.83f),
            checked = checked,
            colors = colors
        )
    }
}

/**
 * return the navigation type based on the window size.
 */
private inline val WindowSize.navType
    get() = when {
        widthRange < Range.Medium -> NavType.BottomBar
        widthRange < Range.xLarge -> NavType.NavRail
        // FixME - For now return only rail as drawer looks pretty bad.
        else -> NavType.NavRail //TYPE_DRAWER_NAV
    }

private val NAV_RAIL_WIDTH = 96.dp
private val NAV_DRAWER_WIDTH = 256.dp

/**
 * Calculates an returns newWindowSizeClass after consuming sapce occupied by  [navType].
 *
 * @return consumed window class.
 * @see [navType]
 */
private inline val WindowSize.remaining
    get() = when {
        widthRange < Range.Medium -> consume(height = 56.dp)
        widthRange < Range.xLarge -> consume(width = NAV_RAIL_WIDTH)
        else -> consume(width = NAV_DRAWER_WIDTH)
    }

/**
 * Extension function for the NavController that facilitates navigation to a specified destination route.
 *
 * @param route The destination route to navigate to.
 *
 * This function uses the provided route to navigate using the navigation graph.
 * It includes additional configuration to manage the back stack and ensure a seamless navigation experience.
 * - It pops up to the start destination of the graph to avoid a buildup of destinations on the back stack.
 * - It uses the `launchSingleTop` flag to prevent multiple copies of the same destination when re-selecting an item.
 * - The `restoreState` flag is set to true, ensuring the restoration of state when re-selecting a previously selected item.
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
private fun NavBar(
    type: NavType,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val routes = remember {
        movableContentOf {
            // Get the current navigation destination from NavController
            val current by navController.currentBackStackEntryAsState()
            val colors = NavigationItemDefaults.navigationItemColors(
                contentColor = AppTheme.colors.onBackground,
            )
            val domain = current?.destination?.domain
            // Files
            NavigationItem(
                label = textResource(R.string.photos),
                icon = Icons.Outlined.PhotoLibrary,
                checked = domain == Files.DOMAIN,
                onClick = { navController.toRoute(Files.direction()) },
                type = type,
                colors = colors
            )

            // Folders
            NavigationItem(
                label = textResource(R.string.folders),
                icon = Icons.Outlined.Folder,
                checked = domain == FoldersViewState.DOMAIN,
                onClick = { navController.toRoute(FoldersViewState.direction()) },
                type = type,
                colors = colors
            )

            // Folders
            NavigationItem(
                label = textResource(R.string.settings),
                icon = Icons.Outlined.Settings,
                checked = domain == Settings.DOMAIN,
                onClick = { navController.toRoute(Settings.direction()) },
                type = type,
                colors = colors
            )
        }
    }

    when (type) {
        NavType.NavRail -> NavigationRail(
            modifier = modifier.width(NAV_RAIL_WIDTH),
            windowInsets = WindowInsets.systemBars,
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
            modifier = modifier,
            windowInsets = WindowInsets.navigationBars,
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            contentPadding = PaddingValues(
                horizontal = AppTheme.padding.normal,
                vertical = AppTheme.padding.medium
            ),
            content = {
                Spacer(Modifier.weight(1f))
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
    arrayOf(
        FilesViewState.DOMAIN,
        FoldersViewState.DOMAIN,
        Settings.DOMAIN
    )

/**
 * Defines the navigation graph for the application.
 */
private val NavGraphBuilder: NavGraphBuilder.() -> Unit = {
    // Route for handling storage permissions.
    composable(ROUTE_STORAGE_PERMISSIONS) {
        Permission() // Composable function for handling permissions.
    }

    // Route for the Files screen.
    composable(Files.ROUTE) {
        val state = koinViewModel<FilesViewModel>()
        Files(state)
    }

    // Route for the Folders screen.
    composable(FoldersViewState.ROUTE) {
        val state = koinViewModel<FoldersViewModel>()
        Folders(state)
    }

    // Route for the Settings screen.
    composable(Settings.ROUTE) {
        val state = koinViewModel<SettingsViewModel>()
        Settings(state = state)
    }
}

// Default Enter/Exit Transitions.
@OptIn(ExperimentalAnimationApi::class)
private val EnterTransition =
    scaleIn(tween(220, 90), 0.98f) + fadeIn(tween(700))
private val ExitTransition = fadeOut(tween(700))

@Composable
fun Home(toastHostState: ToastHostState) {
    val isLight = requiresLightTheme()
    val navController = rememberNavController()
    AppTheme(isLight = isLight) {
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
                val hideNavigationBar =
                    navController.current?.domain !in DOMAINS_REQUIRING_NAV_BAR
                Log.d(TAG, "Home: ${navController.current?.domain}")
                NavigationSuiteScaffold(
                    vertical = clazz.widthRange < Range.Medium,
                    channel = toastHostState,
                    hideNavigationBar = hideNavigationBar,
                    //progress = facade.inAppUpdateProgress,
                    background = AppTheme.colors.background(elevation = 1.dp),
                    // Set up the navigation bar using the NavBar composable
                    navBar = {
                        NavBar(
                            type = clazz.navType,
                            navController = navController
                        )
                    },
                    // Display the main content of the app using the NavGraph composable
                    content = {
                        val context = LocalContext.current
                        // Load start destination based on if storage permission is set or not.
                        val granted = context.isPermissionGranted(ADAPTIVE_STORAGE_PERMISSIONS[0])
                        val startDestination =
                            if (granted) Files.ROUTE else ROUTE_STORAGE_PERMISSIONS
                        // Create the NavHost.
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            builder = NavGraphBuilder,
                            modifier = Modifier
                                .clip(CONTENT_SHAPE)
                                .background(AppTheme.colors.background)
                                .fillMaxSize(),
                            enterTransition = { EnterTransition },
                            exitTransition = { ExitTransition },
                        )
                    }
                )
            }
        )
    }
}