package com.prime.gallery

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.prime.gallery.core.NightMode
import com.prime.gallery.core.compose.BottomBarItem
import com.prime.gallery.core.compose.LocalNavController
import com.prime.gallery.core.compose.LocalSystemFacade
import com.prime.gallery.core.compose.LocalWindowSizeClass
import com.prime.gallery.core.compose.NavigationRailItem2
import com.prime.gallery.core.compose.Placeholder
import com.prime.gallery.core.compose.Scaffold2
import com.prime.gallery.core.compose.Typography
import com.prime.gallery.core.compose.preference
import com.prime.gallery.core.compose.snackbar.SnackbarHostState2
import com.prime.gallery.files.Files
import com.prime.gallery.folders.Folders
import com.prime.gallery.impl.FilesViewModel
import com.prime.gallery.impl.FoldersViewModel
import com.prime.gallery.impl.SettingsViewModel
import com.prime.gallery.impl.ViewerViewModel
import com.prime.gallery.settings.Settings
import com.prime.gallery.viewer.Viewer
import com.primex.core.rememberState

private const val TAG = "Home"

/**
 * A short-hand alias of [MaterialTheme]
 */
typealias Material = MaterialTheme

/**
 * A simple composable that helps in resolving the current app theme as suggested by the [Gallery.NIGHT_MODE]
 */
@Composable
@NonRestartableComposable
private fun isPrefDarkTheme(): Boolean {
    val mode by preference(key = Settings.KEY_NIGHT_MODE)
    return when (mode) {
        NightMode.YES -> true
        NightMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        else -> false
    }
}

// Default Enter/Exit Transitions.
private val EnterTransition = scaleIn(tween(220, 90), 0.98f) +
        fadeIn(tween(700))
private val ExitTransition = fadeOut(tween(700))

private val DarkColorScheme = darkColorScheme(background = Color(0xFF0E0E0F))
private val LightColorScheme = lightColorScheme()

private val DefaultTypography = Typography(defaultFontFamily = Settings.LatoFontFamily)

@Composable
@NonRestartableComposable
private fun Material(
    darkTheme: Boolean,
    dynamicColor: Boolean,  // Dynamic color is available on Android 12+
    content: @Composable () -> Unit,
) {
    // compute the color scheme.
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // Pass values to the actual composable.
    MaterialTheme(colorScheme, content = content, typography = DefaultTypography)
}

/**
 * The route to permission screen.
 */
private const val PERMISSION_ROUTE = "_route_storage_permission"

/**
 * The permission screen.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Permission() {
    val controller = LocalNavController.current
    // Compose the permission state.
    // Once granted set different route like folders as start.
    // Navigate from here to there.
    val permission = rememberPermissionState(permission = Gallery.STORAGE_PERMISSION) {
        if (!it) return@rememberPermissionState
        controller.graph.setStartDestination(Files.route)
        controller.navigate(Files.direction()) { popUpTo(PERMISSION_ROUTE) { inclusive = true } }
    }
    Placeholder(
        iconResId = R.raw.lt_permission,
        title = stringResource(R.string.storage_permission),
        message = stringResource(R.string.storage_permission_msg),
    ) {
        com.primex.material3.OutlinedButton(
            onClick = { permission.launchPermissionRequest() },
            modifier = Modifier.size(width = 200.dp, height = 46.dp),
            elevation = null,
            label = stringResource(R.string.allow),
            border = ButtonDefaults.outlinedButtonBorder,
            shape = CircleShape,
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
        )
    }
}


/**
 * A simple structure of the NavGraph.
 */
@NonRestartableComposable
@Composable
private fun NavGraph(
    controller: NavHostController,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    // Load start destination based on if storage permission is set or not.
    val startDestination =
        when (ContextCompat.checkSelfPermission(ctx, Gallery.STORAGE_PERMISSION)) {
            PackageManager.PERMISSION_GRANTED -> Files.route
            else -> PERMISSION_ROUTE
        }
    // In order to navigate and remove the need to pass controller below UI components.
    // pass controller as composition local.
    CompositionLocalProvider(
        LocalNavController provides controller,
        content = {
            // actual paragraph
            NavHost(
                navController = controller,
                modifier = modifier,
                startDestination = startDestination, //
                enterTransition = { EnterTransition },
                exitTransition = { ExitTransition },
                builder = {
                    //Permission
                    composable(PERMISSION_ROUTE) {
                        Permission()
                    }

                    //Folders
                    composable(Folders.route) {
                        val viewModel = hiltViewModel<FoldersViewModel>()
                        Folders(state = viewModel)
                    }

                    //Images
                    composable(Files.route) {
                        val viewModel = hiltViewModel<FilesViewModel>()
                        Files(state = viewModel)
                    }

                    //Viewer
                    composable(Viewer.route) {
                        val viewModel = hiltViewModel<ViewerViewModel>()
                        Viewer(state = viewModel)
                    }

                    //Settings
                    composable(Settings.route) {
                        val viewModel = hiltViewModel<SettingsViewModel>()
                        Settings(state = viewModel)
                    }

                }
            )
        }
    )
}

@Composable
@NonRestartableComposable
private fun NavRail(
    controller: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.widthIn(min = 120.dp),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        var selected by rememberState(initial = 0)
        NavigationRailItem2(
            label = "Timeline",
            icon = Icons.Outlined.Timeline,
            onClick = { selected = 0 },
            selected = selected == 0
        )
        NavigationRailItem2(
            label = "Folders",
            icon = Icons.Outlined.Folder,
            onClick = { selected = 1 },
            selected = selected == 1
        )

        NavigationRailItem2(
            label = "Albums",
            icon = Icons.Outlined.Timeline,
            onClick = { selected = 2 },
            selected = selected == 2
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
@NonRestartableComposable
private fun BottomBar(
    controller: NavHostController,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        var selected by rememberState(initial = 0)
        BottomBarItem(
            label = "Timeline",
            icon = Icons.Outlined.Timeline,
            onClick = { selected = 0 },
            selected = selected == 0
        )
        BottomBarItem(
            label = "Folders",
            icon = Icons.Outlined.Folder,
            onClick = { selected = 1 },
            selected = selected == 1
        )

        BottomBarItem(
            label = "Albums",
            icon = Icons.Outlined.Timeline,
            onClick = { selected = 2 },
            selected = selected == 2
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Home(channel: SnackbarHostState2) {
    val darkTheme = isPrefDarkTheme()
    // Observe if the user wants dynamic light.
    // Supports only above android 12+
    val dynamicColor by preference(key = Settings.KEY_DYNAMIC_COLORS)
    Material(darkTheme, dynamicColor) {
        // Place the content.
        val vertical = LocalWindowSizeClass.current.widthSizeClass < WindowWidthSizeClass.Medium
        val facade = LocalSystemFacade.current
        val navController = rememberNavController()
        // Current route.
        val current by navController.currentBackStackEntryAsState()
        val hideNavigationBar = when (current?.destination?.route) {
            Settings.route, PERMISSION_ROUTE -> true
            else -> false
        }
        Scaffold2(
            vertical = vertical,
            channel = channel,
            content = { NavGraph(controller = navController) },
            progress = facade.inAppUpdateProgress,
            hideNavigationBar = hideNavigationBar,
            navBar = { if (vertical) BottomBar(navController) else NavRail(navController) }
        )

        // handle the color of navBars.
        val view = LocalView.current
        if (view.isInEditMode)
            return@Material
        // Observe if the user wants to color the SystemBars
        val colorSystemBars by preference(key = Settings.KEY_COLOR_STATUS_BAR)
        val systemBarsColor =
            if (colorSystemBars) Material.colorScheme.primary else Color.Transparent
        val hideStatusBar by preference(key = Settings.KEY_HIDE_STATUS_BAR)
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = systemBarsColor.toArgb()
            window.statusBarColor = systemBarsColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !darkTheme && !colorSystemBars
            //
            if (hideStatusBar) WindowCompat.getInsetsController(window, view)
                .hide(WindowInsetsCompat.Type.statusBars())
            else WindowCompat.getInsetsController(window, view)
                .show(WindowInsetsCompat.Type.statusBars())
        }
    }
}

