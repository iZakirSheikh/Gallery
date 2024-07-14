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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.primex.core.findActivity
import com.primex.core.textResource
import com.primex.material2.DropDownMenuItem
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.appbar.TopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior
import com.primex.material2.menu.DropDownMenu2
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.LocalWindowSize
import com.zs.compose_ktx.None
import com.zs.compose_ktx.Range
import com.zs.compose_ktx.VerticalDivider
import com.zs.compose_ktx.sharedBounds
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.SelectionTracker
import com.zs.gallery.preview.RouteViewer

@Composable
fun ActionMenu(
    state: TimelineViewState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.scale(0.85f),
        color = AppTheme.colors.background(elevation = 1.dp),
        contentColor = AppTheme.colors.onBackground,
        shape = CircleShape,
        border = BorderStroke(1.dp, AppTheme.colors.background(elevation = 4.dp)),
        elevation = 12.dp,
        content = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.padding.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.animateContentSize(), content = {
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
                        imageVector = Icons.Outlined.FavoriteBorder,
                        onClick = state::addToFavourite
                    )

                    val context = LocalContext.current

                    // Delete
                    IconButton(
                        imageVector = Icons.Outlined.DeleteOutline,
                        onClick = { state.delete(context.findActivity()) }
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
            )
        }
    )
}

@Composable
private fun TopAppBar(
    tracker: SelectionTracker,
    modifier: Modifier = Modifier,
    behavior: TopAppBarScrollBehavior? = null,
    insets: WindowInsets = WindowInsets.None
) {
    AnimatedVisibility(
        visible = !tracker.isInSelectionMode,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        content = {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_app),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.padding(AppTheme.padding.normal)
                    )
                },
                title = { Label(text = "Gallery") },
                scrollBehavior = behavior,
                windowInsets = insets,
                modifier = modifier,
                style = TopAppBarDefaults.topAppBarStyle(
                    containerColor = AppTheme.colors.background,
                    scrolledContainerColor = AppTheme.colors.background(elevation = 1.dp),
                    scrolledContentColor = AppTheme.colors.onBackground,
                    contentColor = AppTheme.colors.onBackground
                ),
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    androidx.compose.material.IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        DropDownMenu2(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            // Bin
                            DropDownMenuItem(title = textResource(id = R.string.recycle_bin),
                                icon = rememberVectorPainter(
                                    image = Icons.Outlined.Recycling
                                ),
                                onClick = { /*TODO*/ })

                            // Favourite
                            DropDownMenuItem(title = textResource(id = R.string.favourites),
                                icon = rememberVectorPainter(
                                    image = Icons.Outlined.Favorite
                                ),
                                onClick = { /*TODO*/ })
                        }
                    }
                },
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Timeline(
    viewState: TimelineViewState
) {
    BackHandler(viewState.selected.isNotEmpty(), viewState::clear)
    val clazz = LocalWindowSize.current
    val behaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val selected = viewState.selected
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            TopAppBar(
                tracker = viewState,
                behavior = behaviour,
                insets = WindowInsets.statusBars,
            )
        },
        modifier = Modifier.nestedScroll(behaviour.nestedScrollConnection).animateContentSize(),
        contentWindowInsets = WindowInsets.None,
        content = {
            LazyDataGrid(
                provider = viewState,
                modifier = Modifier.padding(it),
                itemContent = { item ->
                    MediaFile(
                        value = item,
                        focused = false,
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
                                else
                                    navController.navigate(viewState.buildViewerRoute(item.id))
                            },
                            // onLong Click
                            onLongClick = { viewState.select(item.id) }
                        )
                    )
                }
            )
        },
        floatingActionButton = { if (viewState.isInSelectionMode) ActionMenu(state = viewState) },
        floatingActionButtonPosition = FabPosition.Center
    )
}
