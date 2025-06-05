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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.appbar.TopAppBar
import com.zs.compose.theme.text.Text
import dev.chrisbanes.haze.HazeState

private val Veil = Background(
    brush = Brush.verticalGradient(
        listOf(
            Color.Black,
            Color.Black.copy(0.5f),
            Color.Black.copy(0.0f)
        )
    )
)

//            background = AppTheme.colors.background(
//                provider,
//                Color.Transparent,
//                progressive = 1f,
//                blurRadius =  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) 25.dp else 100.dp,
//                noiseFactor = 0.22f,
//                luminance = -1f,
//                tint = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) Color.Black.copy(0.3f) else Color.Transparent,
//                blendMode = BlendMode.Overlay
//            ),

@Composable
@NonRestartableComposable
fun MediaViewerTopAppBar(
    visible: Boolean,
    title: CharSequence,
    provider: HazeState,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
) = AnimatedVisibility(
    visible = visible,
    enter = slideInVertically(animationSpec = AppTheme.motionScheme.defaultSpatialSpec()) + fadeIn(
        AppTheme.motionScheme.fastEffectsSpec()
    ),
    exit = slideOutVertically(animationSpec = AppTheme.motionScheme.defaultSpatialSpec()) + fadeOut(
        AppTheme.motionScheme.fastEffectsSpec()
    ),
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
            background = Veil,
            contentColor = Color.SignalWhite,
            elevation = 0.dp,
        )
    },
    modifier = modifier
)