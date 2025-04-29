/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 26-04-2025.
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

package com.zs.gallery.files

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.findActivity
import com.zs.compose.foundation.plus
import com.zs.compose.foundation.stickyHeader
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.VerticalDivider
import com.zs.compose.theme.adaptive.FabPosition
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.contentInsets
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.minimumInteractiveComponentSize
import com.zs.compose.theme.sharedBounds
import com.zs.compose.theme.text.Label
import com.zs.core.store.MediaFile
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.SelectionTracker
import com.zs.gallery.common.compose.FloatingActionMenu
import com.zs.gallery.common.compose.FloatingLargeTopAppBar
import com.zs.gallery.common.compose.OverflowMenu
import com.zs.gallery.common.compose.TonalHeader
import com.zs.gallery.common.compose.background
import com.zs.gallery.common.compose.emit
import com.zs.gallery.common.compose.fadingEdge2
import com.zs.gallery.common.compose.fullLineSpan
import com.zs.gallery.common.compose.rememberBackgroundProvider
import com.zs.gallery.common.preference
import com.zs.gallery.settings.Settings
import dev.chrisbanes.haze.haze
import androidx.compose.foundation.combinedClickable as clickable
import com.zs.gallery.common.compose.ContentPadding as CP

private val TileArrangement = Arrangement.spacedBy(2.dp)

@Composable
fun Files(viewState: FilesViewState) {
    // prioritise clearing of selection mode if back is pressed
    BackHandler(
        viewState.isInSelectionMode,
        viewState::clear
    )

    // The top nav insets
    val inAppNavInsets = WindowInsets.contentInsets
    val clazz = LocalWindowSize.current
    val portrait = clazz.width < clazz.height

    // Define the scroll behavior for the top app bar
    val topAppBarScrollBehavior = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val observer = rememberBackgroundProvider()

    val primary = @Composable {
        //
        val data = viewState.data
        val selected = viewState.selected
        val state = rememberLazyGridState()
        val navController = LocalNavController.current

        val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
        val colors = AppTheme.colors
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Adaptive(Settings.STANDARD_TILE_SIZE * multiplier),
            horizontalArrangement = TileArrangement,
            verticalArrangement = TileArrangement,
            contentPadding = inAppNavInsets +
                    WindowInsets.contentInsets +
                    PaddingValues(end = if (!portrait) CP.large else 0.dp) +
                    PaddingValues(horizontal = CP.medium),
            modifier = Modifier
                .fillMaxSize()
                .fadingEdge2(
                    listOf(
                        colors.background(1.dp),
                        colors.background.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    length = 56.dp
                )
                .thenIf(observer != null) { haze(observer!!) }
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            content = {
                // emit state or return non-null | non empty data points
                val data = emit(data) ?: return@LazyVerticalGrid
                //
                for ((header, items) in data) {
                    // Selection level of the group.
                    val level by viewState.isGroupSelected(header.toString())
                    stickyHeader(state, key = header, contentType = "header") {
                        Row(
                            Modifier.padding(2.dp, 4.dp, 2.dp, 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                TonalHeader(header)
                                // toggle
                                IconButton(
                                    icon = when (level) {
                                        SelectionTracker.Level.NONE -> Icons.Outlined.Circle
                                        SelectionTracker.Level.PARTIAL -> Icons.Outlined.RemoveCircle
                                        SelectionTracker.Level.FULL -> Icons.Outlined.Verified
                                    },
                                    contentDescription = null,
                                    tint = if (level == SelectionTracker.Level.FULL) AppTheme.colors.accent else LocalContentColor.current,
                                    onClick = { viewState.select(header.toString()) }
                                )
                            }
                        )
                    }
                    // Rest of the items
                    items(
                        items,
                        key = MediaFile::id,
                        contentType = { "media_file" },
                        itemContent = { item ->
                            MediaFile(
                                focused = false,
                                value = item,
                                checked = when {
                                    selected.isEmpty() -> -1
                                    selected.contains(item.id) -> 1
                                    else -> 0
                                },
                                modifier = Modifier
                                    .sharedBounds(RouteFiles.buildSharedFrameKey(item.id))
                                    .clickable(
                                        // onClick of item
                                        onClick = {
                                            if (selected.isNotEmpty())
                                                viewState.select(item.id)
                                            else
                                                navController.navigate(
                                                    viewState.buildRouteViewer(
                                                        item.id
                                                    )
                                                )
                                        },
                                        // onLong Click
                                        onLongClick = { viewState.select(item.id) }
                                    )
                            )
                        }
                    )
                    // Spacer
                    item(contentType = "spacer", span = fullLineSpan, key = "${header}_items_end") {
                        Spacer(Modifier.padding(vertical = CP.normal))
                    }
                }
            }
        )
    }

    val actions = viewState.actions
    val ctx = LocalContext.current
    // Content
    Scaffold(
        fabPosition = if (portrait) FabPosition.Center else FabPosition.End,
        topBar = {
            val (icon, title) = viewState.meta
            FloatingLargeTopAppBar(
                title = { Label(title, maxLines = 2) },
                backdrop = observer,
                behavior = topAppBarScrollBehavior,
                navigationIcon = {
                    Icon(
                        imageVector = icon,
                        title.toString(),
                        modifier = Modifier.minimumInteractiveComponentSize(),
                        tint = Color.Unspecified
                    )
                },
                actions = {
                    if (!viewState.isInSelectionMode)
                        OverflowMenu(
                            actions,
                            onItemClicked = { viewState.onAction(it, ctx.findActivity()) }
                        )
                }
            )
        },
        floatingActionButton = {
            FloatingActionMenu(
                visible = viewState.isInSelectionMode,
                background = Color.Transparent,
                contentColor = AppTheme.colors.onBackground,
                insets = (if (portrait) inAppNavInsets else WindowInsets.contentInsets) + PaddingValues(
                    bottom = CP.medium
                ),
                modifier = Modifier.background(observer, AppTheme.colors.background),
                content = {
                    // Label
                    Label(
                        text = "${viewState.selected.size}",
                        style = AppTheme.typography.title2,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = CP.normal, end = CP.medium)
                    )

                    // Divider
                    VerticalDivider(modifier = Modifier.height(CP.large))

                    // overflow
                    OverflowMenu(
                        actions,
                        onItemClicked = { viewState.onAction(it, ctx.findActivity()) },
                        collapsed = 5
                    )
                }
            )
        },
        primary = primary
    )
}