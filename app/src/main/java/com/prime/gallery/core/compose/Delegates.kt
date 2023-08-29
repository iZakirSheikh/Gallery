package com.prime.gallery.core.compose

import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.AsyncUpdates
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieComposition
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.LongDurationMills
import com.primex.material3.Label
import com.primex.material3.Text
import kotlin.math.roundToLong

private const val TAG = "Scaffold"

//This file holds the simple extension, utility methods of compose.
/**
 * Composes placeholder with lottie icon.
 */
@Composable
inline fun Placeholder(
    title: String,
    modifier: Modifier = Modifier,
    vertical: Boolean = true,
    @RawRes iconResId: Int,
    message: String? = null,
    noinline action: @Composable (() -> Unit)? = null
) {
    com.primex.material3.Placeholder(
        modifier = modifier,
        vertical = vertical,
        message = { if (message != null) Text(text = message) },
        title = { Label(text = title.ifEmpty { " " }, maxLines = 2) },

        icon = {
            val composition by rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(
                    iconResId
                )
            )
            LottieAnimation(
                composition = composition, iterations = Int.MAX_VALUE
            )
        },
        action = action,
    )
}

/**
 * A composable function that delegates to [LottieAnimation] and behaves like [AndroidVectorDrawable].
 *
 * @param atEnd: A boolean parameter that determines whether to display the end-frame or the start
 *              frame of the animation. The change in value causes animation.
 * @param id: The resource identifier of the [LottieCompositionSpec.RawRes] type.
 * @param scale: A float parameter that adjusts the size of the animation. The default size is
 *               24.dp, and the scale can be used to increase or decrease it.
 * @param progressRange: A range of float values that specifies the start and end frames of the
 *                       animation. The default range is 0f..1f, which means the animation will
 *                       start from the first frame and end at the last frame. Some [Lottie]
 *                       animation files may have different start/end frames, and this parameter
 *                       can be used to adjust them accordingly.
 * @param duration: The duration of the animation in milliseconds. The default value is -1, which
 *                  means the animation will use the duration specified in the
 *                  [LottieCompositionSpec] object. If a positive value is given, it will override
 *                  the duration from the [LottieCompositionSpec] object.
 */
@Composable
inline fun LottieAnimation(
    @RawRes id: Int,
    modifier: Modifier = Modifier,
    atEnd: Boolean = false,
    scale: Float = 1f,
    progressRange: ClosedFloatingPointRange<Float> = 0f..1f,
    duration: Int = -1
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(id))
    val duration2 = composition?.duration?.roundToLong() ?: AnimationConstants.LongDurationMills
    val progress by animateFloatAsState(
        targetValue = if (atEnd) progressRange.start else progressRange.endInclusive,
        label = "Lottie $id",
        animationSpec = tween(if (duration == -1) duration2.toInt() else duration)
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .size(24.dp)
            .scale(scale)
            .then(modifier),
    )
}


/**
 * A Delegate to [LottieAnimation] that takes [RawRes] id as parameter.
 * @param scale A float parameter that adjusts the size of the animation. The default size is
 *              24.dp, and the scale can be used to increase or decrease it.
 * @see LottieAnimation
 */
@Composable
inline fun LottieAnimation(
    @RawRes id: Int,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
    iterations: Int = 1,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    enableMergePaths: Boolean = false,
    renderMode: RenderMode = RenderMode.AUTOMATIC,
    reverseOnRepeat: Boolean = false,
    maintainOriginalImageBounds: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    clipToCompositionBounds: Boolean = true,
    fontMap: Map<String, Typeface>? = null,
    asyncUpdates: AsyncUpdates = AsyncUpdates.AUTOMATIC
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(id)
    )
    LottieAnimation(
        composition,
        Modifier
            .size(24.dp)
            .scale(scale)
            .then(modifier),
        isPlaying,
        restartOnPlay,
        clipSpec,
        speed,
        iterations,
        outlineMasksAndMattes,
        applyOpacityToLayers,
        enableMergePaths,
        renderMode,
        reverseOnRepeat,
        maintainOriginalImageBounds,
        dynamicProperties,
        alignment,
        contentScale,
        clipToCompositionBounds,
        fontMap,
        asyncUpdates
    )
}

/**
 * A composable function that creates a [LottieAnimation] [IconButton] with the given resource
 * identifier of the [LottieCompositionSpec.RawRes] type. The [LottieAnimation] renders an Adobe
 * After Effects animation exported as JSON on the screen, and the [IconButton] provides a clickable
 * area around it.
 *
 * @param id: The resource identifier of the [LottieCompositionSpec.RawRes] type.
 * @param onClick: A lambda function that is invoked when the user clicks on the button.
 * @see LottieAnimation for more details about how to render a [Lottie] animation.
 * @see IconButton for more details about how to create a button with an icon.
 */
@Composable
inline fun LottieAnimButton(
    @RawRes id: Int,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    atEnd: Boolean = false,
    scale: Float = 1f,
    progressRange: ClosedFloatingPointRange<Float> = 0f..1f,
    duration: Int = -1,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    IconButton(
        onClick = onClick,
        modifier,
        enabled,
        interactionSource = interactionSource,
        content = {
            LottieAnimation(
                id = id,
                atEnd = atEnd,
                scale = scale,
                progressRange = progressRange,
                duration = duration
            )
        }
    )
}


/**
 * A composable function that creates a [rememberAnimatedVectorResource] [IconButton] with the given
 * resource identifier and the [IconButton] provides a clickable  area around it.
 *
 * @param id: The resource identifier of the [LottieCompositionSpec.RawRes] type.
 * @param onClick: A lambda function that is invoked when the user clicks on the button.
 * @see rememberAnimatedVectorResource for more details about how to render a [Lottie] animation.
 * @param atEnd: A boolean parameter that determines whether to display the end-frame or the start
 *              frame of the animation. The change in value causes animation.
 * @param id: The resource identifier of the [AnimatedVectorResource] type.
 * @param scale: A float parameter that adjusts the size of the animation. The default size is
 *               size of the icon drawable and the scale can be used to increase or decrease it.
 * @see IconButton for more details about how to create a button with an icon.
 * @see rememberAnimatedVectorResource
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
inline fun AnimatedIconButton(
    @DrawableRes id: Int,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    atEnd: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    tint: Color = Color.Unspecified
) {
    IconButton(onClick = onClick, modifier = modifier, enabled, interactionSource = interactionSource, ) {
        Icon(
            painter = rememberAnimatedVectorResource(id = id, atEnd = atEnd),
            modifier = Modifier.scale(scale),
            contentDescription = null,
            tint = tint
        )
    }
}

@Composable
fun BottomBarItem(
    label: CharSequence,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
    enabled: Boolean = !selected,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier.scale(0.85f),
        enabled = enabled,
        color = Color.Transparent,
        border = if (!selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.1f)),
        interactionSource = interactionSource,
        contentColor = MaterialTheme.colorScheme.primary,
        content = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = ContentPadding.medium)
                    .animateContentSize(),
            ) {
                Icon(imageVector = icon, contentDescription = null)
                if (selected)
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
            }
        }
    )
}

@Composable
fun NavigationRailItem2(
    label: CharSequence,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
    enabled: Boolean = !selected,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(16),
        modifier = modifier.scale(0.9f),
        enabled = enabled,
        color = Color.Transparent,
        border = if (!selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.1f)),
        interactionSource = interactionSource,
        contentColor = MaterialTheme.colorScheme.primary,
        content = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = ContentPadding.normal)
                    .animateContentSize(),
            ) {
                Icon(imageVector = icon, contentDescription = null)
                if (selected)
                    Text(
                        text = label,
                        modifier = Modifier.padding(top = ContentPadding.small),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
            }
        }
    )
}

