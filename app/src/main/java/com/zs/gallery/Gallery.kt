/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 11-09-2024.
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

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.NavigationRail
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.primex.core.MetroGreen
import com.primex.core.OrientRed
import com.primex.core.plus
import com.primex.core.textResource
import com.primex.material2.Label
import com.primex.material2.OutlinedButton
import com.zs.foundation.AppTheme
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.NightMode
import com.zs.foundation.Range
import com.zs.foundation.WindowSize
import com.zs.foundation.WindowStyle
import com.zs.foundation.adaptive.NavigationItemDefaults
import com.zs.foundation.adaptive.NavigationSuiteScaffold
import com.zs.foundation.calculateWindowSizeClass
import com.zs.foundation.checkSelfPermissions
import com.zs.foundation.dynamicAccentColor
import com.zs.foundation.isAppearanceLightSystemBars
import com.zs.foundation.shapes.EndConcaveShape
import com.zs.foundation.shapes.TopConcaveShape
import com.zs.foundation.thenIf
import com.zs.foundation.toast.ToastHostState
import com.zs.gallery.bin.RouteTrash
import com.zs.gallery.bin.Trash
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.NavItem
import com.zs.gallery.common.Regular
import com.zs.gallery.common.Route
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.composable
import com.zs.gallery.common.current
import com.zs.gallery.common.domain
import com.zs.gallery.common.dynamicBackdrop
import com.zs.gallery.common.preference
import com.zs.gallery.common.rememberHazeState
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
import com.zs.gallery.impl.TrashViewModel
import com.zs.gallery.impl.ViewerViewModel
import com.zs.gallery.lockscreen.LockScreen
import com.zs.gallery.lockscreen.RouteLockScreen
import com.zs.gallery.settings.RouteSettings
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.RouteViewer
import com.zs.gallery.viewer.Viewer
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.layout.PaddingValues as Padding
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient as HorizontalGradient
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient as VerticalGradient
import androidx.navigation.compose.currentBackStackEntryAsState as current
import com.google.accompanist.permissions.rememberMultiplePermissionsState as Permissions
import com.primex.core.textResource as stringResource
import com.zs.foundation.renderInSharedTransitionScopeOverlay as renderInOverlay

private const val TAG = "Gallery"

private val NAV_RAIL_MIN_WIDTH = 106.dp
private val BOTTOM_NAV_MIN_HEIGHT = 56.dp

private val LightAccentColor = Color.OrientRed
private val DarkAccentColor = Color(0xFFD8A25E)

/**
 *Navigates to the specified route, managing the back stack for a seamless experience.
 * Pops up to the start destination and uses launchSingleTop to prevent duplicate destinations.
 *
 * @param route The destination route.
 */
private fun NavController.toRoute(route: Route) {
    navigate(route()) {
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
 * The set of domains that require the navigation bar to be shown.
 * For other domains, the navigation bar will be hidden.
 */
private val DOMAINS_REQUIRING_NAV_BAR =
    arrayOf(RouteTimeline.domain, RouteFolders.domain, RouteSettings.domain)

/**
 * List of permissions required to run the app.
 *
 * This list is constructed based on the device's Android version to ensure
 * compatibility with scoped storage and legacy storage access.
 */
@SuppressLint("BuildListAdds")
private val REQUIRED_PERMISSIONS = buildList {
    // For Android Tiramisu (33) and above, use media permissions for scoped storage
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this += android.Manifest.permission.ACCESS_MEDIA_LOCATION
        this += android.Manifest.permission.READ_MEDIA_VIDEO
        this += android.Manifest.permission.READ_MEDIA_IMAGES
    }
    // For Android Upside Down Cake (34) and above, add permission for user-selected visual media
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        this += android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    // For Android versions below Tiramisu 10(29), request WRITE_EXTERNAL_STORAGE for
    // legacy storage access
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
        this += android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        this += android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private object RoutePermission : Route

/**
 * Represents the permission screen
 * @see REQUIRED_PERMISSIONS
 * @see RoutePermission
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Permission() {
    val controller = LocalNavController.current
    // Compose the permission state.
    // Once granted set different route like folders as start.
    // Navigate from here to there.
    val permission =
        Permissions(permissions = REQUIRED_PERMISSIONS) {
            if (!it.all { (_, state) -> state }) return@Permissions
            controller.graph.setStartDestination(RouteTimeline())
            controller.navigate(RouteTimeline()) {
                popUpTo(RoutePermission()) {
                    inclusive = true
                }
            }
        }
    // If the permissions are not granted, show the permission screen.
    com.zs.gallery.common.Placeholder(
        iconResId = R.raw.lt_permission,
        title = stringResource(R.string.permission_scr_title).toString(),
        message = textResource(R.string.permission_scr_desc),
        vertical = LocalWindowSize.current.widthRange == Range.Compact
    ) {
        OutlinedButton(
            onClick = permission::launchMultiplePermissionRequest,
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
 *  The navigation graph for the app.
 */
private val navGraphBuilder: NavGraphBuilder.() -> Unit = {
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
    composable(RouteViewer) {
        val state = koinViewModel<ViewerViewModel>()
        Viewer(viewState = state)
    }

    // Folder - That displays files
    composable(RouteFolder) {
        val state = koinViewModel<FolderViewModel>()
        Folder(viewState = state)
    }

    // Album
    composable(RouteAlbum) {
        val state = koinViewModel<AlbumViewModel>()
        Album(viewState = state)
    }

    // Trash
    composable(RouteTrash) {
        val state = koinViewModel<TrashViewModel>()
        Trash(viewState = state)
    }

    // LockScreen
    composable(RouteLockScreen) {
        LockScreen()
    }
}

private val BottomNavShape = TopConcaveShape(radius = 20.dp)
private val NavRailShape = EndConcaveShape(16.dp)

/**
 * A composable function that represents a navigation bar, combining both rail and bottom bar elements.
 *
 * @param typeRail Specifies whether the navigation bar includes a [NavigationRail] or [BottomNavigation] component.
 * @param navController The NavController to manage navigation within the navigation bar.
 * @param modifier The modifier for styling and layout customization of the navigation bar.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
@NonRestartableComposable
private fun NavigationBar(
    typeRail: Boolean,
    contentColor: Color,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val routes = @Composable {
        // Get the current navigation destination from NavController
        val current by navController.current()
        val color = LocalContentColor.current
        val colors = NavigationItemDefaults.navigationItemColors(
            selectedContentColor = color,
            selectedBackgroundColor = color.copy(0.12f)
        )
        val domain = current?.destination?.domain
        val facade = LocalSystemFacade.current

        // Timeline
        NavItem(
            label = { Label(text = textResource(R.string.timeline)) },
            icon = { Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null) },
            checked = domain == RouteTimeline.domain,
            onClick = { facade.launchReviewFlow(); navController.toRoute(RouteTimeline) },
            typeRail = typeRail,
            colors = colors
        )

        // Folders
        NavItem(
            label = { Label(text = textResource(R.string.folders)) },
            icon = { Icon(imageVector = Icons.Filled.FolderCopy, contentDescription = null) },
            checked = domain == RouteFolders.domain,
            onClick = { facade.launchReviewFlow(); navController.toRoute(RouteFolders) },
            typeRail = typeRail,
            colors = colors
        )

        // Settings
        NavItem(
            label = { Label(text = textResource(R.string.settings)) },
            icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = null) },
            checked = domain == RouteSettings.domain,
            onClick = { facade.launchReviewFlow(); navController.toRoute(RouteSettings) },
            typeRail = typeRail,
            colors = colors
        )
    }
    // Get the current theme colors
    val colors = AppTheme.colors
    //
    when {
        typeRail -> NavigationRail(
            modifier = Modifier
                .border(
                    0.5.dp,
                    HorizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Gray.copy(if (colors.isLight) 0.16f else 0.24f),
                            Color.Transparent,
                        )
                    ),
                    NavRailShape
                )
                .clip(NavRailShape)
                .then(modifier)
                .widthIn(NAV_RAIL_MIN_WIDTH),
            windowInsets = WindowInsets.statusBars,
            contentColor = contentColor,
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
            windowInsets = WindowInsets.navigationBars,
            contentColor = contentColor,
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            contentPadding = Padding(
                horizontal = AppTheme.padding.normal,
                vertical = AppTheme.padding.medium
            ) + Padding(top = 16.dp),
            modifier = Modifier
                .border(
                    0.5.dp,
                    VerticalGradient(
                        listOf(
                            if (colors.isLight) colors.background(2.dp) else Color.Gray.copy(0.24f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                        )
                    ),
                    BottomNavShape
                )
                .clip(BottomNavShape)
                .then(modifier)
                .heightIn(BOTTOM_NAV_MIN_HEIGHT),
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
 * Adjusts the [WindowSize] by consuming either the navigation rail width or the bottom navigation height.
 *
 * @param rail Boolean indicating whether to consume the navigation rail width.
 * @return [WindowSize] with the specified dimension consumed.
 */
private fun WindowSize.consume(rail: Boolean) =
    if (rail) consume(width = NAV_RAIL_MIN_WIDTH) else consume(height = BOTTOM_NAV_MIN_HEIGHT)

/**
 * Provides a [Density] object that reflects the user's preferred font scale.
 *
 * This extension function on [Preferences] observes the `KEY_FONT_SCALE` preference
 * and returns a modified [Density] object if the user has set a custom font scale.
 * If the font scale is set to -1 (default), the current [LocalDensity] is returned.
 *
 * @return A [Density] object with the appropriate font scale applied.
 */
private val SystemFacade.density: Density
    @NonRestartableComposable
    @Composable
    inline get() {
        // Observe font scale preference and create a modified Density if necessary
        val fontScale by observeAsState(key = Settings.KEY_FONT_SCALE)
        val density = LocalDensity.current
        return if (fontScale == -1f) density else Density(density.density, fontScale)
    }

/**
 * Represents the entry to the app ui.
 */
@Composable
fun Gallery(
    origin: Route,
    toastHostState: ToastHostState,
    navController: NavHostController,
) {
    // dependencies
    val activity = LocalView.current.context as MainActivity
    val clazz = calculateWindowSizeClass(activity = activity)
    val current = navController.current

    // properties
    val style = (activity as SystemFacade).style
    val requiresNavBar = when (style.flagAppNavBar) {
        WindowStyle.FLAG_APP_NAV_BAR_HIDDEN -> false
        WindowStyle.FLAG_APP_NAV_BAR_VISIBLE -> true
        else -> current?.domain in DOMAINS_REQUIRING_NAV_BAR  // auto
    }
    // Determine the screen orientation.
    // This check assesses whether to display NavRail or BottomBar.
    // BottomBar appears only if the window size suits a mobile screen.
    // Consider this scenario: a large screen that fits the mobile description, like a desktop screen in portrait mode.
    // In this case, showing the BottomBar is preferable!
    val portrait = clazz.widthRange < Range.Medium
    val provider = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> rememberHazeState()
        else -> null
    }
    // content
    val content = @Composable {
        NavigationSuiteScaffold(
            vertical = portrait,
            toastHostState = toastHostState,
            hideNavigationBar = !requiresNavBar,
            background = AppTheme.colors.background,
            progress = activity.inAppUpdateProgress,
            // Set up the navigation bar using the NavBar composable
            navBar = {
                val useAccent by preference(Settings.KEY_USE_ACCENT_IN_NAV_BAR)
                val colors = AppTheme.colors
                NavigationBar(
                    !portrait,
                    if (useAccent) colors.onAccent else colors.onBackground,
                    navController,
                    when {
                        useAccent -> Modifier.background(colors.accent)
                        else -> Modifier.dynamicBackdrop(
                            if (!portrait) null else provider,
                            HazeStyle.Regular(
                                colors.background(if (colors.isLight) 15.dp else 0.dp),
                                if (colors.isLight) 0.24f else 0.63f
                            ),
                            colors.background,
                            colors.accent
                        )
                    }.renderInOverlay(0.2f),
                )
            },
            // Display the main content of the app using the NavGraph composable
            content = {
                // Load start destination based on if storage permission is set or not.
                val granted = activity.checkSelfPermissions(REQUIRED_PERMISSIONS)
                NavHost(
                    navController = navController,
                    startDestination = if (!granted) RoutePermission() else origin(),
                    builder = navGraphBuilder,
                    modifier = Modifier.thenIf(provider != null) { haze(provider!!) }
                )
            }
        )
    }
    // Check if light theme is preferred
    val isDark = run {
        val mode by activity.observeAsState(key = Settings.KEY_NIGHT_MODE)
        when (mode) {
            NightMode.YES -> true
            NightMode.NO -> false
            NightMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        }
    }
    // Setup App Theme and provide necessary dependencies.
    // Provide the navController and window size class to child composable.
    val useDynamicColors by activity.observeAsState(Settings.KEY_DYNAMIC_COLORS)
    AppTheme(
        isLight = !isDark,
        fontFamily = Settings.DefaultFontFamily,
        accent = when {
            useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicAccentColor(activity, isDark)
            isDark -> DarkAccentColor
            else -> LightAccentColor
        },
        content = {
            // Provide the navController, newWindowClass through LocalComposition.
            CompositionLocalProvider(
                LocalNavController provides navController,
                LocalElevationOverlay provides null,  // Disable absolute elevation.
                LocalSystemFacade provides (activity as SystemFacade),
                LocalDensity provides activity.density,
                LocalWindowSize provides if (!requiresNavBar) clazz else clazz.consume(!portrait),
                content = content
            )
        }
    )

    // Observe the state of the IMMERSE_VIEW setting
    // Observe the state of the IMMERSE_VIEW setting
    val immersiveView by activity.observeAsState(Settings.KEY_IMMERSIVE_VIEW)
    val transparentSystemBars by activity.observeAsState(Settings.KEY_TRANSPARENT_SYSTEM_BARS)
    LaunchedEffect(immersiveView, style, isDark, transparentSystemBars) {
        // Get the WindowInsetsController for managing system bars
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Determine the visibility of system bars based on the current style settings
        val visible = when (style.flagSystemBarVisibility) {
            WindowStyle.FLAG_SYSTEM_BARS_HIDDEN -> false  // Hide system bars
            WindowStyle.FLAG_SYSTEM_BARS_VISIBLE -> true  // Show system bars
            else -> !immersiveView  // If not explicitly set, use the immersiveView setting
        }
        // Apply the visibility setting to the system bars
        if (!visible) controller.hide(WindowInsetsCompat.Type.systemBars())
        else controller.show(WindowInsetsCompat.Type.systemBars())
        // Determine the appearance of system bars (dark or light) based on the current style settings
        controller.isAppearanceLightSystemBars = when (style.flagSystemBarAppearance) {
            WindowStyle.FLAG_SYSTEM_BARS_APPEARANCE_DARK -> false  // Use dark system bars appearance
            WindowStyle.FLAG_SYSTEM_BARS_APPEARANCE_LIGHT -> true  // Use light system bars appearance
            else -> !isDark  // If not explicitly set, use the isDark setting
        }
        // Configure the system bars background color based on the current style settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
            return@LaunchedEffect // No supported from here.
        window.apply {
            val color = when (style.flagSystemBarBackground) {
                WindowStyle.FLAG_SYSTEM_BARS_BG_TRANSLUCENT -> Color(0x20000000).toArgb()  // Translucent background
                WindowStyle.FLAG_SYSTEM_BARS_BG_TRANSPARENT -> Color.Transparent.toArgb()  // Transparent background
                else -> (if (!transparentSystemBars) Color(0x20000000) else Color.Transparent).toArgb()// automate using the setting
            }
            // Set the status and navigation bar colors
            statusBarColor = color
            navigationBarColor = color
        }
    }
}