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

import android.os.Build
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FabPosition
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.primex.core.findActivity
import com.primex.core.plus
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.appbar.TopAppBarDefaults
import com.zs.domain.store.MediaFile
import com.zs.foundation.AppTheme
import com.zs.foundation.ListHeader
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.None
import com.zs.foundation.VerticalDivider
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.contentInsets
import com.zs.foundation.menu.FloatingActionMenu
import com.zs.foundation.sharedBounds
import com.zs.foundation.stickyHeader
import com.zs.foundation.thenIf
import com.zs.foundation.toast.Toast
import com.zs.gallery.R
import com.zs.gallery.common.GroupSelectionLevel
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.MediaFile
import com.zs.gallery.common.Regular
import com.zs.gallery.common.SelectionTracker
import com.zs.gallery.common.dynamicBackdrop
import com.zs.gallery.common.emit
import com.zs.gallery.common.preference
import com.zs.gallery.settings.Settings
import com.zs.gallery.viewer.RouteViewer
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient as VerticalGradient
import com.primex.core.textResource as stringResource
import com.zs.foundation.ContentPadding as CP
import com.zs.gallery.common.rememberHazeState as BackdropProvider
import dev.chrisbanes.haze.haze as observerBackdrop

private const val TAG = "Timeline"

private val GridItemsArrangement = Arrangement.spacedBy(2.dp)
private val FloatingTopBarShape = RoundedCornerShape(20)

/**
 * Represents a Top app bar for this screen.
 */
@Composable
@NonRestartableComposable
private fun FloatingTopAppBar(
    modifier: Modifier = Modifier,
    backdropProvider: HazeState? = null,
    insets: WindowInsets = WindowInsets.None,
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                VerticalGradient(
                    listOf(
                        colors.background(1.dp),
                        colors.background.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            ),
        content = {
            com.primex.material2.appbar.TopAppBar(
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
                title = {
                    Label(
                        text = stringResource(id = R.string.timeline),
                        style = AppTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                windowInsets = WindowInsets.None,
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
                },
                style = TopAppBarDefaults.topAppBarStyle(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .widthIn(max = 550.dp)
                    .windowInsetsPadding(insets)
                    .padding(horizontal = CP.xLarge, vertical = CP.small)
                    .clip(FloatingTopBarShape)
                    .border(
                        0.5.dp,
                        VerticalGradient(
                            listOf(
                                if (colors.isLight) colors.background else Color.Gray.copy(
                                    0.24f
                                ),
                                if (colors.isLight) colors.background.copy(0.3f) else Color.Gray.copy(
                                    0.075f
                                ),
                            )
                        ),
                        FloatingTopBarShape
                    )
                    .dynamicBackdrop(
                        backdropProvider,
                        HazeStyle.Regular(colors.background),
                        colors.background,
                        colors.accent
                    )
            )
        }
    )
}

@Composable
private fun MainActions(
    visible: Boolean,
    state: TimelineViewState,
    backdropProvider: HazeState? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier,
        content = {
            val colors = AppTheme.colors
            FloatingActionMenu(
                modifier = Modifier
                    .border(
                        0.5.dp,
                        VerticalGradient(
                            listOf(
                                if (colors.isLight) colors.background else Color.Gray.copy(
                                    0.24f
                                ),
                                if (colors.isLight) colors.background.copy(0.3f) else Color.Gray.copy(
                                    0.075f
                                ),
                            )
                        ),
                        CircleShape
                    )
                    .dynamicBackdrop(
                        backdropProvider,
                        HazeStyle.Regular(colors.background),
                        colors.background,
                        colors.accent
                    ),
                color = Color.Transparent
            ) {
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
        }
    )
}


private fun LazyGridScope.content(
    data: Map<String, List<MediaFile>>,
    state: LazyGridState,
    tracker: SelectionTracker,
    navController: NavHostController,
) {
    // if non-null data is returned
    for ((header, children) in data) {
        stickyHeader(
            state = state,
            key = header,
            contentType = "header",
            content = {
                ListHeader(
                    header,
                    trailing = {
                        val state by tracker.isGroupSelected(header)
                        IconButton(
                            imageVector = when (state) {
                                GroupSelectionLevel.NONE -> Icons.Outlined.Circle
                                GroupSelectionLevel.PARTIAL -> Icons.Outlined.RemoveCircle
                                GroupSelectionLevel.FULL -> Icons.Filled.CheckCircle
                            },
                            tint = if (state == GroupSelectionLevel.FULL) AppTheme.colors.accent else LocalContentColor.current,
                            onClick = { tracker.select(header) }
                        )
                    }
                )
            }
        )
        // now children
        val selected = tracker.selected
        for (child in children) {
            item(key = child.id, contentType = "item") {
                MediaFile(
                    value = child,
                    focused = false,
                    checked = when {
                        selected.isEmpty() -> -1
                        selected.contains(child.id) -> 1
                        else -> 0
                    },
                    modifier = Modifier
                        .sharedBounds(RouteViewer.buildSharedFrameKey(child.id))
                        .combinedClickable(
                            // onClick of item
                            onClick = {
                                if (selected.isNotEmpty())
                                    tracker.select(child.id)
                                else
                                    navController.navigate(RouteViewer(child.id))
                            },
                            // onLong Click
                            onLongClick = { tracker.select(child.id) }
                        )
                )
            }
        }
    }
}

@Composable
fun Timeline(
    viewState: TimelineViewState,
) {
    // Handle back
    BackHandler(
        viewState.selected.isNotEmpty(),
        viewState::clear
    )
    // The top nav insets
    val inAppNavInsets = WindowInsets.contentInsets
    val clazz = LocalWindowSize.current
    val portrait = clazz.widthRange < clazz.heightRange
    //
    val observer = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> BackdropProvider()
        else -> null
    }
    // The actual layout
    TwoPane(
        fabPosition = if (portrait) FabPosition.Center else FabPosition.End,
        topBar = {
            FloatingTopAppBar(
                insets = WindowInsets.statusBars,
                backdropProvider = observer
            )
        },
        floatingActionButton = {
            MainActions(
                viewState.isInSelectionMode,
                modifier = when {
                    portrait -> Modifier.padding(inAppNavInsets)
                    else -> Modifier.navigationBarsPadding()
                },
                state = viewState,
                backdropProvider = observer,
            )
        },
        content = {
            val values = viewState.data
            val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
            val navController = LocalNavController.current
            val lazyGridState = rememberLazyGridState()

            // content
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive(Settings.STANDARD_TILE_SIZE * multiplier),
                horizontalArrangement = GridItemsArrangement,
                verticalArrangement = GridItemsArrangement,
                contentPadding = inAppNavInsets + WindowInsets.contentInsets + PaddingValues(end = if (!portrait) CP.large else 0.dp) + PaddingValues(horizontal = CP.medium),
                modifier = Modifier.thenIf(observer != null) { observerBackdrop(observer!!) }
            ) {
                // return data or emit state
                val data = emit(values) ?: return@LazyVerticalGrid
                // if non-null data is returned
                content(data, lazyGridState, viewState, navController)
            }
        }
    )
}