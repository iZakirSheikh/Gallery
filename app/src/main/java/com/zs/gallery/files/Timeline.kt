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

@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class)

package com.zs.gallery.files

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FabPosition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.primex.core.findActivity
import com.primex.core.plus
import com.primex.core.textResource
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.Text
import com.primex.material2.appbar.LargeTopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior
import com.zs.foundation.AppTheme
import com.zs.foundation.ContentPadding
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.None
import com.zs.foundation.VerticalDivider
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.contentInsets
import com.zs.foundation.menu.FloatingActionMenu
import com.zs.foundation.renderInSharedTransitionScopeOverlay
import com.zs.foundation.sharedBounds
import com.zs.foundation.thenIf
import com.zs.foundation.toast.Toast
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.MediaFile
import com.zs.gallery.common.emit
import com.zs.gallery.common.items
import com.zs.gallery.common.mist
import com.zs.gallery.common.observerBackdrop
import com.zs.gallery.common.preference
import com.zs.gallery.common.regular
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.RouteViewer
import dev.chrisbanes.haze.HazeStyle
import com.zs.gallery.common.rememberBackdropProvider as BackdropProvider

private val GridItemsArrangement = Arrangement.spacedBy(2.dp)
private val FloatingTopBarShape = RoundedCornerShape(20)


/**
 * Represents a Top app bar for this screen.
 */
@Composable
@NonRestartableComposable
private fun FloatingTopAppBar(
    modifier: Modifier = Modifier,
    insets: WindowInsets = WindowInsets.None,
    behaviour: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        navigationIcon = {
            val facade = LocalSystemFacade.current
            IconButton(
                painter = painterResource(id = R.drawable.ic_app),
                contentDescription = null,
                tint = null,
                onClick = {
                    facade.showToast(
                        R.string.what_s_new_latest,
                        Icons.Outlined.NewReleases,
                        priority = Toast.PRIORITY_HIGH
                    )
                }
            )
        },
        title = { Text(text = textResource(id = R.string.timeline)) },
        scrollBehavior = behaviour,
        windowInsets = WindowInsets.None,
        modifier = Modifier
            // Adds a mist to the background of statusBar
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppTheme.colors.background(1.dp),
                        AppTheme.colors.background.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
            .windowInsetsPadding(insets)
            .padding(horizontal = ContentPadding.large)
            // Add a border around the topBar
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppTheme.colors.background,
                        AppTheme.colors.background.copy(alpha = 0.3f),
                    )
                ),
                shape = FloatingTopBarShape
            )
            .clip(FloatingTopBarShape)
            .then(modifier)
            .widthIn(max = 400.dp),
        style = TopAppBarDefaults.largeAppBarStyle(
            scrolledContainerColor = Color.Transparent,
            containerColor = AppTheme.colors.background,
            scrolledContentColor = AppTheme.colors.onBackground,
            contentColor = AppTheme.colors.onBackground
        ),
        actions = {
            val facade = LocalSystemFacade.current
            IconButton(
                Icons.Outlined.HotelClass,
                onClick = facade::launchAppStore
            )
            IconButton(
                Icons.Outlined.Share,
                onClick = { facade.launch(Settings.ShareAppIntent) }
            )
            IconButton(
                Icons.Outlined.BugReport,
                onClick = { facade.launch(Settings.GitHubIssuesPage) }
            )
            IconButton(
                Icons.Outlined.Chat,
                onClick = { facade.launch(Settings.TelegramIntent) }
            )
        }
    )
}

@Composable
private fun MainActions(
    state: TimelineViewState,
    modifier: Modifier = Modifier
) = FloatingActionMenu(modifier = modifier, color = Color.Transparent) {
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

    // Favourite
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

@Composable
private fun MainContent(
    viewState: TimelineViewState,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val values = viewState.data
    val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
    val selected = viewState.selected
    val navController = LocalNavController.current
    // content
    LazyVerticalGrid(
        columns = GridCells.Adaptive(Settings.STANDARD_TILE_SIZE * multiplier),
        horizontalArrangement = GridItemsArrangement,
        verticalArrangement = GridItemsArrangement,
        contentPadding = padding,
        modifier = modifier
    ) {
        // return data or emit state
        val data = emit(values) ?: return@LazyVerticalGrid
        // if non-null data is returned
        items(
            data = data,
            key = com.zs.domain.store.MediaFile::id,
            tracker = viewState,
            itemContent = { item ->
                MediaFile(
                    value = item,
                    focused = false,
                    checked = when {
                        selected.isEmpty() -> -1
                        selected.contains(item.id) -> 1
                        else -> 0
                    },
                    modifier = Modifier
                        .sharedBounds(RouteViewer.buildSharedFrameKey(item.id))
                        .combinedClickable(
                            // onClick of item
                            onClick = {
                                if (selected.isNotEmpty())
                                    viewState.select(item.id)
                                else
                                    navController.navigate(RouteViewer(item.id))
                            },
                            // onLong Click
                            onLongClick = { viewState.select(item.id) }
                        )
                )
            }
        )
    }
}

@Composable
fun Timeline(
    viewState: TimelineViewState
) {
    // Handle back
    BackHandler(
        viewState.selected.isNotEmpty(),
        viewState::clear
    )


    // The top nav insets
    val navInsets = WindowInsets.contentInsets
    val clazz = LocalWindowSize.current
    val portrait = clazz.widthRange < clazz.heightRange

    // Properties
    val provider = BackdropProvider()
    val behaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // The actual layout
    TwoPane(
        fabPosition = FabPosition.Center,
        modifier = Modifier.nestedScroll(behaviour.nestedScrollConnection),
        topBar = {
            FloatingTopAppBar(
                behaviour = behaviour,
                insets = WindowInsets.statusBars,
                modifier = Modifier
                    .mist(provider, HazeStyle.regular())
                    .renderInSharedTransitionScopeOverlay(0.12f)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = viewState.isInSelectionMode,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .padding(navInsets)
                    .thenIf(!portrait) { navigationBarsPadding() },
                content = {
                    MainActions(
                        state = viewState,
                        modifier = Modifier.mist(provider, HazeStyle.regular())
                    )
                }
            )
        },
        content = {
            MainContent(
                viewState = viewState,
                padding = navInsets + PaddingValues(vertical = AppTheme.padding.normal) + WindowInsets.contentInsets + if (!portrait) {
                    PaddingValues(end = ContentPadding.large)
                } else PaddingValues(0.dp),
                modifier = Modifier.observerBackdrop(provider)
            )
        }
    )
}