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

@file:SuppressLint("NewApi")
@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zs.gallery.bin

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.primex.core.findActivity
import com.primex.core.textResource
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.ListTile
import com.primex.material2.Text
import com.primex.material2.TextButton
import com.primex.material2.appbar.LargeTopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior
import com.zs.api.store.MediaFile
import com.zs.api.store.Trashed
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.LocalWindowSize
import com.zs.compose_ktx.None
import com.zs.compose_ktx.VerticalDivider
import com.zs.compose_ktx.sharedBounds
import com.zs.gallery.R
import com.zs.gallery.common.FabActionMenu
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.Placeholder
import com.zs.gallery.common.SelectionTracker
import com.zs.gallery.common.fullLineSpan
import com.zs.gallery.common.preference
import com.zs.gallery.files.DataProvider
import com.zs.gallery.files.FilesActionMenu
import com.zs.gallery.files.FolderViewState
import com.zs.gallery.files.GroupHeader
import com.zs.gallery.files.MediaFile
import com.zs.gallery.preview.RouteViewer
import com.zs.gallery.settings.Settings


@Composable
private fun TopAppBar(
    viewState: TrashViewState,
    modifier: Modifier = Modifier,
    behavior: TopAppBarScrollBehavior? = null,
    insets: WindowInsets = WindowInsets.None
) {
    AnimatedVisibility(
        visible = !viewState.isInSelectionMode,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        content = {
            LargeTopAppBar(
                navigationIcon = {
                    val navController = LocalNavController.current
                    IconButton(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = navController::navigateUp
                    )
                },
                title = { Label(text = textResource(id = R.string.trash)) },
                scrollBehavior = behavior,
                windowInsets = insets,
                modifier = modifier,
                style = TopAppBarDefaults.largeAppBarStyle(
                    containerColor = AppTheme.colors.background,
                    scrolledContainerColor = AppTheme.colors.background(elevation = 1.dp),
                    scrolledContentColor = AppTheme.colors.onBackground,
                    contentColor = AppTheme.colors.onBackground
                ),
                actions = {
                    val context = LocalContext.current
                    TextButton(
                        label = "Restore",
                        onClick = { viewState.restoreAll(context.findActivity()) })
                    TextButton(
                        label = "Empty",
                        onClick = { viewState.empty(context.findActivity()) })
                }
            )
        }
    )
}


@Composable
fun Actions(
    viewState: TrashViewState,
    modifier: Modifier = Modifier
) {
    FabActionMenu(modifier = modifier) {
        // Label
        Label(
            text = "${viewState.selected.size}",
            modifier = Modifier.padding(
                start = AppTheme.padding.normal,
                end = AppTheme.padding.medium
            ),
            style = AppTheme.typography.titleLarge
        )

        // Divider
        VerticalDivider(modifier = Modifier.height(AppTheme.padding.large))

        val context = LocalContext.current
        IconButton(
            imageVector = Icons.Default.Restore,
            onClick = { viewState.restore(context.findActivity()) })
        IconButton(
            imageVector = Icons.Default.DeleteSweep,
            onClick = { viewState.delete(context.findActivity()) })
    }
}

/**
 * The min size of the single cell in grid.
 */
private val MIN_TILE_SIZE = 100.dp
private val GridItemsArrangement = Arrangement.spacedBy(2.dp)

@Composable
fun LazyDataGrid(
    viewState: TrashViewState,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(vertical = AppTheme.padding.normal),
    itemContent: @Composable LazyGridItemScope.(value: Trashed) -> Unit
) {
    val values = viewState.data
    val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(MIN_TILE_SIZE * multiplier),
        horizontalArrangement = GridItemsArrangement,
        verticalArrangement = GridItemsArrangement,
        modifier = modifier,
        contentPadding = paddingValues
    ) {
        // null means loading
        val data = values
            ?: return@LazyVerticalGrid item(span = fullLineSpan, key = "key_loading_placeholder") {
                Placeholder(
                    title = stringResource(R.string.loading),
                    iconResId = R.raw.lt_loading_bubbles,
                    modifier = Modifier
                        .fillMaxSize()
                        .animateItem()
                )
            }
        // empty means empty
        if (data.isEmpty())
            return@LazyVerticalGrid item(
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
                }
            )

        item(
            span = fullLineSpan,
            key = "key_recycler_info",
            content = {
                ListTile(
                    leading = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                        )
                    },
                    headline = {
                        Text(text = "Photos and Videos You delete will be removed after 30 days.")
                    },
                    modifier = Modifier
                        .padding(horizontal = AppTheme.padding.normal)
                        .fillMaxWidth(),
                    shape = AppTheme.shapes.compact,
                    centerAlign = false
                )
            }
        )

        // place the actual items on the screen.
        data.forEach { (header, list) ->
            item(
                span = fullLineSpan,
                key = "key_header_$header",
                content = {
                    val state by remember(header) {
                        viewState.isGroupSelected(header)
                    }
                    GroupHeader(
                        label = header, state = state, {
                            viewState.select(header)
                        }
                    )
                }
            )

            items(list, key = { it.id }) { item ->
                itemContent(item)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Trash(viewState: TrashViewState) {
    BackHandler(viewState.selected.isNotEmpty(), viewState::clear)
    val clazz = LocalWindowSize.current
    val behaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val selected = viewState.selected
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            TopAppBar(
                viewState = viewState,
                behavior = behaviour,
                insets = WindowInsets.statusBars,
            )
        },
        modifier = Modifier
            .nestedScroll(behaviour.nestedScrollConnection),
        contentWindowInsets = WindowInsets.None,
        content = {
            LazyDataGrid(
                viewState = viewState,
                modifier = Modifier
                    .padding(it)
                    .animateContentSize(),
                itemContent = { item ->
                    TrashItem(
                        value = item,
                        checked = when {
                            selected.isEmpty() -> -1
                            selected.contains(item.id) -> 1
                            else -> 0
                        },
                        modifier = Modifier.sharedBounds(
                            key = RouteViewer.buildSharedFrameKey(item.id),
                        ) then Modifier.combinedClickable(
                            // onClick of item
                            onClick = {
                                if (selected.isNotEmpty())
                                    viewState.select(item.id)
                            },
                            // onLong Click
                            onLongClick = { viewState.select(item.id) }
                        )
                    )
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = viewState.isInSelectionMode,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                content = {
                    Actions(viewState = viewState)
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    )
}