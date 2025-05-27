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
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottiePainter
import com.zs.compose.foundation.composableIf
import com.zs.compose.foundation.effects.shimmer
import com.zs.compose.foundation.fullLineSpan
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.LocalNavAnimatedVisibilityScope
import com.zs.compose.theme.Placeholder
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.Text
import com.zs.core.billing.Purchase
import com.zs.core.player.PlayerController
import com.zs.core.player.PlayerView
import com.zs.gallery.R
import com.zs.gallery.common.Mapped
import com.zs.gallery.common.Route
import com.zs.gallery.common.SystemFacade
import com.zs.gallery.common.compose.ContentPadding
import com.zs.preferences.Key
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
        composition = composition, progress = progress
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
                text = title.ifEmpty { " " }, maxLines = 2, color = AppTheme.colors.onBackground
            )
        },
        icon = {
            Image(
                painter = lottieAnimationPainter(id = iconResId), contentDescription = null
            )
        },
        action = action,
    )
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
        data == null -> items(20, contentType = { "loading_item" }, span = {
            if (isIndexHeader(it)) GridItemSpan(maxLineSpan)
            else GridItemSpan(1)
        }, itemContent = {
            val isHeader = isIndexHeader(it)
            if (isHeader) Box(
                contentAlignment = Alignment.CenterStart, content = {
                    Spacer(
                        Modifier
                            .padding(
                                top = ContentPadding.large, bottom = ContentPadding.medium
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

                })
            else Spacer(
                Modifier
                    .aspectRatio(1.0f)
                    .background(AppTheme.colors.background(0.5.dp), RectangleShape)
                    .shimmer(
                        Color.Gray.copy(0.5f), animationSpec = ShimmerAnimSpec, width = 20.dp
                    )
            )
        })

        // empty means empty
        data.isEmpty() -> item(
            span = fullLineSpan, key = "key_empty_placeholder...", content = {
                Placeholder(
                    title = stringResource(R.string.oops_empty),
                    iconResId = R.raw.lt_empty_box,
                    modifier = Modifier
                        .fillMaxSize()
                        .animateItem(
                            AppTheme.motionScheme.slowSpatialSpec<Float>(),
                            AppTheme.motionScheme.defaultSpatialSpec(),
                            AppTheme.motionScheme.slowSpatialSpec<Float>(),
                        )
                )
            }, contentType = "data_empty_placeholder"
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
// TODO - Add logic to make fading edge apply/exclude content padding in real one.
fun Modifier.fadingEdge2(
    length: Dp = 10.dp,
    vertical: Boolean = true,
) = graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
    drawContent()
    drawRect(
        Brush.verticalGradient(
            listOf(Color.Black, Color.Transparent), endY = length.toPx(), startY = 0f
        ), blendMode = BlendMode.DstOut, size = size.copy(height = length.toPx())
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startY = size.height - length.toPx(),
            endY = size.height
        ),
        //  topLeft = Offset(0f, size.height - length.toPx()),
        // size = size.copy(height = length.toPx()),
        blendMode = BlendMode.DstOut
    )
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
    })


/**
 * Adds a composable route to the [NavGraphBuilder] for the given [Route].
 *
 * @param route The [Route] object representing the navigation destination.
 * @param content The composable content to display for this route.
 */
fun NavGraphBuilder.composable(
    route: Route,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composable(
    route = route.route, content = { id ->
        CompositionLocalProvider(value = LocalNavAnimatedVisibilityScope provides this) {
            content(id)
        }
    })

/**
 * Used to provide access to the [NavHostController] through composition without needing to pass it down the tree.
 *
 * To use this composition local, you can call [LocalNavController.current] to get the [NavHostController].
 * If no [NavHostController] has been set, an error will be thrown.
 *
 * Example usage:
 *
 * ```
 * val navController = LocalNavController.current
 * navController.navigate("destination")
 * ```
 */
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("no local nav host controller found")
}

/**
 * A [staticCompositionLocalOf] variable that provides access to the [SystemFacade] interface.
 *
 * The [SystemFacade] interface defines common methods that can be implemented by an activity that
 * uses a single view with child views.
 * This local composition allows child views to access the implementation of the [SystemFacade]
 * interface provided by their parent activity.
 *
 * If the [SystemFacade] interface is not defined, an error message will be thrown.
 */
val LocalSystemFacade = staticCompositionLocalOf<SystemFacade> {
    error("Provider not defined.")
}

/**
 * A composable function that uses the [LocalSystemFacade] to fetch [Preference] as state.
 * @param key A key to identify the preference value.
 * @return A [State] object that represents the current value of the preference identified by the provided key.
 * The value can be null if no preference value has been set for the given key.
 */
@Composable
inline fun <S, O> preference(key: Key.Key1<S, O>): androidx.compose.runtime.State<O?> {
    val provider = LocalSystemFacade.current
    return provider.observeAsState(key = key)
}

/**
 * @see [preference]
 */
@Composable
inline fun <S, O> preference(key: Key.Key2<S, O>): androidx.compose.runtime.State<O> {
    val provider = LocalSystemFacade.current
    return provider.observeAsState(key = key)
}

/**
 * A composable function that retrieves the purchase state of a product using the [LocalSystemFacade].
 *
 * This function leverages the `LocalSystemFacade` to access the purchase information for a given product ID.
 * In preview mode, it returns a `null` purchase state as the activity context is unavailable.
 *
 * @param id The ID of the product to check the purchase state for.
 * @return A [State] object representing the current purchase state of the product.
 * The state value can be `null` if there is no purchase associated with the given product ID or if the function
 * is called in preview mode.
 */
@Composable
@NonRestartableComposable
@Stable
fun purchase(id: String): State<Purchase?> = LocalSystemFacade.current.observePurchaseAsState(id)

@Composable
inline fun <S> ProvideAnimationScope(
    target: S,
    modifier: Modifier = Modifier,
    crossinline content: @Composable() AnimatedContentScope.(targetState: S) -> Unit,
) {
    AnimatedContent(target, modifier) { value ->
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            content(value)
        }
    }
}

// Spacer
fun LazyGridScope.section(height: Dp = ContentPadding.normal) =
    item(contentType = "spacer", span = fullLineSpan) {
        Spacer(Modifier.padding(vertical = height))
    }