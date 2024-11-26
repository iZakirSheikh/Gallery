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

@file:Suppress("NOTHING_TO_INLINE")

package com.zs.gallery.common

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.zs.foundation.AppTheme
import com.zs.foundation.ContentPadding
import com.zs.foundation.effect.shimmer
import com.zs.foundation.thenIf
import com.zs.gallery.R

private const val TAG = "lazy-ktx"

/**
 * A helper function that emits a placeholder item if the provided list is empty or null.
 * Otherwise, it returns the non-empty, non-null list.
 *
 * @return  item list, can be null if placeholder emitted otherwise no-null
 */
inline fun <T> LazyGridScope.emit(
    data: List<T>?,
): List<T>? {
    when {
        // null means loading
        data == null -> item(
            span = fullLineSpan,
            key = "key_loading_placeholder",
            contentType = "data_loading_placeholder",
            content = {
                Placeholder(
                    title = stringResource(R.string.loading),
                    iconResId = R.raw.lt_loading_bubbles,
                    modifier = Modifier
                        .fillMaxSize()
                        .animateItem()
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
 * A simple utility fun for loading animation header index
 */
private fun isIndexHeader(index: Int) = when (index) {
    0, 2, 9, 18 -> true
    else -> false
}

private val ShimmerHeaderPadding =
    PaddingValues(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 50.dp)
private val ShimmerAnimSpec = infiniteRepeatable(
    tween<Float>(800, 100, easing = LinearEasing),
)

/**
 * @see emit
 */
@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <K, T> LazyGridScope.emit(
    data: Map<K, T>?,
): Map<K, T>? {
    val fullLineSpan = fullLineSpan
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
                Spacer(
                    Modifier
                        .then(
                            if (isHeader) Modifier
                                .padding(ShimmerHeaderPadding)
                                .requiredHeight(30.dp)
                            else
                                Modifier.aspectRatio(1.0f)
                        )
                        .background(
                            AppTheme.colors.background(0.5.dp),
                            if (isHeader) CircleShape else RectangleShape
                        )
                        .shimmer(
                            Color.Gray.copy(0.5f),
                            animationSpec = ShimmerAnimSpec,
                            blendMode = BlendMode.Hardlight,
                            width = 400.dp
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

private val HeaderPadding = PaddingValues(ContentPadding.medium, ContentPadding.xLarge, ContentPadding.medium, ContentPadding.medium)

/**
 * Item header.
 */
@Composable
private fun GroupHeader(
    label: CharSequence,
    state: GroupSelectionLevel,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.padding(HeaderPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Label(
            text = label,
            maxLines = 2,
            fontWeight = FontWeight.SemiBold,
            style = AppTheme.typography.titleMedium,
            modifier = Modifier
                .background(AppTheme.colors.background(1.dp), CircleShape)
                .widthIn(min = 100.dp)
                .padding(ContentPadding.normal, vertical = ContentPadding.medium)
        )

        IconButton(
            imageVector = when (state) {
                GroupSelectionLevel.NONE -> Icons.Outlined.CheckCircleOutline
                GroupSelectionLevel.PARTIAL -> Icons.Outlined.RemoveCircle
                GroupSelectionLevel.FULL -> Icons.Outlined.CheckCircleOutline
            },
            tint = if (state == GroupSelectionLevel.FULL) AppTheme.colors.accent else LocalContentColor.current,
            onClick = onToggle
        )
    }
}

/**
 * The group header
 */
@Composable
private fun GroupHeader(
    label: CharSequence,
    modifier: Modifier = Modifier
) = Label(
    text = label,
    maxLines = 2,
    style = AppTheme.typography.titleMedium,
    modifier = modifier.padding(HeaderPadding)
)

/**
 * @see items
 */
@JvmName("itemsCharSequence")
@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <T> LazyGridScope.items(
    data: Map<CharSequence, List<T>>,
    tracker: SelectionTracker? = null,
    noinline key: ((item: T) -> Any)? = null,
    crossinline itemContent: @Composable LazyGridItemScope.(item: T) -> Unit
) {
    // place the actual items on the screen.
    data.forEach { (header, list) ->
        item(
            span = fullLineSpan,
            key = "key_header_$header",
            contentType = "list_item_header",
            content = {
                val groupModifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
                val selector = tracker ?: return@item GroupHeader(header, groupModifier)
                val groupKey = header.toString()
                val state by remember(groupKey) { selector.isGroupSelected(groupKey) }
                GroupHeader(
                    label = header, state = state, { selector.select(groupKey) }, groupModifier
                )
            }
        )

        // place the actual items on the screen.
        items(
            items = list,
            key = key,
            contentType = { "main_item" },
            itemContent = itemContent
        )
    }
}


private val HeaderBgBrush
    @Composable
    @ReadOnlyComposable
    get() = Brush.verticalGradient(
        listOf(
            AppTheme.colors.background,
            Color.Transparent
        )
    )

/**
 * Displays a list of items in a LazyGrid, grouped by headers.
 * @param T The type of items in the list.
 * @param data The grouped list of items to display.
 * @param tracker An optional SelectionTracker to enable group selection.
 * @param key A function to provide a unique key for each item.
 * @param itemContent A composable function to display each item.
 */
@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <T> LazyGridScope.items(
    data: Map<String, List<T>>,
    tracker: SelectionTracker? = null,
    noinline key: ((item: T) -> Any)? = null,
    crossinline itemContent: @Composable LazyGridItemScope.(item: T) -> Unit
) {
    // place the actual items on the screen.
    data.forEach { (header, list) ->
        // GroupHeader as StickyHeader
        item(
            key = "key_header_$header",
            contentType = "list_item_header",
            span = fullLineSpan,
            content = {
                val groupModifier = Modifier
                    .background(HeaderBgBrush)
                    .animateItem()
                val selector = tracker ?: return@item GroupHeader(header, groupModifier)
                val state by remember(header) { selector.isGroupSelected(header) }
                GroupHeader(
                    label = header, state = state, { selector.select(header) }, groupModifier
                )
            }
        )

        // place the actual items on the screen.
        items(
            items = list,
            key = key,
            contentType = { "main_item" },
            itemContent = itemContent
        )
    }
}
