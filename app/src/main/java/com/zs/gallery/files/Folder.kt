/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 24-07-2024.
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.primex.core.findActivity
import com.primex.core.plus
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.Text
import com.primex.material2.appbar.LargeTopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior
import com.zs.foundation.AppTheme
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.None
import com.zs.foundation.VerticalDivider
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.contentInsets
import com.zs.foundation.sharedElement
import com.zs.gallery.common.FloatingActionMenu
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.MediaFile
import com.zs.gallery.common.emit
import com.zs.gallery.common.items
import com.zs.gallery.common.preference
import com.zs.gallery.viewer.RouteViewer
import com.zs.gallery.settings.Settings


@Composable
private fun TopAppBar(
    viewState: FolderViewState,
    modifier: Modifier = Modifier,
    behavior: TopAppBarScrollBehavior? = null,
    insets: WindowInsets = WindowInsets.None
) {
    LargeTopAppBar(
        navigationIcon = {
            Icon(
                imageVector = Icons.Outlined.FolderCopy,
                contentDescription = null,
                modifier = Modifier.padding(AppTheme.padding.normal)
            )
        },
        title = { Text(text = viewState.title, maxLines = 2, lineHeight = 20.sp) },
        scrollBehavior = behavior,
        windowInsets = insets,
        modifier = modifier,
        style = TopAppBarDefaults.largeAppBarStyle(
            containerColor = AppTheme.colors.background,
            scrolledContainerColor = AppTheme.colors.background(elevation = 1.dp),
            scrolledContentColor = AppTheme.colors.onBackground,
            contentColor = AppTheme.colors.onBackground
        ),
    )
}

@Composable
private fun Actions(
    state: FolderViewState,
    modifier: Modifier = Modifier
) = FloatingActionMenu(modifier = modifier) {
    // Label
    Label(
        text = "${state.selected.size}",
        modifier = Modifier.padding(
            start = AppTheme.padding.normal,
            end = AppTheme.padding.medium
        ),
        style = AppTheme.typography.titleLarge
    )
    // Divider
    VerticalDivider(modifier = Modifier.height(AppTheme.padding.large))

    // Select/Deselect
    if (!state.allSelected)
        IconButton(
            imageVector = Icons.Outlined.SelectAll,
            onClick = state::selectAll
        )
    IconButton(
        imageVector = when (state.allFavourite) {
            1 -> Icons.Filled.Star
            0 -> Icons.Outlined.StarOutline
            else -> Icons.Outlined.StarHalf
        },
        onClick = state::toggleLike
    )

    val context = LocalContext.current

    // Delete
    IconButton(
        imageVector = Icons.Outlined.DeleteOutline,
        onClick = { state.remove(context.findActivity()) }
    )

    // Share
    IconButton(
        imageVector = Icons.Outlined.Share,
        onClick = { state.share(context.findActivity()) }
    )

    // close
    IconButton(
        imageVector = Icons.Outlined.Close,
        onClick = state::clear
    )
}

private val GridItemsArrangement = Arrangement.spacedBy(2.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Folder(
    viewState: FolderViewState
) {
    BackHandler(viewState.selected.isNotEmpty(), viewState::clear)
    val clazz = LocalWindowSize.current
    val behaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    // The top nav insets
    val navInsets = WindowInsets.contentInsets

    // The actual layout
    TwoPane(
        fabPosition = FabPosition.Center,
        modifier = Modifier
            .nestedScroll(behaviour.nestedScrollConnection),
        topBar = {
            AnimatedVisibility(
                visible = !viewState.isInSelectionMode,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.animateContentSize(),
                content = {
                    TopAppBar(
                        viewState,
                        behavior = behaviour,
                        insets = WindowInsets.statusBars,
                    )
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = viewState.isInSelectionMode,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.padding(navInsets),
                content = {
                    Actions(state = viewState)
                }
            )
        },
        content = {
            val values = viewState.data
            val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
            val navController = LocalNavController.current
            val selected = viewState.selected
            LazyVerticalGrid(
                columns = GridCells.Adaptive(Settings.STANDARD_TILE_SIZE * multiplier),
                horizontalArrangement = GridItemsArrangement,
                verticalArrangement = GridItemsArrangement,
                contentPadding = WindowInsets.contentInsets + navInsets + PaddingValues(vertical = AppTheme.padding.normal),
            ) {
                val data = emit(values) ?: return@LazyVerticalGrid
                items(data, key = { it.id }, tracker = viewState) { item ->
                    MediaFile(
                        value = item,
                        focused = false,
                        checked = when {
                            selected.isEmpty() -> -1
                            selected.contains(item.id) -> 1
                            else -> 0
                        },
                        modifier = Modifier.sharedElement(
                            key = RouteViewer.buildSharedFrameKey(item.id),
                        ) then Modifier.combinedClickable(
                            // onClick of item
                            onClick = {
                                if (selected.isNotEmpty())
                                    viewState.select(item.id)
                                else
                                    navController.navigate(RouteViewer(item.id, folder = viewState.path))
                            },
                            // onLong Click
                            onLongClick = { viewState.select(item.id) }
                        )
                    )
                }
            }
        }
    )
}