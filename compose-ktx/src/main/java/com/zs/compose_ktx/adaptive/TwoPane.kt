/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 07-08-2024.
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

package com.zs.compose_ktx.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.FabPosition
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.zs.compose_ktx.AppTheme

private const val TAG = "TwoPane"

/**
 * The empty top-padding
 */
private val PaddingNone = PaddingValues(0.dp)

// The indexes of the content slots
private const val INDEX_CONTENT = 0
private const val INDEX_TOP_BAR = 1
private const val INDEX_FAB = 2

// details might be absent; because return spacer is sometimes is tiresome.
// but others must be present.
private const val INDEX_DETAILS = 3

// used only in case of stacked layout strategy
private const val INDEX_SCRIM = 4

// FAB spacing above the bottom bar / bottom of the Scaffold
private val FabSpacing = 16.dp

private val DEFAULT_SPACING = 0.dp
private val DEFAULT_SCRIM_COLOR = Color.Black.copy(alpha = 0.32f)

@Composable
private inline fun Horizontal(
    strategy: HorizontalTwoPaneStrategy,
    spacing: Dp,
    content: @Composable @UiComposable () -> Unit,
    crossinline onUpdateIntent: (PaddingValues) -> Unit,
    modifier: Modifier = Modifier
) {

}


@Composable
private inline fun Stacked(
    strategy: StackedTwoPaneStrategy,
    content: @Composable @UiComposable () -> Unit,
    crossinline onIndentUpdated: (PaddingValues) -> Unit,
    modifier: Modifier = Modifier
) {

}

@Composable
private inline fun Vertical(
    strategy: VerticalTwoPaneStrategy,
    fabPosition: FabPosition,
    spacing: Dp,
    content: @Composable @UiComposable () -> Unit,
    crossinline onUpdateIntent: (PaddingValues) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO - Add Fold Aware split.
    Layout(content, modifier) { measurables, c ->
        val width = c.maxWidth;
        val height = c.maxHeight
        // measure content with original coordinates.
        // Loose constraints for initial measurements
        var constraints = c.copy(0, minHeight = 0)
        val topBarPlaceable = measurables[INDEX_TOP_BAR].measure(constraints)
        // Update content insets to account for Top Bar height
        onUpdateIntent(PaddingValues(top = topBarPlaceable.height.toDp()))
        val fabPlaceable = measurables[INDEX_FAB].measure(constraints)
        // Determine the vertical split point for the layout.
        // Both content and details will have half of the gapWidth subtracted from their maximum heights
        // to ensure proper spacing.
        val splitAt = strategy.calculate(IntSize(width, height))
        val gapWidthPx = spacing.roundToPx()
        // Measure Details Pane (if present), limiting its maximum height to the space below the split point
        val detailsMaxHeight = height - splitAt + gapWidthPx / 2
        constraints = c.copy(
            minWidth = 0,
            minHeight = 0,
            maxHeight = detailsMaxHeight
        )
        val detailsPlaceable = measurables[INDEX_DETAILS].measure(constraints)
        // Check if details pane is effectively absent (null or zero height)
        val isDetailsAbsent = detailsPlaceable.height == 0
        // Measure Content, dynamically allocating space based on the presence and size of the details pane.
        // If details are absent, content gets the full available space.
        // If details are present, content height is limited to either the space above the split
        // or the actual height of the details pane, whichever is greater.
        // This ensures content fills the available space efficiently, even if the details pane
        // doesn't fully utilize its allocated area.
        val contentAllocated = splitAt - gapWidthPx / 2
        val detailsMeasured = detailsPlaceable.height + splitAt / 2
        val available = maxOf(contentAllocated, detailsMeasured)
        constraints = if (isDetailsAbsent) c else
            c.copy(
                minHeight = available,
                maxHeight = available
            )
        val contentPlaceable = measurables[INDEX_CONTENT].measure(constraints)
        layout(width, height) {
            // place the content at top
            contentPlaceable.placeRelative(0, 0)
            // place topBar at top the content
            topBarPlaceable.placeRelative(0, 0)
            // place fab according to fabPosition.
            val fabSpacingPx = FabSpacing.roundToPx()
            fabPlaceable.placeRelative(
                y = contentPlaceable.height - fabPlaceable.height - fabSpacingPx,
                x = when (fabPosition) {
                    FabPosition.End -> contentPlaceable.width - fabPlaceable.width - fabSpacingPx
                    FabPosition.Center -> (contentPlaceable.width - fabPlaceable.width) / 2
                    FabPosition.Start -> fabSpacingPx
                    else -> error("Invalid fab position")
                }
            )
            if (isDetailsAbsent) return@layout
            // after the gap place the details at the bottom of the content
            detailsPlaceable.placeRelative(
                width / 2 - detailsPlaceable.width / 2,
                height - detailsPlaceable.height
            )
        }
    }
}

/**
 * A simple slot for holding content within the TwoPane layout.
 *
 * @param content The composable content to be displayed within the slot.
 */
@Composable
private inline fun Slot(content: @Composable () -> Unit) =
    Box(content = { content() })

/**
 * A two-pane layout that displays content and details according to the provided strategy.
 * This component is particularly useful in scenarios where traditional shared transitions with
 * dialogs are not feasible, such as when using a [StackedTwoPaneStrategy].
 *
 * The layout adapts to the presence of details content: if the `details` composable has a size
 * of 0, it is not rendered, optimizing the layout for situations where details are not always present.
 *
 * It also supports the use of a top app bar, seamlessly integrating its indent into the content
 * via [WindowInsets.Companion.contentInsets]. Additionally, you can include a floating action
 * button (FAB) positioned relative to the `content` using [fabPosition].
 *
 * @param content The main content to be displayed. This content occupies at least the fraction
 * specified in the strategy when the details pane is visible. Otherwise, it fills the available
 * space.
 * @param modifier Modifier to be applied to the layout.
 * @param spacing The spacing between the main content and details pane for horizontal or vertical
 * strategies. This parameter is ignored for stacked strategies or when no details are present.
 * @param background The background color of the layout.
 * @param onColor The content color of this layout, typically used for text or icons.
 * @param scrim The color of the scrim behind the details pane for stacked strategies, providing
 * a visual separation similar to a dialog.
 * @param details The optional details content to be displayed. When present, it occupies at
 * most the remaining space after the content pane's minimum allocation.
 * @param topBar The top app bar content to be displayed.
 * @param floatingActionButton The floating action button content to be displayed.
 * @param fabPosition The position of the floating action button relative to the content.
 * @param strategy The strategy to use for positioning the details pane relative to the content.
 */
@Composable
fun TwoPane(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = DEFAULT_SPACING,
    background: Color = AppTheme.colors.background,
    onColor: Color = AppTheme.colors.onBackground,
    scrim: Color = DEFAULT_SCRIM_COLOR,
    details: @Composable () -> Unit = { },
    topBar: @Composable () -> Unit = { },
    floatingActionButton: @Composable () -> Unit = { },
    fabPosition: FabPosition = FabPosition.End,
    strategy: TwoPaneStrategy = VerticalTwoPaneStrategy(0.5f)
) {
    // The indent propagated through window.contentIndent
    // The removes its old value; which means child has access to only topBar indent.
    val (indent, onIndentUpdated) = remember { mutableStateOf(PaddingNone) }
    // Compose the individual elements within a UiComposable to ensure they are measured and laid out correctly.
    val composed = @Composable @UiComposable {
        // Content (index 0)
        Slot {
            CompositionLocalProvider(
                LocalContentColor provides onColor,
                LocalContentInsets provides indent,
                content = content
            )
        }
        // Top Bar (index 1)
        Slot(topBar)
        // Floating Action Button (index 2)
        Slot(floatingActionButton)
        // Details (index 3, optional)
        Slot(details)
        // Scrim for StackedTwoPaneStrategy
        if (strategy is StackedTwoPaneStrategy) {
            // Consume interactions to prevent clicks passing through the scrim
            Spacer(
                Modifier
                    .pointerInput(Unit) {}
                    .background(scrim)
                    .fillMaxSize()
            )
        }
    }
    // Apply background color and fill the available space with the Scaffold
    val finalModifier = modifier
        .background(background)
        .fillMaxSize()
    // Delegate layout based on the chosen strategy
    when (strategy) {
        is VerticalTwoPaneStrategy -> Vertical(
            strategy,
            fabPosition,
            spacing,
            composed,
            onIndentUpdated,
            finalModifier
        )

        is HorizontalTwoPaneStrategy -> Horizontal(
            strategy,
            spacing,
            composed,
            onIndentUpdated,
            finalModifier
        )

        is StackedTwoPaneStrategy -> Stacked(strategy, composed, onIndentUpdated, finalModifier)
    }
}