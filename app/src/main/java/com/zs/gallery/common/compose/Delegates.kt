/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 03-04-2025.
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

@file:OptIn(ExperimentalThemeApi::class)

package com.zs.gallery.common.compose

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottiePainter
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.background
import com.zs.compose.foundation.composableIf
import com.zs.compose.foundation.effects.shimmer
import com.zs.compose.foundation.fullLineSpan
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Colors
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.Placeholder
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.appbar.BottomNavigationItem
import com.zs.compose.theme.appbar.FloatingLargeTopAppBar
import com.zs.compose.theme.appbar.LargeTopAppBar
import com.zs.compose.theme.appbar.NavigationItemColors
import com.zs.compose.theme.appbar.NavigationItemDefaults
import com.zs.compose.theme.appbar.SideNavigationItem
import com.zs.compose.theme.appbar.TopAppBar
import com.zs.compose.theme.appbar.TopAppBarScrollBehavior
import com.zs.compose.theme.appbar.TopAppBarStyle
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.Text
import com.zs.core.player.PlayerController
import com.zs.core.player.PlayerView
import com.zs.gallery.R
import com.zs.gallery.common.Mapped
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlin.math.roundToInt

private const val TAG = "Delegates"

/**
 * A composable function that delegates to [LottieAnimation] painter and behaves like [AndroidVectorDrawable].
 * This overload animates between the start and end frames of the animation based on the value of `atEnd`.
 *
 * @param id The resource identifier of the [LottieCompositionSpec.RawRes] type.
 * @param atEnd A boolean parameter that determines whether to display the end-frame or the start
 *              frame of the animation. The change in value causes animation.
 * @param duration The duration of the animation in milliseconds. The default value is -1, which
 *                 means the animation will use the duration specified in the
 *                 [LottieCompositionSpec] object. If a positive value is given, it will override
 *                 the duration from the [LottieCompositionSpec] object.
 * @param progressRange A range of float values that specifies the start and end frames of the
 *                      animation. The default range is 0f..1f, which means the animation will
 *                      start from the first frame and end at the last frame. Some [Lottie]
 *                      animation files may have different start/end frames, and this parameter
 *                      can be used to adjust them accordingly.
 * @param easing The easing function to use for the animation.
 */
@Composable
fun lottieAnimationPainter(
    @RawRes id: Int,
    atEnd: Boolean,
    duration: Int = -1,
    progressRange: ClosedFloatingPointRange<Float> = 0f..1f,
    easing: Easing = FastOutSlowInEasing,
): Painter {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(id))
    val duration2 = composition?.duration?.roundToInt() ?: AnimationConstants.DefaultDurationMillis
    val progress by animateFloatAsState(
        targetValue = if (atEnd) progressRange.start else progressRange.endInclusive,
        label = "Lottie $id",
        animationSpec = tween(if (duration == -1) duration2 else duration, easing = easing)
    )
    return rememberLottiePainter(
        composition = composition,
        progress = progress,
    )
}

/**
 * A composable function that delegates to [LottieAnimation] painter and behaves like [AndroidVectorDrawable].
 * This overload animates the Lottie composition according to the given parameters.
 *
 * @param id The resource identifier of the [LottieCompositionSpec.RawRes] type.
 * @param speed The speed at which the animation plays.
 * @param repeatMode The repeat mode for the animation.
 * @param iterations The number of iterations to play the animation.
 * @see lottieAnimationPainter
 */
@Composable
fun lottieAnimationPainter(
    @RawRes id: Int,
    speed: Float = 1f,
    repeatMode: RepeatMode = RepeatMode.Restart,
    iterations: Int = Int.MAX_VALUE,
): Painter {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(id))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed,
        reverseOnRepeat = repeatMode == RepeatMode.Reverse,
    )
    return rememberLottiePainter(
        composition = composition,
        progress = progress
    )
}

/** Composes placeholder with lottie icon. */
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Placeholder(
    title: String,
    modifier: Modifier = Modifier,
    vertical: Boolean = true,
    @RawRes iconResId: Int,
    message: CharSequence? = null,
    noinline action: @Composable (() -> Unit)? = null,
) {
    Placeholder(
        modifier = modifier, vertical = vertical,
        message = composableIf(message != null) {
            Text(
                text = message!!,
                color = AppTheme.colors.onBackground,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        },
        title = {
            Label(
                text = title.ifEmpty { " " },
                maxLines = 2,
                color = AppTheme.colors.onBackground
            )
        },
        icon = {
            Image(
                painter = lottieAnimationPainter(id = iconResId),
                contentDescription = null
            )
        },
        action = action,
    )
}

/**
 * Represents a navigation item that can be used in either a bottom navigation bar or a side navigation rail.
 *
 * This composable simplifies the creation of navigation items by abstracting the underlying implementation
 * (either [BottomNavigationItem] or [NavigationRailItem]). It determines which item type to use based on
 * the `typeRail` parameter.
 *
 * @param onClick The callback to be invoked when this item is clicked.
 * @param icon The icon content of this navigation item.
 * @param label The label content of this navigation item.
 * @param modifier The [Modifier] to be applied to this navigation item.
 * @param checked `true` if this item is selected, `false` otherwise.
 * @param typeRail `true` if this item should be rendered as a side navigation rail item,
 *                 `false` if it should be rendered as a bottom navigation item.
 */
@Composable
inline fun NavItem(
    noinline onClick: () -> Unit,
    noinline icon: @Composable () -> Unit,
    noinline label: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    typeRail: Boolean = false,
    colors: NavigationItemColors = NavigationItemDefaults.colors(),
) = when (typeRail) {
    true -> SideNavigationItem(checked, onClick, icon, label, modifier, colors = colors)
    else -> BottomNavigationItem(checked, onClick, icon, label, modifier, colors = colors)
}

/**
 * A simple utility fun for loading animation header index
 */
private fun isIndexHeader(index: Int) = when (index) {
    0, 2, 9, 18 -> true
    else -> false
}

private val ShimmerHeaderPadding =
    PaddingValues(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 50.dp)
private val ShimmerAnimSpec = infiniteRepeatable(
    tween<Float>(800, 0, easing = LinearEasing)
)

/**
 * @see emit
 */
fun <T> LazyGridScope.emit(
    data: Mapped<T>?,
): Mapped<T>? {
    when {
        // null means loading
        data == null -> items(
            20,
            contentType = { "loading_item" },
            span = {
                if (isIndexHeader(it))
                    GridItemSpan(maxLineSpan)
                else
                    GridItemSpan(1)
            },
            itemContent = {
                val isHeader = isIndexHeader(it)
                if (isHeader)
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        content = {
                            Spacer(
                                Modifier
                                    .padding(
                                        top = ContentPadding.large,
                                        bottom = ContentPadding.medium
                                    )
                                    .requiredSize(100.dp, 30.dp)
                                    .clip(CircleShape)
                                    .shimmer(
                                        Color.Gray.copy(0.5f),
                                        animationSpec = ShimmerAnimSpec,
                                        width = 400.dp
                                    )
                                    .background(
                                        AppTheme.colors.background(0.5.dp)
                                    )
                            )

                        }
                    )
                else
                    Spacer(
                        Modifier
                            .aspectRatio(1.0f)
                            .background(AppTheme.colors.background(0.5.dp), RectangleShape)
                            .shimmer(
                                Color.Gray.copy(0.5f),
                                animationSpec = ShimmerAnimSpec,
                                width = 20.dp
                            )
                    )
            }
        )

        // empty means empty
        data.isEmpty() -> item(
            span = fullLineSpan,
            key = "key_empty_placeholder...",
            content = {
                Placeholder(
                    title = stringResource(R.string.oops_empty),
                    iconResId = R.raw.lt_empty_box,
                    modifier = Modifier
                        .fillMaxSize()
                        .animateItem()
                )
            },
            contentType = "data_empty_placeholder"
        )
    }
    // return if non-empty else return null
    return data?.takeIf { it.isNotEmpty() }
}

/**
 * Applies a fading edge effect to content.
 *
 * Creates a gradient that fades the content to transparency at the edges.
 *
 * @param colors Gradient colors, e.g., `listOf(backgroundColor, Color.Transparent)`. Horizontal if `vertical` is `false`.
 * @param length Fade length from the edge.
 * @param vertical `true` for top/bottom fade, `false` for left/right. Defaults to `true`.
 * @return A [Modifier] with the fading edge effect.
 */
fun Modifier.fadingEdge2(
    colors: List<Color>,
    length: Dp = 10.dp,
    vertical: Boolean = true,
) = drawWithContent {
    drawContent()
    drawRect(Brush.verticalGradient(colors, endY = length.toPx()))
    drawRect(Brush.verticalGradient(colors.reversed(), startY = (size.height - length.toPx())))
}

/**
 * Creates and remembers a [PlayerController] instance using [remember].
 *
 * This composable function provides a convenient way to create and manage a `PlayerController` within a Jetpack Compose context.
 * The `PlayerController` is created lazily and stored in the composition's memory, ensuring that it's only initialized once
 * and reused across recompositions.
 *
 * @param handleAudioBecomingNoisy Whether the player should handle audio becoming noisy events. Defaults to false.
 * @param handleAudioFocus Whether the player should handle audio focus changes. Defaults to false.
 *
 * @return A [PlayerController] instance that is remembered across recompositions.
 */
@Composable
@NonRestartableComposable
fun rememberPlayerController(
    handleAudioBecomingNoisy: Boolean = false,
    handleAudioFocus: Boolean = false,
): PlayerController {
    val context = LocalContext.current
    return remember {
        // Lazily create the PlayerController
        val controller by lazy {
            PlayerController(context, handleAudioBecomingNoisy, handleAudioFocus)
        }
        // Return the PlayerController instance
        controller
    }
}

/**
 * A composable function that displays a [PlayerView] for media playback.
 *
 * This function provides a convenient way to integrate an ExoPlayer view into your Jetpack Compose UI.
 * It takes a [PlayerController] instance as input and configures the `PlayerView` accordingly.
 *
 * @param controller The [PlayerController] instance to use for media playback.
 * @param modifier The modifier to apply to the `PlayerView`.
 * @param background The background color of the `PlayerView`. Defaults to black.
 * @param keepScreenOn Whether to keep the screen on during playback. Defaults to false.
 * @param useController Whether to display the default playback controls. Defaults to false.
 */
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun PlayerView(
    controller: PlayerController,
    modifier: Modifier = Modifier,
    background: Color = Color.Black,
    keepScreenOn: Boolean = false,
    useController: Boolean = false,
) = AndroidView(
    modifier = modifier,
    onRelease = { Log.d(TAG, "PlayerView: releasing") },
    factory = { context ->
        Log.d(TAG, "PlayerView: creating")
        PlayerView(context, controller, useController)
    },
    update = { playerView ->
        Log.d(TAG, "PlayerView: updating")
        playerView.setBackgroundColor(background.toArgb())
        playerView.keepScreenOn = keepScreenOn
    }
)


/** Creates and [remember] s the instance of [HazeState] */
@Composable
@NonRestartableComposable
fun rememberBackgroundProvider() = remember(::HazeState)

fun Modifier.observe(provider: HazeState) = hazeSource(state = provider)

// Reusable mask
private val PROGRESSIVE_MASK = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
    Brush.verticalGradient(
        listOf(
            Color.Black,
            Color.Black,
            Color.Transparent
        )
    )
else
    Brush.verticalGradient(
        0.5f to Color.Black,
        0.8f to Color.Black.copy(0.5f),
        1.0f to Color.Transparent,
    )

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
    provider: HazeState,
    containerColor: Color = background(1.dp),
    blurRadius: Dp = if (containerColor.luminance() >= 0.5f) 38.dp else 80.dp,
    noiseFactor: Float = if (containerColor.luminance() >= 0.5f) 0.5f else 0.25f,
    tint: Color = containerColor.copy(alpha = if (containerColor.luminance() >= 0.5) 0.63f else 0.65f),
    blendMode: BlendMode = BlendMode.SrcOver,
    progressive: Float = -1f,
) = Background(
    Modifier
        .hazeEffect(state = provider) {
            this.blurEnabled = true
            this.blurRadius = blurRadius
            this.backgroundColor = containerColor
            // Disable noise factor on Android versions below 12.
            this.noiseFactor = noiseFactor
            this.tints = listOf(
                // apply luminosity just like in Microsoft Acrylic.
                HazeTint(Color.White.copy(0.07f), BlendMode.Luminosity),
                HazeTint(tint, blendMode = blendMode)
            )
            // Configure progressive blurring (if enabled).
            if (progressive != -1f) {
                this.progressive = HazeProgressive.verticalGradient(
                    startIntensity = progressive,
                    endIntensity = 0f,
                    preferPerformance = true
                )
                // Adjust input scale for Android versions below 12 for better visuals.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                    inputScale = HazeInputScale.Fixed(0.5f)
                mask = PROGRESSIVE_MASK
            }
        }
)

private val FloatingTopBarShape = RoundedCornerShape(20)

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

/**
 * Represents the general purpose [TopAppBar] for screens.
 * @param immersive weather to load a topBar tha is end to end or floating.
 */
@Composable
@NonRestartableComposable
fun GalleryTopAppBar(
    immersive: Boolean,
    title: @Composable () -> Unit,
    backdrop: Background,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    behavior: TopAppBarScrollBehavior,
    style: TopAppBarStyle = if (immersive) AppBarDefaults.largeAppBarStyle() else AppBarDefaults.floatingLargeAppBarStyle(),
    insets: WindowInsets = AppBarDefaults.topAppBarWindowInsets,
) = when {
    !immersive -> FloatingLargeTopAppBar(
        title = title,
        scrollBehavior = behavior,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = insets,
        style = style,
        background = {
            if (fraction > 0.1f) return@FloatingLargeTopAppBar Spacer(Modifier)
            val colors = AppTheme.colors
            Spacer(
                modifier = Modifier
                    .shadow(lerp(100.dp, 0.dp, fraction / .05f), FloatingTopBarShape)
                    .thenIf(fraction == 0f) {
                        border(colors.border, FloatingTopBarShape)
                    }
                    .background(backdrop)
                    .fillMaxSize()
            )
        }
    )

    else -> LargeTopAppBar(
        scrollBehavior = behavior,
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = insets,
        style = style,
        background = {
            if (fraction > 0.1f) return@LargeTopAppBar Spacer(Modifier)
            Spacer(
                modifier = Modifier
                    .background(backdrop)
                    .fillMaxSize()
            )
        }
    )
}