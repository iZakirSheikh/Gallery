/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 23-07-2024.
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

package com.zs.gallery.folders

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.primex.core.drawHorizontalDivider
import com.primex.core.textResource
import com.primex.material2.DropDownMenuItem
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.menu.DropDownMenu2
import com.primex.material2.neumorphic.NeumorphicTopAppBar
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.ContentPadding
import com.zs.compose_ktx.None
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.Placeholder
import com.zs.gallery.common.fullLineSpan
import com.zs.gallery.common.preference
import com.zs.gallery.files.RouteFolder
import com.zs.gallery.settings.Settings

private const val TAG = "Folders"

@StringRes
fun fetchOrderTitle(order: Int): Int {
    return when (order) {
        FoldersViewState.ORDER_BY_NAME -> R.string.name
        FoldersViewState.ORDER_BY_DATE_MODIFIED -> R.string.last_modified
        FoldersViewState.ORDER_BY_SIZE -> R.string.size
        else -> error("Oops invalid id passed $order")
    }
}

fun fetchOrderIcon(order: Int): ImageVector {
    return when (order) {
        FoldersViewState.ORDER_BY_NAME -> Icons.Outlined.TextFields
        FoldersViewState.ORDER_BY_DATE_MODIFIED -> Icons.Outlined.DateRange
        FoldersViewState.ORDER_BY_SIZE -> Icons.Outlined.Memory
        else -> error("Oops invalid id passed $order")
    }
}

/**
 * Constructs an order by menu. if [onRequestChange] == -1 then the menu is not shown.
 */
context(RowScope)
@Composable
private inline fun Actions(
    viewState: FoldersViewState
) {
    val ascending = viewState.ascending
    val rotation by animateFloatAsState(targetValue = if (ascending) 180f else 0f)
    IconButton(
        imageVector = Icons.AutoMirrored.Outlined.Sort,
        onClick = { viewState.ascending = !ascending },
        modifier = Modifier.rotate(rotation)
    )
    // show order
    var expanded by remember { mutableStateOf(false) }
    Button(
        onClick = { expanded = !expanded },
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier
            .padding(end = AppTheme.padding.small)
            .scale(0.90f),
        shape = CircleShape,
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.background(elevation = 1.dp),
            contentColor = AppTheme.colors.onBackground,
        ),
        content = {
            val order = viewState.order
            // icon
            Icon(
                painter = rememberVectorPainter(image = fetchOrderIcon(order = order)),
                contentDescription = null,
                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing)
            )

            // label
            Label(text = stringResource(id = fetchOrderTitle(order)))

            DropDownMenu2(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = AppTheme.shapes.compact,
                modifier = Modifier.sizeIn(minWidth = 180.dp),
                content = {
                    // Sort by size
                    repeat(3) {
                        DropDownMenuItem(
                            title = textResource(id = fetchOrderTitle(it)),
                            icon = rememberVectorPainter(image = fetchOrderIcon(it)),
                            onClick = { viewState.order = it; expanded = false },
                            enabled = order != it
                        )
                    }
                }
            )
        },
    )
}

@Composable
@NonRestartableComposable
private fun Toolbar(
    viewState: FoldersViewState,
    modifier: Modifier = Modifier
) {
    NeumorphicTopAppBar(
        title = { Label(text = textResource(id = R.string.folders)) },
        elevation = AppTheme.elevation.low,
        shape = CircleShape,
        modifier = modifier.padding(top = ContentPadding.medium),
        lightShadowColor = AppTheme.colors.lightShadowColor,
        darkShadowColor = AppTheme.colors.darkShadowColor,
        navigationIcon = {
            IconButton(imageVector = Icons.Default.FolderCopy, onClick = {})
        },
        actions = { Actions(viewState = viewState) }
    )
}

/**
 * The min size of the single cell in grid.
 */
private val MIN_TILE_SIZE = 100.dp
private val FolderContentPadding =
    PaddingValues(vertical = AppTheme.padding.normal, horizontal = AppTheme.padding.medium)
private val GridItemsArrangement = Arrangement.spacedBy(6.dp)

@Composable
private fun FolderGrid(
    state: FoldersViewState,
    modifier: Modifier = Modifier
) {
    val values by state.data.collectAsState()
    val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
    val navController = LocalNavController.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(MIN_TILE_SIZE * multiplier),
        modifier,
        contentPadding = FolderContentPadding,
        verticalArrangement = GridItemsArrangement,
        content = {
            // null means loading
            val data = values ?: return@LazyVerticalGrid item(
                span = fullLineSpan, key = "key_loading_placeholder",
                content = {
                    Placeholder(
                        title = stringResource(R.string.loading),
                        iconResId = R.raw.lt_loading_dots_blue,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            )

            // empty means empty
            if (data.isEmpty())
                return@LazyVerticalGrid item(
                    span = fullLineSpan,
                    key = "key_empty_placeholder...",
                    content = {
                        Placeholder(
                            title = stringResource(R.string.oops_empty),
                            iconResId = R.raw.lt_empty_box,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )

            // place the actual items on the screen.
            items(items = data, key = { it.artworkID }) {
                Folder(
                    value = it,
                    modifier = Modifier
                        .clickable() { navController.navigate(RouteFolder(it.path)) }
                        .animateItem()
                )
            }
        }
    )
}

@Composable
fun Folders(viewState: FoldersViewState) {
    Scaffold(
        topBar = {
            Toolbar(
                viewState = viewState,
                modifier = Modifier
                    .statusBarsPadding()
                    .drawHorizontalDivider(color = AppTheme.colors.onBackground)
                    .padding(bottom = ContentPadding.medium),
            )
        },
        contentWindowInsets = WindowInsets.None,
        content = {
            FolderGrid(state = viewState, modifier = Modifier.padding(it))
        }
    )
}
