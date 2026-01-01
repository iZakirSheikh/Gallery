/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 30 of Dec 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 30 of Dec 2025
 *
 */

package com.zs.gallery

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ButtonDefaults
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.MotionScheme
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.NavigationSuiteScaffold
import com.zs.compose.theme.appbar.NavigationItemDefaults
import com.zs.compose.theme.calculateWindowSizeClass
import com.zs.compose.theme.dynamicAccentColor
import com.zs.compose.theme.renderInSharedTransitionScopeOverlay
import com.zs.gallery.albums.Albums
import com.zs.gallery.common.LocalNavigator
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.Navigator
import com.zs.gallery.common.NightMode.FOLLOW_SYSTEM
import com.zs.gallery.common.NightMode.NO
import com.zs.gallery.common.NightMode.YES
import com.zs.gallery.common.Route
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.compose.NavigationBar
import com.zs.gallery.common.compose.NavigationType
import com.zs.gallery.common.impl.AlbumsViewModel
import com.zs.gallery.common.impl.FilesViewModel
import com.zs.gallery.common.preference
import com.zs.gallery.files.Files
import com.zs.gallery.intro.Onboarding
import com.zs.gallery.login.Login
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.zs.compose.theme.snackbar.SnackbarHostState as SnackbarController
import com.zs.gallery.common.Gallery as G

private const val TAG = "MainContent"

private val BOTTOM_NAV_HEIGHT = 56.dp
private val NAV_RAIL_WIDTH = 100.dp

//Use this build your nav-graph
private val navEntryProvider: (Route) -> NavEntry<Route> = { key: Route ->
    when (key) {
        is Route.Onboarding -> NavEntry(key) { Onboarding() }
        is Route.ScreenLock -> NavEntry(key) { Login() }
        is Route.Files, Route.Timeline -> NavEntry(key) {
            val viewModel = koinViewModel<FilesViewModel> {
                if (key != Route.Timeline)
                    parametersOf(key)
                else
                    parametersOf()
            }
            Files(viewModel)
        }

        is Route.Albums -> NavEntry(key = key) {
            val viewState = koinViewModel<AlbumsViewModel> {
                parametersOf(key)
            }
            Albums(viewState)
        }

        else -> TODO("Not Implemented yet!")
    }
}

/**
 * Represents the entry into the [Gallery] UI.
 */
@Composable
@NonRestartableComposable
context(activity: MainActivity)
fun MainContent(
    navController: Navigator<Route>,
    controller: SnackbarController
) {
    // dependencies
    val clazz = calculateWindowSizeClass(activity = activity)
    val entry = navController.active
    //
    val x = ButtonDefaults.IconSpacing
    val isDarkTheme = run {
        val mode by preference(key = G.keys.night_mode)
        when (mode) {
            YES -> true
            NO -> false
            FOLLOW_SYSTEM -> isSystemInDarkTheme()
        }
    }
    // calculate accent color
    val accent = run {
        val enabled by preference(G.keys.dynamic_colors)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && enabled ->
                dynamicAccentColor(activity, isDarkTheme)

            isDarkTheme -> G.DarkAccentColor
            else -> G.LightAccentColor
        }
    }
    val portrait = clazz.width < Category.Medium
    val facade = activity as SystemFacade

    val isNavBarRequired =
        entry is Route.Timeline || entry is Route.Folders || entry is Route.Albums
    val content = @Composable {
        // Get the current theme colors
        val colors = AppTheme.colors
        val navBar = @Composable {
            NavigationBar(
                if (portrait) NavigationType.BOTTOM_NAV else NavigationType.NAV_RAIL,
                floating = false,
                modifier = Modifier.renderInSharedTransitionScopeOverlay(0.3f),
                content = { type ->
                    val contentColor = AppTheme.colors.onAccent
                    val colors = NavigationItemDefaults.colors(
                        selectedIndicatorColor = if (contentColor == colors.onAccent) contentColor.copy(
                            ContentAlpha.indication
                        ) else colors.accent,
                        selectedTextColor = if (contentColor == colors.onAccent) contentColor else colors.accent,
                        unselectedIconColor = if (contentColor == colors.onAccent) colors.onAccent else colors.onBackground,
                        unselectedTextColor = if (contentColor == colors.onAccent) colors.onAccent else colors.onBackground
                    )

                    // Timeline
                    com.zs.gallery.common.compose.NavigationItem(
                        label = textResource(R.string.timeline),
                        icon = R.drawable.ic_twotone_photo_library,
                        checked = entry is Route.Timeline,
                        onClick = { navController.navigate(Route.Timeline) },
                        type = type,
                        colors = colors
                    )

                    // Albums
                    com.zs.gallery.common.compose.NavigationItem(
                        label = textResource(R.string.albums),
                        icon = R.drawable.ic_outline_collections,
                        checked = entry is Route.Albums,
                        onClick = { navController.navigate(Route.Albums) },
                        type = type,
                        colors = colors
                    )

                    // Folders
                    com.zs.gallery.common.compose.NavigationItem(
                        label = textResource(R.string.folders),
                        icon = R.drawable.ic_outline_folder_copy,
                        checked = entry is Route.Folders,
                        onClick = { navController.navigate(Route.Folders) },
                        type = type,
                        colors = colors
                    )
                }
            )
        }

        NavigationSuiteScaffold(
            vertical = portrait,
            containerColor = AppTheme.colors.background,
            progress = activity.inAppUpdateProgress,
            snackbarHostState = controller,
            hideNavigationBar = !isNavBarRequired,
            content = {
                NavDisplay(
                    backStack = navController.backstack,
                    onBack = navController::navigateUp,
                    // These provide extra information to the entry's content to enable it to
                    // display correctly and save its state.
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = navEntryProvider
                )
            },
            navBar = navBar
        )
    }


    //
    AppTheme(
        isLight = !isDarkTheme,
        fontFamily = G.DefaultFontFamily,
        motionScheme = MotionScheme.expressive(),
        accent = accent,
        content = {
            // Provide the navController, newWindowClass through LocalComposition.
            CompositionLocalProvider(
                LocalNavigator provides navController,
                LocalSystemFacade provides activity,
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