/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 31 of Dec 2025
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
 * Last Modified by sheik on 31 of Dec 2025
 *
 */

package com.zs.gallery.common.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Background
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Icon
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.appbar.BottomAppBar
import com.zs.compose.theme.appbar.BottomNavigationItem
import com.zs.compose.theme.appbar.FloatingBottomNavigationBar
import com.zs.compose.theme.appbar.NavigationItemColors
import com.zs.compose.theme.appbar.NavigationItemDefaults
import com.zs.compose.theme.appbar.SideBar
import com.zs.compose.theme.appbar.SideNavigationItem
import com.zs.compose.theme.text.Label
import com.zs.gallery.common.shapes.EndConcaveShape
import com.zs.gallery.common.shapes.TopConcaveShape
import com.zs.gallery.common.vectorResource

enum class NavigationType { BOTTOM_NAV, NAV_RAIL, NAV_DRAWER }


private val NAV_RAIL_SHAPE = EndConcaveShape(12.dp)
private val BOTTOM_NAV_SHAPE = TopConcaveShape(12.dp)
private val FLOATING_BOTTOM_NAV_SHAPE = CircleShape

private val NAV_RAIL_BORDER = BorderStroke(
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

@Composable
@Suppress("PrivatePropertyName")
internal inline fun NavigationBar(
    type: NavigationType,
    floating: Boolean,
    modifier: Modifier = Modifier,
    background: Background = Background(AppTheme.colors.accent),
    contentColor: Color = AppTheme.colors.onAccent,
    crossinline content: @Composable (type: NavigationType) -> Unit
) {
    when (type) {
        NavigationType.BOTTOM_NAV if (floating) -> FloatingBottomNavigationBar(
            contentColor = contentColor,
            background = background,
            elevation = 12.dp,
            //border = colors.shine,
            windowInsets = AppBarDefaults.bottomAppBarWindowInsets.union(WindowInsets(bottom = 16.dp))
                .add(WindowInsets(bottom = 10.dp)),
            shape = FLOATING_BOTTOM_NAV_SHAPE,
            modifier = modifier,
            // Display routes at the contre of available space
            content = { content(type) }
        )

        NavigationType.BOTTOM_NAV -> BottomAppBar(
            contentColor = contentColor,
            background = background,
            elevation = 0.dp,
            shape = BOTTOM_NAV_SHAPE,
            windowInsets = AppBarDefaults.bottomAppBarWindowInsets,
            modifier = modifier,
            content = { content(type) }
        )

        NavigationType.NAV_RAIL if (floating) -> TODO("Not Implemented yet!")
        NavigationType.NAV_RAIL -> SideBar(
            modifier = modifier.width(100.dp),
            windowInsets = AppBarDefaults.sideBarWindowInsets,
            contentColor = contentColor,
            border = NAV_RAIL_BORDER,
            shape = NAV_RAIL_SHAPE,
            background = background,
            elevation = 0.dp,
            content = { content(type) },
        )

        else -> TODO("Not Implemented yet!")
    }
}

@Composable
@NonRestartableComposable
fun NavigationItem(
    @DrawableRes icon: Int,
    label: CharSequence = "",
    type: NavigationType,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onClick: () -> Unit,
    colors: NavigationItemColors = NavigationItemDefaults.colors(),
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) = when (type) {
    NavigationType.BOTTOM_NAV -> BottomNavigationItem(
        selected = checked,
        onClick = onClick,
        icon = { Icon(vectorResource(icon), label.toString()) },
        label = { Label(label) },
        modifier = modifier,
        colors = colors,
        enabled = enabled,
        interactionSource = interactionSource
    )

    NavigationType.NAV_RAIL -> SideNavigationItem(
        selected = checked,
        onClick = onClick,
        icon = { Icon(vectorResource(icon), label.toString()) },
        label = { Label(label) },
        modifier = modifier,
        colors = colors,
        enabled = enabled,
        interactionSource = interactionSource
    )

    else -> TODO("Not Implemented yet!")
}