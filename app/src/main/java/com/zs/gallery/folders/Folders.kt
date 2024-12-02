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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.primex.core.plus
import com.primex.material2.Label
import com.primex.material2.appbar.TopAppBarDefaults
import com.zs.domain.store.Folder
import com.zs.foundation.AppTheme
import com.zs.foundation.ListHeader
import com.zs.foundation.None
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.contentInsets
import com.zs.foundation.stickyHeader
import com.zs.foundation.thenIf
import com.zs.gallery.R
import com.zs.gallery.bin.RouteTrash
import com.zs.gallery.common.Filters
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.Mapped
import com.zs.gallery.common.Regular
import com.zs.gallery.common.dynamicBackdrop
import com.zs.gallery.common.emit
import com.zs.gallery.common.fullLineSpan
import com.zs.gallery.common.preference
import com.zs.gallery.files.RouteAlbum
import com.zs.gallery.files.RouteFolder
import com.zs.gallery.settings.Settings
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.foundation.layout.PaddingValues as Padding
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient as VerticalGradient
import com.zs.foundation.ContentPadding as CP
import com.zs.gallery.common.rememberHazeState as BackdropProvider
import dev.chrisbanes.haze.haze as observerBackdrop

private const val TAG = "Folders"

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
                    Icon(
                        imageVector = Icons.Default.FolderCopy,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = CP.medium)
                    )
                },
                title = {
                    Label(
                        text = stringResource(id = R.string.folders),
                        style = AppTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                windowInsets = WindowInsets.None,
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

private val GRID_ITEM_SPACING = Arrangement.spacedBy(CP.small)
private fun LazyGridScope.content(
    navController: NavHostController,
    state: LazyGridState,
    data: Mapped<Folder>,
) {
    // First row represents shortcuts
    items(2, contentType = { "shortcut" }) { index ->
        when (index) {
            0 -> Shortcut(
                Icons.Outlined.HotelClass,
                stringResource(R.string.favourites),
                onClick = { navController.navigate(RouteAlbum()) }
            )

            1 -> Shortcut(
                Icons.Outlined.Recycling,
                stringResource(R.string.recycle_bin),
                onClick = { navController.navigate(RouteTrash()) },
                // Only enable if R and above
                enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            )
        }
    }

    for ((header, items) in data) {
        if (header.isNotBlank()) // only show this if non-blank.
            stickyHeader(
                state,
                header,
                contentType = "header",
                content = {
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 6.dp),
                        content = { ListHeader(header) }
                    )
                }
            )
        // rest of the items
        items(
            items,
            key = Folder::path,
            contentType = { "album" },
            itemContent = {
                Folder(
                    value = it,
                    modifier = Modifier
                        .clickable() { navController.navigate(RouteFolder(it.path)) }
                        //.sharedElement(RouteViewer.buildSharedFrameKey(it.artworkID))
                        .animateItem()
                )
            }
        )
    }
}

/**
 * The min size of the single cell in grid.
 */
private val MIN_TILE_SIZE = 100.dp
private val FolderContentPadding =
    Padding(horizontal = AppTheme.padding.large, AppTheme.padding.normal)
private val GridItemsArrangement = Arrangement.spacedBy(6.dp)

@Composable
fun Folders(viewState: FoldersViewState) {
    val navInsets = WindowInsets.contentInsets
    val observer = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> BackdropProvider()
        else -> null
    }

    // Actual Content
    TwoPane(
        topBar = {
            FloatingTopAppBar(
                insets = WindowInsets.statusBars,
                backdropProvider = observer
            )
        },
        content = {
            val values by viewState.data.collectAsState()
            val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
            val navController = LocalNavController.current
            val state = rememberLazyGridState()
            LazyVerticalGrid(
                state = state,
                columns = GridCells.Adaptive((MIN_TILE_SIZE * multiplier.coerceAtLeast(1f))),
                contentPadding = FolderContentPadding + WindowInsets.contentInsets + navInsets,
                verticalArrangement = GridItemsArrangement,
                horizontalArrangement = GridItemsArrangement,
                modifier = Modifier.thenIf(observer != null) { observerBackdrop(observer!!) },
                content = {
                    val data = emit(values) ?: return@LazyVerticalGrid
                    // only show other content; if data is avaiable.
                    // Filters: Display the filters section.
                    item(
                        "folder_filters",
                        contentType = "filters",
                        span = fullLineSpan,
                        content = {
                            Filters(
                                viewState.filter,
                                viewState.orders,
                                modifier = Modifier.padding(bottom = CP.medium),
                                onRequest = {
                                    when {
                                        it == null -> viewState.filter(!viewState.filter.first)
                                        else -> viewState.filter(order = it)
                                    }
                                }
                            )
                        }
                    )

                    // Rest of the content.
                    content(navController, state, data)
                }
            )
        }
    )
}