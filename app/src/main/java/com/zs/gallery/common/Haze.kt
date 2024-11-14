/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 13-11-2024.
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

package com.zs.gallery.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.zs.foundation.AppTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

private const val TAG = "Haze"

/**
 * Alias for [HazeState], representing a backdrop provider.
 */
typealias BackdropProvider = HazeState

@Composable
@NonRestartableComposable
fun rememberBackdropProvider(): BackdropProvider =
    remember(::HazeState)

@ReadOnlyComposable
@Composable
private inline fun AppleHaze(
    containerColor: Color = AppTheme.colors.background,
    isDark: Boolean = containerColor.luminance() < 0.5f,
    lightBackgroundColor: Color,
    lightForegroundColor: Color,
    darkBackgroundColor: Color,
    darkForegroundColor: Color,
): HazeStyle = HazeStyle(
    blurRadius = 24.dp,
    backgroundColor = AppTheme.colors.background,
    noiseFactor = 0.3f,
    tints = listOf(
        HazeTint(
            color = if (isDark) darkBackgroundColor else lightBackgroundColor,
            blendMode = if (isDark) BlendMode.Overlay else BlendMode.ColorDodge,
        ),
        HazeTint(color = if (isDark) darkForegroundColor else lightForegroundColor),
    ),
)

/**
 * A [HazeStyle] which implements a somewhat opaque material. More opaque than [thin],
 * more translucent than [thick].
 */
@Composable
@ReadOnlyComposable
fun HazeStyle.Companion.regular(
    containerColor: Color = AppTheme.colors.background,
): HazeStyle = AppleHaze(
    containerColor = containerColor,
    lightBackgroundColor = Color(0xFF383838),
    lightForegroundColor = Color(color = 0xB3B3B3, alpha = 0.82f),
    darkBackgroundColor = Color(0xFF8C8C8C),
    darkForegroundColor = Color(color = 0x252525, alpha = 0.82f),
)

private fun Color(color: Int, alpha: Float): Color = Color(color).copy(alpha = alpha)

private fun hazeMaterial(
    containerColor: Color,
    lightAlpha: Float,
    darkAlpha: Float,
): HazeStyle = HazeStyle(
    blurRadius = 24.dp,
    backgroundColor = containerColor,
    noiseFactor = 0.4f,
    tint = HazeTint(
        containerColor.copy(alpha = if (containerColor.luminance() >= 0.5) lightAlpha else darkAlpha),
    ),
)

/**
 * A [HazeStyle] which implements a somewhat opaque material. More opaque than [thin],
 * more translucent than [thick].
 */
@Composable
@ReadOnlyComposable
fun HazeStyle.Companion.hazeRegular(
    containerColor: Color = AppTheme.colors.background,
): HazeStyle = hazeMaterial(
    containerColor = containerColor,
    lightAlpha = 0.63f,
    darkAlpha = 0.7f,
)