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

@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zs.gallery.folders

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import com.primex.core.drawHorizontalDivider
import com.primex.core.plus
import com.primex.core.textResource
import com.primex.material2.DropDownMenuItem
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.menu.DropDownMenu2
import com.primex.material2.neumorphic.NeumorphicTopAppBar
import com.zs.foundation.AppTheme
import com.zs.foundation.ContentPadding
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.contentInsets
import com.zs.foundation.sharedElement
import com.zs.gallery.R
import com.zs.gallery.bin.RouteTrash
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.emit
import com.zs.gallery.common.fullLineSpan
import com.zs.gallery.common.preference
import com.zs.gallery.files.RouteAlbum
import com.zs.gallery.files.RouteFolder
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.RouteViewer

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
@Suppress("NOTHING_TO_INLINE")
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

private fun LazyGridScope.shortcuts(navController: NavHostController) =
    items(2, contentType = { "shortcut" }) { index ->
        when (index) {
            0 -> Shortcut(
                Icons.Outlined.HotelClass,
                "Favourites",
                onClick = { navController.navigate(RouteAlbum()) }
            )

            1 -> Shortcut(
                Icons.Outlined.Recycling,
                "Recycle Bin",
                onClick = { navController.navigate(RouteTrash()) },
                // Only enable if R and above
                enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            )
        }
    }

@Composable
fun Folders(viewState: FoldersViewState) {
    val navInsets = WindowInsets.contentInsets
    TwoPane(
        topBar = {
            Toolbar(
                viewState = viewState,
                modifier = Modifier
                    .background(AppTheme.colors.background)
                    .statusBarsPadding()
                    .drawHorizontalDivider(color = AppTheme.colors.onBackground)
                    .padding(bottom = ContentPadding.medium),
            )
        },
        content = {
            val values by viewState.data.collectAsState()
            val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
            val navController = LocalNavController.current
            LazyVerticalGrid(
                columns = GridCells.Adaptive((MIN_TILE_SIZE * multiplier.coerceAtLeast(1f))),
                contentPadding = FolderContentPadding + WindowInsets.contentInsets + navInsets,
                verticalArrangement = GridItemsArrangement,
                horizontalArrangement = GridItemsArrangement,
                content = {
                    val data = emit(values) ?: return@LazyVerticalGrid

                    // The standard shortcuts.
                    shortcuts(navController)

                    item(span = fullLineSpan) {
                        Spacer(Modifier.padding(vertical = ContentPadding.medium))
                    }

                    // else emit the items.
                    items(data, key = { it.artworkID }) {
                        Folder(
                            value = it,
                            modifier = Modifier
                                .clickable() { navController.navigate(RouteFolder(it.path)) }
                                .sharedElement(RouteViewer.buildSharedFrameKey(it.artworkID))
                                .animateItem()
                        )
                    }
                },
            )
        }
    )
}