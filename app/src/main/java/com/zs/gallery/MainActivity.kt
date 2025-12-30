package com.zs.gallery

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zs.common.analytics.Analytics
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.MotionScheme
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.NavigationSuiteScaffold
import com.zs.compose.theme.calculateWindowSizeClass
import com.zs.compose.theme.dynamicAccentColor
import com.zs.gallery.common.LocalNavigator
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.Navigator
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.Route
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.checkSelfPermissions
import com.zs.gallery.common.impl.FilesViewModel
import com.zs.gallery.common.impl.SettingsViewModel
import com.zs.gallery.common.preference
import com.zs.gallery.files.Files
import com.zs.gallery.intro.Onboarding
import com.zs.gallery.login.Login
import com.zs.preferences.Preferences
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.zs.compose.theme.snackbar.SnackbarHostState as SnackbarController
import com.zs.gallery.common.Gallery as G


class MainActivity : ComponentActivity(), SystemFacade {

    private val TAG = "MainActivity"


    val preferences: Preferences by inject()
    val controller: SnackbarController by inject()
    private lateinit var navigator: Navigator<Route>
    private val analytics: Analytics by inject()


    private val isAuthenticationRequired: Boolean get() = false


    override fun unlock() {
        TODO("Not yet implemented")
    }


    val navEntryProvider: (Route) -> NavEntry<Route> = {key: Route ->
        when(key){
            Route.AboutUs -> TODO()
            is Route.Files -> NavEntry(key, content = {
                val viewModel = koinViewModel<FilesViewModel> {
                    parametersOf(key)
                }
                Files(viewState = viewModel) })
            Route.IntentViewer -> TODO()
            Route.Onboarding -> NavEntry(key, content = {
                val viewModel = koinViewModel<SettingsViewModel> {
                    parametersOf(key)
                }
                Onboarding() })
            Route.ScreenLock -> NavEntry(key, content = { Login() })
            Route.Settings -> TODO()
            is Route.Viewer -> TODO()
        }
    }


    private val content = @Composable {
        // Check if light theme is preferred
        val isDark = run {
            val mode by preference(key = G.keys.night_mode)
            when (mode) {
                NightMode.YES -> true
                NightMode.NO -> false
                NightMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            }
        }
        val accent = run {
            val enabled by preference(G.keys.dynamic_colors)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && enabled -> dynamicAccentColor(
                    this,
                    isDark
                )

                isDark -> G.DarkAccentColor
                else -> G.LightAccentColor
            }
        }
        val isNavBarRequired = false
        val clazz = calculateWindowSizeClass(activity = this)
        // Determine the screen orientation.
        // This check assesses whether to display NavRail or BottomBar.
        // BottomBar appears only if the window size suits a mobile screen.
        // Consider this scenario: a large screen that fits the mobile description, like a desktop screen in portrait mode.
        // In this case, maybe showing the BottomBar is preferable!
        val portrait = clazz.width < Category.Medium
        //val surface = rememberAcrylicSurface()
        val content = @Composable {
            NavigationSuiteScaffold(
                vertical = portrait,
                containerColor = AppTheme.colors.background,
                progress = /*activity.inAppUpdateProgress*/ -1f,
                snackbarHostState = controller,
                hideNavigationBar = !isNavBarRequired,
                content = {
                    // Load start destination based on if storage permission is set or not.

                    val motion = AppTheme.motionScheme
                    NavDisplay(
                        backStack = navigator.backstack,
                        onBack = navigator::navigateUp,
                        // In order to add the `ViewModelStoreNavEntryDecorator` (see comment below for why)
                        // we also need to add the default `NavEntryDecorator`s as well. These provide
                        // extra information to the entry's content to enable it to display correctly
                        // and save its state.
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = navEntryProvider
                    )
                },
                navBar = { Spacer(Modifier) }
            )
        }
        AppTheme(
            isLight = !isDark,
            fontFamily = G.DefaultFontFamily,
            motionScheme = MotionScheme.expressive(),
            accent = accent,
            content = {
                // Provide the navController, newWindowClass through LocalComposition.
                CompositionLocalProvider(
                    LocalNavigator provides navigator,
                    LocalSystemFacade provides this@MainActivity,
                    LocalWindowSize provides when {
                        !isNavBarRequired -> clazz
                        portrait -> clazz.consume(56.dp)
                        else -> clazz.consume(100.dp)
                    },
                    content = content
                )
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // check if this is fresh launch
        val isFreshLaunch = savedInstanceState == null
        if (isFreshLaunch) {
            navigator = when {
                intent.action == Intent.ACTION_VIEW -> Navigator(Route.IntentViewer)
                !checkSelfPermissions(G.REQUIRED_PERMISSIONS) -> Navigator(Route.Onboarding)
                isAuthenticationRequired -> Navigator(Route.ScreenLock)
                else -> Navigator(Route.Files(""))
            }
        }

        // Set up the window to fit the system windows
        // This setting is usually configured in the app theme, but is ensured here
        WindowCompat.setDecorFitsSystemWindows(window, false)
        //
        setContent(content = content)
    }
}