/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 12-07-2024.
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

package com.zs.compose_ktx.toast

import androidx.annotation.IntDef
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.primex.core.SignalWhite
import com.primex.core.composableOrNull
import com.primex.material2.Label
import com.primex.material2.ListTile
import com.primex.material2.TextButton
import com.zs.compose_ktx.AppTheme
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

/**
 * Interface to represent a single [Toast] instance to be displayed by the [ToastHost].
 *
 * @property message The text message to be displayed in the Toast.
 * @property action Optional label for an action button to be shown in the Toast.
 * @property duration The duration for which the Toast should be displayed. See [Toast.DURATION_SHORT],
 * [Toast.DURATION_LONG], and [Toast.DURATION_INDEFINITE].
 * @property accent The accent color to be used for this Toast. Defaults to [Color.Unspecified].
 * @property icon Optional leading icon to be displayed in the Toast.
 */
interface Toast {

    companion object {
        /**
         * Show the Toast for a short period of time.
         */
        const val DURATION_SHORT = 0

        /**
         * Show the Toast for a long period of time.
         */
        const val DURATION_LONG = 1

        /*** Show the Toast indefinitely until explicitly dismissed or the action is clicked.
         */
        const val DURATION_INDEFINITE = 2

        /**
         * Result code indicating the Toast's action was performed.
         */
        const val ACTION_PERFORMED = 1

        /**
         * Result code indicating the Toast was dismissed.
         */
        const val RESULT_DISMISSED = 2
    }

    val accent: Color get() = Color.Unspecified
    val icon: ImageVector?

    val message: CharSequence
    val action: CharSequence? get() = null

    @Duration
    val duration: Int

    /**
     * Callback invoked when the Toast's action button is clicked.
     */
    fun action()

    /**
     * Callback invoked when the Toast is dismissed, either by timeout or user interaction.
     */
    fun dismiss()
}


@Stable
internal data class Data(
    override val icon: ImageVector?,
    override val message: CharSequence,
    @Duration override val duration: Int,
    override val action: CharSequence?,
    override val accent: Color,
    private val continuation: CancellableContinuation<Int>,
) : Toast {

    override fun action() {
        if (continuation.isActive) continuation.resume(Toast.ACTION_PERFORMED)
    }

    override fun dismiss() {
        if (continuation.isActive) continuation.resume(Toast.RESULT_DISMISSED)
    }
}

/**
 * Annotation for properties representing Toast duration values.
 */
@IntDef(Toast.DURATION_LONG, Toast.DURATION_SHORT, Toast.DURATION_INDEFINITE)
@Target(
    AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Duration

/**
 * Annotation for properties representing Toast result codes.
 */
@IntDef(Toast.ACTION_PERFORMED, Toast.RESULT_DISMISSED)
@Target(
    AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY, AnnotationTarget.TYPE
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Result

/**
 * Converts the [Toast] duration to milliseconds, considering accessibility settings.
 *
 * @param hasAction Whether the Toast has anaction button.
 * @param accessibilityManager The [AccessibilityManager] to use for calculating the timeout.
 * @return The recommended timeout in milliseconds, adjusted for accessibility.
 */
// TODO: magic numbers adjustment
internal fun Toast.toMillis(
    hasAction: Boolean,
    accessibilityManager: AccessibilityManager?
): Long {
    val original = when (duration) {
        Toast.DURATION_SHORT -> 4000L
        Toast.DURATION_LONG -> 10000L
        else -> Long.MAX_VALUE
    }
    if (accessibilityManager == null) {
        return original
    }
    return accessibilityManager.calculateRecommendedTimeoutMillis(
        original, containsIcons = true, containsText = true, containsControls = hasAction
    )
}


private fun Modifier.indicatior(color: Color) = this then Modifier.drawBehind {
    drawRect(color = color, size = size.copy(width = 4.dp.toPx()))
}

@Composable
fun Toast(
    state: Toast,
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.shapes.small,
    backgroundColor: Color = if (AppTheme.colors.isLight)
        Color(0xFF0E0E0F)
    else
        AppTheme.colors.background(1.dp),
    contentColor: Color = Color.SignalWhite,
    actionColor: Color = state.accent.takeOrElse { AppTheme.colors.accent },
    elevation: Dp = 6.dp,
) {
    Surface(
        // fill whole width and add some padding.
        modifier = modifier
            .padding(horizontal = 16.dp)
            .sizeIn(minHeight = 56.dp),
        shape = shape,
        elevation = elevation,
        color = backgroundColor,
        contentColor = contentColor,
        content = {
            ListTile(
                // draw the indicator.
                modifier = Modifier.indicatior(actionColor),
                centerAlign = false,
                color = Color.Transparent,
                headline = {
                    Label(
                        text = state.message,
                        color = LocalContentColor.current,
                        style = AppTheme.typography.bodyMedium,
                        maxLines = 6,
                    )
                },
                leading = composableOrNull(state.icon != null) {
                    // TODO: It might case the problems.
                    val icon = state.icon!!
                    Icon(
                        painter = rememberVectorPainter(image = icon),
                        contentDescription = null,
                        tint = actionColor
                    )
                },
                trailing = composableOrNull(state.action != null) {
                    if (state.action != null)
                        TextButton(
                            label = state.action!!,
                            onClick = state::action,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = actionColor
                            )
                        )
                    else
                        com.primex.material2.IconButton(
                            onClick = { state.dismiss() },
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null
                        )
                }
            )
        },
    )
}