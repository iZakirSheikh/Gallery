/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 13-05-2025.
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

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Background
import com.zs.compose.theme.Colors
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient as Gradient

// Reusable mask
private val PROGRESSIVE_MASK = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
    Gradient(listOf(Color.Black, Color.Black, Color.Transparent))
else
    Gradient(0.5f to Color.Black, 0.8f to Color.Black.copy(0.5f), 1.0f to Color.Transparent)

/**
 * Applies a hazy effect to the background based on the provided [HazeState].
 *
 * This function creates a blurred background with optional noise and tint effects. It provides customization options
 * for blur radius, noise factor, tint color, blend mode, and progressive blurring.
 *
 * @param provider The [HazeState] instance that manages the haze effect.
 * @param containerColor The background color of the container. Defaults to [Colors.background].
 * @param blurRadius The radius of the blur effect. Defaults to 38.dp for light backgrounds (luminance >= 0.5) and 60.dp for dark backgrounds.
 * @param noiseFactor The factor for the noise effect. Defaults to 0.4f for light backgrounds and 0.28f for dark backgrounds. Noise effect is disabled on Android versions below 12.
 * @param tint The color to tint the blurred background with. Defaults to a semi-transparent version of [containerColor].
 * @param blendMode The blend mode to use for the tint. Defaults to [BlendMode.SrcOver].
 * @param luminance controls the luminosity of the blured layer defaults to 0.07f. -1 disables it.
 * @param progressive A float value to control progressive blurring:
 *   - -1f: Progressive blurring is disabled.
 *   - 0f: Bottom to top gradient.
 *   - 1f: Top to bottom gradient.
 *   - Values between 0f and 1f: Intermediate gradient positions.
 *   Progressive blurring is only available on Android 12 and above.
 * @return A [Background] composable with the specified haze effect.
 */
@SuppressLint("ModifierFactoryExtensionFunction")
@OptIn(ExperimentalHazeApi::class)
fun Colors.background(
    surface: HazeState,
    containerColor: Color = background(0.4.dp),
    blurRadius: Dp = if (containerColor.luminance() >= 0.5f) 38.dp else 80.dp,
    noiseFactor: Float = if (containerColor.luminance() >= 0.5f) 0.5f else 0.25f,
    tint: Color = containerColor.copy(alpha = if (containerColor.luminance() >= 0.5) 0.63f else 0.65f),
    luminance: Float = 0.07f,
    blendMode: BlendMode = BlendMode.SrcOver,
    progressive: Float = -1f,
) = Background(
    Modifier.hazeEffect(state = surface) {
        this.blurEnabled = true
        this.blurRadius =  blurRadius
        this.backgroundColor = containerColor
        // Disable noise factor on Android versions below 12.
        this.noiseFactor = noiseFactor
        this.tints = buildList {
            // apply luminosity just like in Microsoft Acrylic.
            if (luminance != -1f)
                this += HazeTint(Color.White.copy(0.07f), BlendMode.Luminosity)
            this += HazeTint(tint, blendMode = blendMode)
        }
        // Configure progressive blurring (if enabled).
        if (progressive != -1f) {
            this.progressive = HazeProgressive.verticalGradient(
                startIntensity = progressive,
                endIntensity = 0f,
                preferPerformance = true
            )
            mask = PROGRESSIVE_MASK
            // Adjust input scale for Android versions below 12 for better visuals.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                inputScale = HazeInputScale.Fixed(0.5f)
        }
    }
)

/** Creates and [remember] s the instance of [HazeState] */
@Composable
@NonRestartableComposable
fun rememberAcrylicSurface() = remember(::HazeState)

/**
 * Marks the content to be a source of blurred content for the provided [surface].
 *
 * This function registers the content it is applied to as a source that contributes to the blurring
 * effect managed by the given [surface]. Any views or composables marked with [background] using
 * the same [surface] will blur this source content.
 *
 * @param surface The [HazeState] instance that this content will contribute to.
 * @return A [Modifier] that marks the content as a blur source.
 */
fun Modifier.source(surface: HazeState) = hazeSource(state = surface)

/**
 * Adds a subtle shine effect to components, particularly [Acrylic] ones,
 * mimicking the gleaming edge of glass.
 *
 * This property defines a [BorderStroke] that uses a vertical gradient.
 * The gradient's colors are determined by whether the current theme is light or dark.
 * - In a light theme, it transitions from the `background` color to a slightly darker,
 *   semi-transparent version of the `background`.
 * - In a dark theme, it transitions from a semi-transparent gray to an even more
 *   transparent gray.
 *
 * This creates a visual highlight, suggesting a light source reflecting off the edge
 * of the component.
 */
val Colors.shine
    get() = BorderStroke(
        0.5.dp,
        Gradient(
            listOf(
                if (isLight) background else Color.Gray.copy(0.24f),
                if (isLight) background.copy(0.3f) else Color.Gray.copy(0.075f),
            )
        )
    )