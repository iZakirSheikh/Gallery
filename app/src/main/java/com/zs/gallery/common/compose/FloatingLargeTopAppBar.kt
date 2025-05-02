/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 05-04-2025.
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

package com.zs.gallery.common.compose

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Colors
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.None
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.appbar.CollapsableTopBarLayout
import com.zs.compose.theme.appbar.TopAppBarScope
import com.zs.compose.theme.appbar.TopAppBarScrollBehavior
import com.zs.compose.theme.appbar.TopAppBarStyle
import com.zs.compose.theme.text.ProvideTextStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle

@Stable
fun TopAppBarStyle.contentColor(fraction: Float): Color {
    return androidx.compose.ui.graphics.lerp(
        contentColor,
        scrolledContentColor,
        FastOutLinearInEasing.transform(fraction)
    )
}

@Stable
fun TopAppBarStyle.titleTextStyle(fraction: Float): TextStyle {
    return androidx.compose.ui.text.lerp(
        titleTextStyle,
        scrolledTitleTextStyle,
        FastOutLinearInEasing.transform(fraction)
    )
}

@Stable
fun TopAppBarStyle.containerColor(fraction: Float): Color {
    return androidx.compose.ui.graphics.lerp(
        containerColor,
        scrolledContainerColor,
        FastOutLinearInEasing.transform(fraction)
    )
}

/**
 * A floating large top app bar that can collapse and expand based on scroll behavior.
 *
 * @param title The title to be displayed in the app bar. This should be a composable that
 *   renders the title text or other title-related content.
 * @param navigationIcon The navigation icon to be displayed in the app bar.
 * @param actions The actions to be displayed in the app bar.
 * @param backdrop The backdrop to be displayed behind the app bar.
 * @param modifier Optional modifier for the app bar.
 */
@Composable
fun FloatingLargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable() () -> Unit = {},
    actions: @Composable() RowScope.() -> Unit = {},
    windowInsets: WindowInsets = AppBarDefaults.topAppBarWindowInsets,
    style: TopAppBarStyle = AppBarDefaults.largeAppBarStyle(
        scrolledContainerColor = AppTheme.colors.accent,
        scrolledContentColor = AppTheme.colors.onAccent,
        scrolledTitleTextStyle = AppTheme.typography.display3,
        titleTextStyle = AppTheme.typography.title3
    ),
    behaviour: TopAppBarScrollBehavior? = null,
    backdrop: @Composable() TopAppBarScope.() -> Unit = {
        Spacer(
            modifier = Modifier
                .shadow(lerp(12.dp, 0.dp, fraction / .05f), FloatingTopBarShape)
                .thenIf(!AppTheme.colors.isLight && fraction == 0f) {
                    border(
                        0.1.dp,
                        style.contentColor.copy(
                            com.zs.compose.foundation.lerp(
                                ContentAlpha.divider,
                                0f,
                                fraction
                            )
                        ),
                        FloatingTopBarShape
                    )
                }
                .background(style.containerColor(1 - fraction))
                .layoutId(AppBarDefaults.LayoutIdBackground)
                .fillMaxSize()
        )
    },
) {
    // Ensure that user has provided settings for large app bar.
    require(style.height < style.maxHeight) {
        "LargeTopAppBar maxHeight (${style.maxHeight}) must be greater than height (${style.height})"
    }

    // TODO - Expose fraction through behaviour; instead of relying here on state.
    var hPadding by rememberSaveable { mutableFloatStateOf(0f) }
    CollapsableTopBarLayout(
        height = style.height,
        maxHeight = style.maxHeight,
        insets = WindowInsets.None,
        scrollBehavior = behaviour,
        // maybe instead of hPadding we can use scale.
        modifier = modifier
            .widthIn(max = 500.dp)
            .windowInsetsPadding(windowInsets)
            .padding(horizontal = hPadding.dp),
        content = {
            // update hPadding
            hPadding = androidx.compose.ui.util.lerp(30f, 0f, fraction)
            CompositionLocalProvider(LocalContentColor provides style.contentColor(1 - fraction)) {
                ProvideTextStyle(style.titleTextStyle(fraction)) {

                    //Backdrop
                    backdrop()
                    // Defines the navIcon and actions first;
                    // make sure that title is always last; because if it is not; a new list of
                    // measurables will be created; which will make sure it is at the last.
                    Box(
                        Modifier
                            .layoutId(AppBarDefaults.LayoutIdNavIcon)
                            .padding(start = 4.dp),
                        content = { navigationIcon() }
                    )

                    // Actions
                    Box(
                        Modifier
                            .layoutId(AppBarDefaults.LayoutIdAction)
                            .padding(end = 4.dp),
                        content = {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                                content = actions
                            )
                        }
                    )

                    Box(
                        Modifier
                            .layoutId(AppBarDefaults.LayoutIdCollapsable_title)
                            .padding(horizontal = 4.dp),
                        content = { title() }
                    )
                }
            }
        }
    )
}

private val Colors.border
    get() = BorderStroke(
        0.5.dp,
        Brush.verticalGradient(
            listOf(
                if (isLight) background else Color.Gray.copy(0.24f),
                if (isLight) background.copy(0.3f) else Color.Gray.copy(0.075f),
            )
        )
    )
private val FloatingTopBarShape = RoundedCornerShape(20)
/**
 * A floating large top app bar that can collapse and expand based on scroll behavior and takes
 * [HazeState] as backdrop.
 */
@Composable
@NonRestartableComposable
fun FloatingLargeTopAppBar(
    title: @Composable () -> Unit,
    backdrop: HazeState,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    behavior: TopAppBarScrollBehavior? = null,
    style: TopAppBarStyle = AppBarDefaults.largeAppBarStyle(
        scrolledTitleTextStyle = AppTheme.typography.display3,
        titleTextStyle = AppTheme.typography.title3
    ),
    insets: WindowInsets = AppBarDefaults.topAppBarWindowInsets,
) = FloatingLargeTopAppBar(
    title, modifier, navigationIcon, actions, insets, style, behavior,
    backdrop = {
        if (fraction > 0.1f) return@FloatingLargeTopAppBar Spacer(Modifier)
        val colors = AppTheme.colors
        Spacer(
            modifier = Modifier
                .shadow(lerp(100.dp, 0.dp, fraction / .05f), FloatingTopBarShape)
                .thenIf(fraction == 0f) {
                    border(AppTheme.colors.border, FloatingTopBarShape)
                }
                .background(backdrop, AppTheme.colors.background)
                .layoutId(AppBarDefaults.LayoutIdBackground)
                .fillMaxSize()
        )
    }
)
