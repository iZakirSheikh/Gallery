/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 20-07-2024.
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

package com.zs.foundation.adaptive

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.zs.foundation.toast.ToastHost
import com.zs.foundation.toast.ToastHostState

private const val TAG = "NavigationSuiteScaffold"

private const val INDEX_CONTENT = 0
private const val INDEX_NAV_BAR = 1
private const val INDEX_TOAST = 2
private const val INDEX_PROGRESS_BAR = 3

private val EmptyPadding = PaddingValues(0.dp)

/**
 * The content insets for the screen under current [NavigationSuitScaffold]
 */
internal val LocalContentInsets =
    compositionLocalOf { EmptyPadding }

/**
 * Provides the insets for the current content within the [Scaffold].
 */
val WindowInsets.Companion.contentInsets
    @ReadOnlyComposable @Composable get() = LocalContentInsets.current

/**
 * The standard spacing (in dp) applied between two consecutive items,
 * such as the pixel and snack bar, within a vertical layout.
 */
private val STANDARD_SPACING = 8.dp

/**
 * A flexible Scaffold composable for navigation-based layouts.
 *
 * This composable provides a basic structure for screens within a navigation flow,
 * including a content area, optional navigation bar, toast host, and progress indicator.
 *
 * @param vertical Whether the layout should be arranged vertically (true) or horizontally (false).
 * @param content The main content of the screen.
 * @param modifier Modifier to be applied to the Scaffold layout.
 * @param hideNavigationBar Whether to hide the navigation bar.
 * @param background Background color for the Scaffold.
 * @param contentColor Content color for the main content area.
 * @param toastHostState State for managing toasts (similar to Snackbars).
 * @param progress Progress value for the progress indicator (NaN for no indicator, -1 for indeterminate).
 * @param navBar Composable function that provides the navigation bar content.
 */
@Composable
fun NavigationSuiteScaffold(
    vertical: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    hideNavigationBar: Boolean = false,
    background: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor = background),
    toastHostState: ToastHostState = remember(::ToastHostState),
    @FloatRange(0.0, 1.0) progress: Float = Float.NaN,
    navBar: @Composable () -> Unit,
) {
    // Compose all the individual elements of the Scaffold into a single composable
    val (insets, onNewInsets) =
        remember { mutableStateOf(EmptyPadding) }
    val navBarInsets = WindowInsets.navigationBars
    Layout(
        modifier = modifier
            .background(background)
            .fillMaxSize(),
        measurePolicy = remember(vertical, navBarInsets) {
            when{
                vertical -> NavVerticalMeasurePolicy(navBarInsets, onNewInsets)
                else -> NavHorizontalMeasurePolicy(navBarInsets, onNewInsets)
            }
        },
        content = {
            // Provide the content color for the main content
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalContentInsets provides insets,
                content = content
            )
            // Conditionally display the navigation bar based on
            // 'hideNavigationBar'
            // Display the navigation bar (either bottom bar or navigation rail)
            when {
                // Don't show anything.
                hideNavigationBar -> Spacer(modifier = Modifier)
                else -> navBar()
            }
            // Display the Snackbar using the provided channel
            ToastHost(toastHostState)
            // Conditionally display the progress bar based on the 'progress' value
            // Show an indeterminate progress bar when progress is -1
            // Show a determinate progress bar when progress is between 0 and 1
            when {
                progress == -1f -> LinearProgressIndicator()
                !progress.isNaN() -> LinearProgressIndicator(progress = progress)
                else -> Spacer(modifier = Modifier)
            }
        }
    )
}

private class NavVerticalMeasurePolicy(
    private val insets: WindowInsets,
    private val onNewInsets: (PaddingValues) -> Unit
): MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, c: Constraints): MeasureResult {
        val width = c.maxWidth;
        val height = c.maxHeight
        // Measure the size requirements of each child element, allowing
        // them to use the full width
        val constraints = c.copy(minHeight = 0)
        val toastPlaceable = measurables[INDEX_TOAST].measure(constraints)
        val progressBarPlaceable = measurables[INDEX_PROGRESS_BAR].measure(constraints)
        val navBarPlaceable = measurables[INDEX_NAV_BAR].measure(constraints)
        // Measure content against original constraints.
        // the content must fill the entire screen.
        val contentPlaceable = measurables[INDEX_CONTENT].measure(c)
        // Calculate the insets for the content.
        // and report through onNewIntent
        onNewInsets(PaddingValues(bottom = navBarPlaceable.height.toDp()))
        // Place the content
        return layout(width, height){
            var x = 0;
            var y = 0
            // Place the main content at the top, filling the space up to the navigation bar
            contentPlaceable.placeRelative(x, y)
            // Place navbar at the bottom of the screen.
            x = 0; y = height - navBarPlaceable.height
            navBarPlaceable.placeRelative(x, y)
            // Place progress bar at the very bottom of the screen, ignoring system insets
            // (it might overlap system bars if they are not colored)
            x = width / 2 - progressBarPlaceable.width / 2
            y = (height - progressBarPlaceable.height)
            progressBarPlaceable.placeRelative(x, y)
            // we only need bottom insets since we are placing above the navBar
            val insetBottom =
                if (navBarPlaceable.height == 0) insets.getBottom(density = this@measure) else 0
            // Place Toast at the centre bottom of the screen
            // remove nav bar offset from it.
            x = width / 2 - toastPlaceable.width / 2   // centre
            // full height - toaster height - navbar - 16dp padding + navbar offset.
            y =
                (height - navBarPlaceable.height - toastPlaceable.height - STANDARD_SPACING.roundToPx() - insetBottom)
            toastPlaceable.placeRelative(x, y)
        }
    }
}

private class NavHorizontalMeasurePolicy(
    private val insets: WindowInsets,
    private val onNewInsets: (PaddingValues) -> Unit
): MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, c: Constraints): MeasureResult {
        val width = c.maxWidth;
        val height = c.maxHeight
        // Measure the size requirements of each child element
        // Allow the elements to have a custom size by not constraining them.
        var constraints = c.copy(minHeight = 0)
        val progressBarPlaceable = measurables[INDEX_PROGRESS_BAR].measure(constraints)
        constraints = c.copy(minHeight = 0, minWidth = 0)
        val toastPlaceable = measurables[INDEX_TOAST].measure(constraints)
        val navBarPlaceable = measurables[INDEX_NAV_BAR].measure(constraints)
        // Calculate the width available for the main content, excluding the navigation bar
        val contentWidth = width - navBarPlaceable.width
        constraints = c.copy(minWidth = contentWidth, maxWidth = contentWidth)
        val contentPlaceable = measurables[INDEX_CONTENT].measure(constraints)
        // Calculate the insets for the content.
        // and report through onNewIntent
        // onNewInsets(PaddingValues(start = navBarPlaceable.width.toDp()))
        // Place the content
        // reset new insets
        onNewInsets(EmptyPadding)
        return layout(width, height) {
            var x = 0;
            var y = 0
            // place nav_bar from top at the start of the screen
            navBarPlaceable.placeRelative(x, y)
            x = navBarPlaceable.width
            // Place the main content at the top, after nav_bar width
            contentPlaceable.placeRelative(x, y)
            // Place progress bar at the very bottom of the screen, ignoring system insets
            // (it might overlap system bars if they are not colored)
            x = width / 2 - progressBarPlaceable.width / 2
            y = (height - progressBarPlaceable.height)
            progressBarPlaceable.placeRelative(x, y)
            // Place toast above the system navigationBar at the centre of the screen.
            val insetBottom = insets.getBottom(density = this@measure)
            x = width / 2 - toastPlaceable.width / 2   // centre
            // full height - toaster height - navbar - 16dp padding + navbar offset.
            y = (height - toastPlaceable.height - STANDARD_SPACING.roundToPx() - insetBottom)
            toastPlaceable.placeRelative(x, y)
        }
    }
}