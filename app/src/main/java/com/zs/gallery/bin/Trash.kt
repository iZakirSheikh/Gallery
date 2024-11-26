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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FabPosition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.primex.core.findActivity
import com.primex.core.plus
import com.primex.core.textResource
import com.primex.material2.Button
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.OutlinedButton
import com.primex.material2.TextButton
import com.primex.material2.appbar.LargeTopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior
import com.zs.foundation.AppTheme
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.None
import com.zs.foundation.VerticalDivider
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.VerticalTwoPaneStrategy
import com.zs.foundation.adaptive.contentInsets
import com.zs.gallery.R
import com.zs.foundation.menu.FloatingActionMenu
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.emit
import com.zs.gallery.common.items
import com.zs.gallery.common.preference
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
        modifier = Modifier.animateContentSize(),
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
                    Button(
                        label = "Restore",
                        onClick = { viewState.restoreAll(context.findActivity()) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.background(2.dp)),
                        shape = CircleShape,
                        elevation = null,
                        modifier = Modifier.scale(0.9f)
                    )
                    Button(
                        label = "Empty Bin",
                        onClick = { viewState.empty(context.findActivity()) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.background(2.dp)),
                        shape = CircleShape,
                        elevation = null,
                        modifier = Modifier.scale(0.9f)
                    )
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
    FloatingActionMenu(modifier = modifier) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Trash(viewState: TrashViewState) {
    BackHandler(viewState.selected.isNotEmpty(), viewState::clear)
    val behaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val clazz = LocalWindowSize.current
    val navInsets = WindowInsets.contentInsets

    TwoPane(
        fabPosition = FabPosition.Center,
        modifier = Modifier.nestedScroll(behaviour.nestedScrollConnection),
        topBar = {
            TopAppBar(
                viewState = viewState,
                behavior = behaviour,
                insets = WindowInsets.statusBars,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = viewState.isInSelectionMode,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.padding(navInsets),
                content = {
                    Actions(viewState = viewState)
                }
            )
        },
        strategy = VerticalTwoPaneStrategy(0.5f),
        content = {
            val values = viewState.data
            val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
            val selected = viewState.selected
            LazyVerticalGrid(
                columns = GridCells.Adaptive(MIN_TILE_SIZE * multiplier),
                horizontalArrangement = GridItemsArrangement,
                verticalArrangement = GridItemsArrangement,
                contentPadding = WindowInsets.contentInsets + navInsets,
                content = {
                    // emit the state;
                    val data = emit(values) ?: return@LazyVerticalGrid

                    // if data is non-null place it.
                    items(
                        data = data,
                        tracker = viewState,
                        key = { it.id },
                        itemContent = { item ->
                            // handle interaction for this item.
                            // TODO - Add preview for this item
                            val onClick = {
                                if (selected.isNotEmpty())
                                    viewState.select(item.id)
                            }
                            val onLongClick = { viewState.select(item.id) }
                            // The Item
                            TrashItem(
                                value = item,
                                checked = when {
                                    selected.isEmpty() -> -1
                                    selected.contains(item.id) -> 1
                                    else -> 0
                                },
                                modifier = Modifier.combinedClickable(
                                    onClick = onClick,
                                    onLongClick = onLongClick
                                )
                            )
                        }
                    )
                }
            )
        },
    )
}