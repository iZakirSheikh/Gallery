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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.primex.core.textResource
import com.primex.material2.DropDownMenuItem
import com.primex.material2.Label
import com.primex.material2.Text
import com.primex.material2.appbar.LargeTopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior
import com.primex.material2.menu.DropDownMenu2
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.LocalWindowSize
import com.zs.compose_ktx.None
import com.zs.compose_ktx.sharedBounds
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.preview.RouteViewer


@Composable
private fun TopAppBar(
    viewState: FolderViewState,
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
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun Folder(viewState: FolderViewState) {
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
            .nestedScroll(behaviour.nestedScrollConnection)
            .animateContentSize(),
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
                                    navController.navigate(RouteViewer(item.id, folder = item.path))
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
                    FilesActionMenu(state = viewState)
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    )
}