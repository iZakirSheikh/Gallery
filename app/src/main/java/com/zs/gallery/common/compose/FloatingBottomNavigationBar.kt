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

package com.zs.gallery.common.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.None
import com.zs.compose.theme.Surface
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.appbar.BottomAppBar

private val SideBarMinWidth = 80.dp
private val BottomAppBarMinHeight = SideBarMinWidth

// TODO: this should probably be part of the touch target of the start and end icons, clarify this
private val AppBarHorizontalPadding = 4.dp
private val SideBarVerticalPadding = AppBarHorizontalPadding

/// The space between conservative items of Bottom app bar
private val BottomBarItemHorizontalPadding: Dp = 8.dp

@Composable
fun FloatingBottomNavigationBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AppBarDefaults.bottomAppBarWindowInsets,
    backgroundColor: Color = AppTheme.colors.background(1.dp),
    contentColor: Color = AppTheme.colors.onBackground,
    shape: Shape = RectangleShape,
    border: BorderStroke? = null,
    elevation: Dp = AppBarDefaults.BottomAppBarElevation,
    contentPadding: PaddingValues = AppBarDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        color = backgroundColor,
        contentColor = contentColor.copy(ContentAlpha.medium),
        elevation = 0.dp,
        shape = RectangleShape,
        border = null,
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .thenIf(windowInsets !== WindowInsets.None) { windowInsetsPadding(windowInsets) }
            .thenIf(border != null) { border(border!!, shape) }
            .shadow(elevation, shape, true)
            .then(modifier)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .defaultMinSize(minHeight = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(
                BottomBarItemHorizontalPadding,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}