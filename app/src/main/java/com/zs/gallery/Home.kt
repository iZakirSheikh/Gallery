/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 04-04-2025.
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
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.ClaretViolet
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.Icon
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.MotionScheme
import com.zs.compose.theme.OutlinedButton
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.NavigationSuiteScaffold
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.appbar.FloatingBottomNavigationBar
import com.zs.compose.theme.appbar.NavigationItem
import com.zs.compose.theme.appbar.NavigationItemDefaults
import com.zs.compose.theme.appbar.SideBar
import com.zs.compose.theme.calculateWindowSizeClass
import com.zs.compose.theme.dynamicAccentColor
import com.zs.compose.theme.renderInSharedTransitionScopeOverlay
import com.zs.compose.theme.snackbar.SnackbarHostState
import com.zs.compose.theme.text.Label
import com.zs.core.common.checkSelfPermissions
import com.zs.core.isAppearanceLightSystemBars
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.Route
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.WindowStyle
import com.zs.gallery.common.compose.LocalNavController
import com.zs.gallery.common.compose.LocalSystemFacade
import com.zs.gallery.common.compose.background
import com.zs.gallery.common.compose.composable
import com.zs.gallery.common.compose.preference
import com.zs.gallery.common.compose.rememberAcrylicSurface
import com.zs.gallery.common.compose.shine
import com.zs.gallery.common.compose.source
import com.zs.gallery.common.domain
import com.zs.gallery.common.shapes.EndConcaveShape
import com.zs.gallery.files.Files
import com.zs.gallery.files.RouteFiles
import com.zs.gallery.folders.Folders
import com.zs.gallery.folders.RouteFolders
import com.zs.gallery.impl.FilesViewModel
import com.zs.gallery.impl.FoldersViewModel
import com.zs.gallery.impl.MediaViewerViewModel
import com.zs.gallery.impl.SettingsViewModel
import com.zs.gallery.lockscreen.LockScreen
import com.zs.gallery.lockscreen.RouteLockScreen
import com.zs.gallery.settings.RouteSettings
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.IntentViewer
import com.zs.gallery.viewer.MediaViewer
import com.zs.gallery.viewer.RouteIntentViewer
import com.zs.gallery.viewer.RouteViewer
import org.koin.androidx.compose.koinViewModel
import com.google.accompanist.permissions.rememberMultiplePermissionsState as Permissions

private const val TAG = "Home"

private val LightAccentColor = /*Color(0xFF514700)*/ Color.ClaretViolet
private val DarkAccentColor = Color(0xFFD8A25E)

private val SIDE_BAR_WIDTH = 100.dp

/**
 * Determines the primary navigation route domain from the current [NavController] back stack.
 *
 * This extension property observes the current back stack entry and identifies the domain of
 * the top-level destination if it's one of the known primary routes (RouteFiles, RouteFolders, RouteSettings)
 * and has no arguments.
 *
 * The result is memoized using [remember] and updated efficiently with [derivedStateOf]
 * whenever the back stack changes.
 *
 * @return A [State] holding the domain string of the primary route, or `null` if none is active.
 */
private val NavController.primary: State<String?>
    @Composable
    inline get() {
        // Observe the current back stack entry as state
        val entry by currentBackStackEntryAsState()
        return remember() {
            derivedStateOf {
                val dest = entry?.destination ?: return@derivedStateOf null

                // Check if the destination is one of the known top-level domains
                val isPrimary = when (dest.domain) {
                    RouteFiles.domain -> true
                    RouteFolders.domain -> true
                    RouteSettings.domain -> true
                    else -> false
                }

                // Determines if the current route is a primary, top-level screen without arguments.
                //
                // TODO: Investigate why arguments are sometimes present in `dest.arguments`
                //       and other times in `entry?.arguments`.
                //
                // The logic is as follows:
                // 1. Check if the current route's domain is one of the primary routes.
                // 2. Verify that there are no arguments in `dest.arguments`.
                // 3. Additionally, ensure that `entry?.arguments` is either null or contains only one entry
                //    (which might be a default or system-added argument).
                // If all conditions are met, the domain of the primary route is returned.
                // Otherwise, `null` is returned, indicating it's not a primary, argument-less route.
                Log.d(TAG, "args: ${dest.arguments} | ${entry?.arguments?.size()}")
                if (isPrimary &&( dest.arguments.isEmpty() && (entry?.arguments == null || entry?.arguments?.size() == 1))) dest.domain else null
            }
        }
    }

private val NavRailShape = EndConcaveShape(12.dp)
private val NavRailBorder = BorderStroke(
    0.5.dp,
    Brush.horizontalGradient(
        listOf(
            Color.Transparent,
            Color.Transparent,
            Color.Gray.copy(0.20f),
            Color.Transparent,
        )
    )
)

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

//
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
            controller.graph.setStartDestination(RouteFiles())
            controller.navigate(RouteFiles()) {
                popUpTo(RoutePermission()) {
                    inclusive = true
                }
            }
        }
    // If the permissions are not granted, show the permission screen.
    com.zs.gallery.common.compose.Placeholder(
        iconResId = R.raw.lt_permission,
        title = stringResource(R.string.permission_scr_title),
        message = textResource(R.string.permission_scr_desc),
        vertical = LocalWindowSize.current.width == Category.Small,
        action = {
            OutlinedButton(
                onClick = permission::launchMultiplePermissionRequest,
                modifier = Modifier.size(width = 200.dp, height = 46.dp),
                text = stringResource(R.string.allow),
                shape = CircleShape
            )
        }
    )
}

/**
 *  The navigation graph for the app.
 */
private val navGraphBuilder: NavGraphBuilder.() -> Unit = {

    // LockScreen
    composable(RouteLockScreen) { LockScreen() }

    // Route for handling storage permissions.
    composable(RoutePermission) { Permission() }

    // RouteFiles
    composable(RouteFiles) {
        val state = koinViewModel<FilesViewModel>()
        Files(viewState = state)
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
        val state = koinViewModel<MediaViewerViewModel>()
        MediaViewer(viewState = state)
    }

    // IntentViewer
    composable(RouteIntentViewer) {
        val args = it.arguments
        val intent = when {
            args != null -> RouteIntentViewer.buildArgs(args)
            else -> {
                // TODO - Find actual way to do this;
                val activity = (LocalSystemFacade.current as Activity)
                val i = activity.intent
                i.data!! to (i.type ?: "image/*")
            }
        }
        IntentViewer(intent)
    }
}

/**
 * A composable function that represents a navigation bar, combining both rail and bottom bar elements.
 *
 * @param isBottomAligned Specifies whether the navigation bar includes a [NavigationRail] or [BottomNavigation] component.
 * @param navController The NavController to manage navigation within the navigation bar.
 * @param modifier The modifier for styling and layout customization of the navigation bar.
 */
@Composable
@NonRestartableComposable
private fun NavigationBar(
    isBottomAligned: Boolean,
    background: Background,
    contentColor: Color,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    // Get the current theme colors
    val colors = AppTheme.colors
    val routes = @Composable {
        // Get the current navigation destination from NavController
        val current by navController.currentBackStackEntryAsState()
        val colors = NavigationItemDefaults.colors(
            selectedIndicatorColor = if (contentColor == colors.onAccent) contentColor.copy(
                ContentAlpha.indication
            ) else colors.accent,
            selectedTextColor = if (contentColor == colors.onAccent) contentColor else colors.accent,
            unselectedIconColor = if (contentColor == colors.onAccent) colors.onAccent else colors.onBackground,
            unselectedTextColor = if (contentColor == colors.onAccent) colors.onAccent else colors.onBackground
        )

        // Required to launch review.
        val facade = LocalSystemFacade.current
        val primary by navController.primary

        // Timeline
        NavigationItem(
            label = { Label(text = textResource(R.string.timeline)) },
            icon = { Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null) },
            selected = primary == RouteFiles.domain,
            onClick = { facade.initiateReviewFlow(); navController.toRoute(RouteFiles) },
            isBottomNav = isBottomAligned,
            colors = colors
        )

        // Folders
        NavigationItem(
            label = { Label(text = textResource(R.string.folders)) },
            icon = { Icon(imageVector = Icons.Filled.FolderCopy, contentDescription = null) },
            selected = primary == RouteFolders.domain,
            onClick = { facade.initiateReviewFlow(); navController.toRoute(RouteFolders) },
            isBottomNav = isBottomAligned,
            colors = colors
        )

        // Settings
        NavigationItem(
            label = { Label(text = textResource(R.string.settings)) },
            icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = null) },
            selected = primary == RouteSettings.domain,
            onClick = { facade.initiateReviewFlow(); navController.toRoute(RouteSettings) },
            isBottomNav = isBottomAligned,
            colors = colors
        )
    }
    // Load appropriate navigation bar.
    when {
        isBottomAligned -> FloatingBottomNavigationBar(
            contentColor = contentColor,
            background = background,
            elevation = 12.dp,
            border = colors.shine,
            shape = CircleShape,
            modifier = modifier,
            // Display routes at the contre of available space
            content = { routes() }
        )

        else -> SideBar(
            modifier = modifier.width(SIDE_BAR_WIDTH),
            windowInsets = AppBarDefaults.sideBarWindowInsets,
            contentColor = contentColor,
            border = NavRailBorder,
            shape = NavRailShape,
            background = background,
            elevation = 0.dp,
            content = { routes() },
        )
    }
}

/** The main navigation host for the app. */
@Composable
fun Home(
    origin: Route,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
) {
    // dependencies
    val activity = LocalView.current.context as MainActivity
    val clazz = calculateWindowSizeClass(activity = activity)
    val current by navController.currentBackStackEntryAsState()

    // properties
    val style = (activity as SystemFacade).style
    val primary by navController.primary
    val requiresNavBar = when (style.flagNavBarVisibility) {
        WindowStyle.FLAG_APP_NAV_BAR_HIDDEN -> false
        WindowStyle.FLAG_APP_NAV_BAR_VISIBLE -> true
        else -> primary != null // auto
    }
    // Determine the screen orientation.
    // This check assesses whether to display NavRail or BottomBar.
    // BottomBar appears only if the window size suits a mobile screen.
    // Consider this scenario: a large screen that fits the mobile description, like a desktop screen in portrait mode.
    // In this case, maybe showing the BottomBar is preferable!
    val portrait = clazz.width < Category.Medium
    val surface = rememberAcrylicSurface()

    // content
    val content = @Composable {
        NavigationSuiteScaffold(
            vertical = portrait,
            snackbarHostState = snackbarHostState,
            hideNavigationBar = !requiresNavBar,
            containerColor = AppTheme.colors.background,
            progress = activity.inAppUpdateProgress,
            // Set up the navigation bar using the NavBar composable
            navBar = {
                val useAccent by preference(Settings.KEY_USE_ACCENT_IN_NAV_BAR)
                val colors = AppTheme.colors
                NavigationBar(
                    portrait,
                    when {
                        useAccent -> Background(colors.accent)
                        !portrait -> Background(colors.background(2.dp))
                        else -> colors.background(surface)
                    },
                    if (useAccent) colors.onAccent else colors.onBackground,
                    navController,
                    Modifier.renderInSharedTransitionScopeOverlay(0.3f),
                )
            },
            // Display the main content of the app using the NavGraph composable
            content = {
                // Load start destination based on if storage permission is set or not.
                val granted = activity.checkSelfPermissions(REQUIRED_PERMISSIONS)
                val motion = AppTheme.motionScheme
                NavHost(
                    navController = navController,
                    startDestination = if (origin != RouteIntentViewer && !granted) RoutePermission() else origin(),
                    builder = navGraphBuilder,
                    modifier = Modifier.source(surface),
                    enterTransition = { scaleIn(motion.slowSpatialSpec(), 0.98f) + fadeIn(motion.slowEffectsSpec()) },
                    exitTransition = { fadeOut(motion.slowEffectsSpec()) },
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
        motionScheme = MotionScheme.expressive(),
        accent = when {
            useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicAccentColor(
                activity,
                isDark
            )

            isDark -> DarkAccentColor
            else -> LightAccentColor
        },
        content = {
            // Provide the navController, newWindowClass through LocalComposition.
            CompositionLocalProvider(
                LocalNavController provides navController,
                LocalSystemFacade provides (activity as SystemFacade),
                LocalDensity provides activity.density,
                LocalWindowSize provides when {
                    !requiresNavBar -> clazz
                    portrait -> clazz.consume(56.dp)
                    else -> clazz.consume(SIDE_BAR_WIDTH)
                },
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