/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 08-05-2025.
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

package com.zs.gallery.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.appbar.TopAppBar
import com.zs.compose.theme.text.Text
import com.zs.gallery.common.compose.background
import dev.chrisbanes.haze.HazeState

@Composable
@NonRestartableComposable
fun MediaViewerTopAppBar(
    visible: Boolean,
    title: CharSequence,
    provider: HazeState,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
) =  AnimatedVisibility(
    visible = visible,
    enter = slideInVertically() + fadeIn(),
    exit = slideOutVertically() + fadeOut(),
    content = {
        TopAppBar(
            navigationIcon = navigationIcon,
            title = {
                Text(
                    title,
                    maxLines = 2,
                    fontFamily = FontFamily.Monospace,
                    style = AppTheme.typography.title3
                )
            },
            actions = actions,
            background = AppTheme.colors.background(
                provider,
                Color.Transparent,
                progressive = 1f,
                blurRadius = 100.dp,
                noiseFactor = 0.18f,
                luminance = -1f,
                tint = Color.Black.copy(0.3f),
                blendMode = BlendMode.Multiply
            ),
            contentColor = Color.SignalWhite,
            elevation = 0.dp,
        )
    },
    modifier = modifier
)