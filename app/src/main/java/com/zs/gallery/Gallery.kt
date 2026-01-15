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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.MotionScheme
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.NavigationSuiteScaffold
import com.zs.compose.theme.appbar.NavigationItemDefaults
import com.zs.compose.theme.calculateWindowSizeClass
import com.zs.compose.theme.dynamicAccentColor
import com.zs.compose.theme.renderInSharedTransitionScopeOverlay
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.NavController
import com.zs.gallery.common.NavKey
import com.zs.gallery.common.NightMode
import com.zs.gallery.common.NightMode.FOLLOW_SYSTEM
import com.zs.gallery.common.Res
import com.zs.gallery.common.compose.NavigationBar
import com.zs.gallery.common.compose.NavigationType
import com.zs.gallery.common.impl.FilesViewModel
import com.zs.gallery.common.preference
import com.zs.gallery.files.Files
import com.zs.gallery.intro.Intro
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.zs.compose.theme.snackbar.SnackbarHostState as SnackbarController

private const val TAG = "Gallery"
private typealias NavGraphBuilder = (NavKey) -> NavEntry<NavKey>

// Use this build your nav-graph
// Define the navigation graph builder.
// This lambda maps each NavKey to its corresponding NavEntry (Composable screen).
private val navGraph: NavGraphBuilder = { key: NavKey ->
    when (key) {
        // --- App Intro Screen ---
        // If the route is AppIntro, show the AppIntro composable.
        NavKey.AppIntro -> NavEntry(key) { Intro() }
        // --- Files Screens ---
        is NavKey.Files -> NavEntry(key) {
            val viewModel = koinViewModel<FilesViewModel> { parametersOf(key) }
            Files(viewModel) // Render the Files screen with the resolved ViewModel.
        }
        // --- Fallback ---
        // For any unhandled route, throw a TODO to indicate missing implementation.
        else -> TODO("${key.domain} - Not Implemented yet!")
    }
}

@Composable
@NonRestartableComposable
context(activity: MainActivity)
fun Gallery(
    navController: NavController,
    controller: SnackbarController
) {
    // Environment & State Setup
    val clazz = calculateWindowSizeClass(activity = activity) // Screen size classification
    val entry = navController.active                          // Current navigation entry
    val portrait = clazz.width < Category.Medium              // Orientation check

    // Resolve dark/light theme
    val isDarkTheme = run {
        val mode by preference(key = Res.key.night_mode_policy)
        when (mode) {
            NightMode.ENABLED -> true
            NightMode.DISABLED -> false
            FOLLOW_SYSTEM -> isSystemInDarkTheme()
        }
    }

    // Resolve accent color (dynamic if supported, fallback otherwise)
    val accent = run {
        val enabled by preference(Res.key.dynamic_colors)
        when {
            Res.manifest.isAtLeast(Build.VERSION_CODES.S) && enabled -> dynamicAccentColor(
                activity,
                isDarkTheme
            )

            isDarkTheme -> Res.manifest.color_accent_dark
            else -> Res.manifest.color_accent_light
        }
    }

    // Determine Navigation Bar Visibility
    val isNavBarRequired =
        entry is NavKey.Files && entry.isTimeline || entry is NavKey.Folders || entry is NavKey.Albums
    // Navigation Bar Definition
    val navBar: @Composable () -> Unit = {
        NavigationBar(
            if (portrait) NavigationType.BOTTOM_NAV else NavigationType.NAV_RAIL,
            floating = false,
            modifier = Modifier.renderInSharedTransitionScopeOverlay(0.3f),
            content = { type ->
                val contentColor = AppTheme.colors.onAccent
                val colors = NavigationItemDefaults.colors(
                    selectedIndicatorColor =
                        if (contentColor == AppTheme.colors.onAccent)
                            contentColor.copy(ContentAlpha.indication)
                        else AppTheme.colors.accent,
                    selectedTextColor =
                        if (contentColor == AppTheme.colors.onAccent)
                            contentColor
                        else AppTheme.colors.accent,
                    unselectedIconColor =
                        if (contentColor == AppTheme.colors.onAccent)
                            AppTheme.colors.onAccent
                        else AppTheme.colors.onBackground,
                    unselectedTextColor =
                        if (contentColor == AppTheme.colors.onAccent)
                            AppTheme.colors.onAccent
                        else AppTheme.colors.onBackground
                )

                // Timeline
                com.zs.gallery.common.compose.NavigationItem(
                    label = textResource(Res.string.timeline),
                    icon = Res.drawable.ic_photo_library_two_tone,
                    checked = entry is NavKey.Files && entry.isTimeline,
                    onClick = { navController.navigate(NavKey.Files()) },
                    type = type,
                    colors = colors
                )

                // Albums
                com.zs.gallery.common.compose.NavigationItem(
                    label = textResource(Res.string.albums),
                    icon = Res.drawable.ic_outline_collections,
                    checked = entry is NavKey.Albums,
                    onClick = { navController.navigate(NavKey.Albums) },
                    type = type,
                    colors = colors
                )

                // Folders
                com.zs.gallery.common.compose.NavigationItem(
                    label = textResource(Res.string.folders),
                    icon = Res.drawable.ic_outline_folder_copy,
                    checked = entry is NavKey.Folders,
                    onClick = { navController.navigate(NavKey.Folders) },
                    type = type,
                    colors = colors
                )
            }
        )
    }
    // Main Content Scaffold
    val content: @Composable () -> Unit = {
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
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = navGraph
                )
            },
            navBar = navBar
        )
    }
    // Apply Theme + Composition Locals
    AppTheme(
        isLight = !isDarkTheme,
        fontFamily = FontFamily.Default,
        motionScheme = MotionScheme.expressive(),
        accent = accent,
        content = {
            CompositionLocalProvider(
                LocalNavController provides navController,
                LocalSystemFacade provides activity,
                LocalWindowSize provides when {
                    !isNavBarRequired -> clazz
                    portrait -> clazz.consume(56.dp) // Adjust for bottom nav
                    else -> clazz.consume(100.dp)   // Adjust for nav rail
                },
                content = content
            )
        }
    )
}
