package com.zs.gallery.common.compose

import androidx.annotation.RawRes
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieDynamicProperties
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottiePainter
import com.zs.compose.foundation.composableIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.Placeholder
import com.zs.compose.theme.TonalIconButton
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.Text
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
    dynamicProperties: LottieDynamicProperties? = null,
    progressRange: ClosedFloatingPointRange<Float> = 0f..1f,
    animationSpec: AnimationSpec<Float>? = null,
): Painter {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(id))
    val duration2 = composition?.duration?.roundToInt() ?: AnimationConstants.DefaultDurationMillis
    val progress by animateFloatAsState(
        targetValue = if (!atEnd) progressRange.start else progressRange.endInclusive,
        label = "Lottie $id",
        animationSpec = animationSpec ?: tween(duration2)
    )
    return rememberLottiePainter(
        composition = composition,
        dynamicProperties = dynamicProperties,
        progress = progress,
    )
}

/**
 * A composable function that displays a Lottie animation as an icon.
 * This function uses [lottieAnimationPainter] to render the animation and behaves like an [Icon].
 * The animation plays based on the `atEnd` parameter, animating between the start and end frames.
 *
 * @param id The raw resource ID of the Lottie animation file.
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 * @param modifier The [Modifier] to be applied to this icon.
 * @param atEnd A boolean indicating whether the animation should be at its end state.
 *              When this value changes, the animation transitions between the start and end of the `progressRange`.
 * @param scale A factor to scale the size of the icon. Defaults to 1f (no scaling).
 * @param progressRange A [ClosedFloatingPointRange] specifying the start and end progress values for the animation.
 *                      Defaults to 0f..1f, representing the full animation.
 * @param duration The duration of the animation transition in milliseconds.
 *                 Defaults to -1, which means the duration will be derived from the Lottie composition.
 * @param easing The [Easing] function to be used for the animation transition.
 *               Defaults to [FastOutSlowInEasing].
 */
@Composable
inline fun LottieAnimatedIcon(
    @RawRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    atEnd: Boolean = false,
    scale: Float = 1f,
    progressRange: ClosedFloatingPointRange<Float> = 0f..1f,
    tint: Color = Color.Unspecified,
    animationSpec: AnimationSpec<Float>? = null,
) {
    Icon(
        painter = lottieAnimationPainter(
            id = id,
            atEnd = atEnd,
            progressRange = progressRange,
            animationSpec = animationSpec
        ),
        tint = tint,
        contentDescription = contentDescription,
        modifier = modifier
            .size(24.dp)
            .scale(scale),
    )
}

@Composable
fun LottieAnimatedIcon(
    @RawRes id: Int,
    isPlaying: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    //progressRange: ClosedFloatingPointRange<Float> = 0f..1f,
    tint: Color = Color.Unspecified,
    repeatMode: RepeatMode = RepeatMode.Restart,
    iterations: Int = Int.MAX_VALUE,
    // animationSpec: AnimationSpec<Float>? = null,
) {
    Icon(
        painter = lottieAnimationPainter(
            id = id,
            isPlaying = isPlaying,
            // progressRange = progressRange,
            // animationSpec = animationSpec,
            repeatMode = repeatMode,
            iterations = iterations
        ),
        tint = tint,
        contentDescription = contentDescription,
        modifier = modifier
            .size(24.dp)
            .scale(scale),
    )
}

@Composable
inline fun LottieAnimatedButton(
    @RawRes id: Int,
    noinline onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    atEnd: Boolean = false,
    scale: Float = 1f,
    progressRange: ClosedFloatingPointRange<Float> = 0f..1f,
    tint: Color = Color.Unspecified,
    animationSpec: AnimationSpec<Float>? = null,
    enabled: Boolean = true,
) {
    IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(
            painter = lottieAnimationPainter(
                id = id,
                atEnd = atEnd,
                progressRange = progressRange,
                animationSpec = animationSpec
            ),
            tint = tint,
            contentDescription = contentDescription,
            modifier = Modifier.lottie(scale),
        )
    }
}

@Composable
inline fun LottieTonalButton(
    @RawRes id: Int,
    noinline onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    atEnd: Boolean = false,
    scale: Float = 1f,
    progressRange: ClosedFloatingPointRange<Float> = 0f..1f,
    tint: Color = Color.Unspecified,
    animationSpec: AnimationSpec<Float>? = null,
    enabled: Boolean = true,
) {
    TonalIconButton(onClick = onClick, modifier = modifier, enabled = enabled, tint) {
        Icon(
            painter = lottieAnimationPainter(
                id = id,
                atEnd = atEnd,
                progressRange = progressRange,
                animationSpec = animationSpec
            ),
            tint = tint,
            contentDescription = contentDescription,
            modifier = Modifier.lottie(scale),
        )
    }
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
    isPlaying: Boolean = true,
    dynamicProperties: LottieDynamicProperties? = null,
    repeatMode: RepeatMode = RepeatMode.Restart,
    iterations: Int = Int.MAX_VALUE,
): Painter {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(id))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed,
        isPlaying = isPlaying,
        reverseOnRepeat = repeatMode == RepeatMode.Reverse,
    )
    return rememberLottiePainter(
        composition = composition,
        progress = progress,

        dynamicProperties = dynamicProperties
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
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
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