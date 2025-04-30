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

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

private const val TAG = "BlurBackground"

/**
 * Creates and [remember] s the instance of [HazeState]
 */
@Composable
@NonRestartableComposable
fun rememberBackgroundProvider() = remember(::HazeState)

private val PROGRESSIVE_MASK = Brush.verticalGradient(
    0.5f to Color.Black,
    0.8f to Color.Black.copy(0.5f),
    1.0f to Color.Transparent,
)

fun Modifier.observe(provider: HazeState) = hazeSource(state = provider)

/**
 * Applies a fancy blur effect to the composable, simulating a frosted glass/blur effect.
 *
 * This function uses the [hazeEffect] to create a blur and tinted background. It provides
 * a configurable way to achieve a frosted glass look, with parameters for controlling
 * blur radius, noise, tint, blend mode, and progressive effect. It also supports a progressive blur effect on Android 12 and up.
 *
 * @param containerColor The base color of the background. The function uses this color to
 *                       calculate default values for other parameters.
 * @param provider The [HazeState] to be used to manage the haze effect. This should be a remembered state.
 * @param blurRadius The radius of the blur effect. Default values are based on the luminance of the
 *                   [containerColor]: 38.dp for light colors (luminance >= 0.5) and 50.dp for dark
 *                   colors.
 * @param noiseFactor The amount of noise to add to the effect. Default values are based on the
 *                    luminance of the [containerColor]: 0.4f for light colors and 0.25f for dark
 *                    colors. **Note:** This parameter is ignored on Android versions below 12 (API level 31).
 * @param tint The color used to tint the blurred area. The default tint is a semi-transparent
 *             version of the [containerColor]. The alpha value of the tint is dependent on the luminance of the container color.
 *             If the luminance is greater than or equal to 0.5 it will use 0.45f as alpha, otherwise it will use 0.56f.
 * @param blendMode The [BlendMode] to use when blending the tint with the blurred content. Defaults to [BlendMode.SrcOver].
 * @param progressive The vertical progressive intensity of the blur like gradient.
 *                    - `-1f`: Disables the progressive effect. (Default)
 *                    - `0f`: Applies maximum blur at the bottom, fading to no blur at the top.
 *                    - `1f`: Applies maximum blur at the top, fading to no blur at the bottom.
 *                    - values between `0f` and `1f` create a gradient effect accordingly.
 */
fun Modifier.background(
    provider: HazeState,
    containerColor: Color,
    blurRadius: Dp = if (containerColor.luminance() >= 0.5f) 38.dp else 50.dp,
    noiseFactor: Float = if (containerColor.luminance() >= 0.5f) 0.4f else 0.25f,
    tint: Color = containerColor.copy(alpha = if (containerColor.luminance() >= 0.5) 0.45f else 0.56f),
    blendMode: BlendMode = BlendMode.SrcOver,
    progressive: Float = -1f,
) = hazeEffect(state = provider) {
    this.blurEnabled = true
    this.blurRadius = blurRadius
    this.backgroundColor = containerColor
    // Noise factor is disabled for now for below android 12.
    this.noiseFactor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) noiseFactor else 0f
    this.tints = listOf(HazeTint(tint, blendMode = blendMode))
    // For now progressive only works in android 12 and up. -1 means disbaled 0f means bottom to top 1f means top to bottom
    if (progressive != -1f) {
        this.progressive = HazeProgressive.verticalGradient(
            startIntensity = progressive,
            endIntensity = 0f,
            endY = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) 40f else Float.POSITIVE_INFINITY,
            preferPerformance = true
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            inputScale = HazeInputScale.Fixed(1.0f)
        mask = PROGRESSIVE_MASK
    }
}

