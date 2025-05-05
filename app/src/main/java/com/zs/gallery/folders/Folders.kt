/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 04-04-2025.
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

package com.zs.gallery.folders

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.fullLineSpan
import com.zs.compose.foundation.plus
import com.zs.compose.foundation.stickyHeader
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Icon
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.contentInsets
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.minimumInteractiveComponentSize
import com.zs.compose.theme.text.Header
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.TonalHeader
import com.zs.core.store.Folder
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.compose.Filters
import com.zs.gallery.common.compose.GalleryTopAppBar
import com.zs.gallery.common.compose.background
import com.zs.gallery.common.compose.emit
import com.zs.gallery.common.compose.fadingEdge2
import com.zs.gallery.common.compose.observe
import com.zs.gallery.common.compose.rememberBackgroundProvider
import com.zs.gallery.common.preference
import com.zs.gallery.files.RouteBin
import com.zs.gallery.files.RouteFiles
import com.zs.gallery.files.RouteLiked
import com.zs.gallery.settings.Settings
import com.zs.gallery.common.compose.ContentPadding as CP

private val TileArrangement = Arrangement.spacedBy(4.dp)

/**
 * The min size of the single cell in grid.
 */
private val DEF_MIN_TILE_SIZE = 100.dp

@Composable
fun Folders(viewState: FoldersViewState) {
    // The top nav insets
    val inAppNavInsets = WindowInsets.contentInsets
    val clazz = LocalWindowSize.current
    val portrait = clazz.width < clazz.height
    //
    // Define the scroll behavior for the top app bar
    val topAppBarScrollBehavior = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val observer = rememberBackgroundProvider()
    val navController = LocalNavController.current
    // Actual composable
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            GalleryTopAppBar(
                immersive = false,
                title = { Label(stringResource(R.string.folders)) },
                backdrop = colors.background(observer),
                behavior = topAppBarScrollBehavior,
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Outlined.FolderCopy,
                        null,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                },
            )
        },
        content = {
            val data by viewState.data.collectAsState()
            val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
            val lazyGridState = rememberLazyGridState()

            //
            val content: LazyGridScope.() -> Unit = content@{
                // emit state or return non-null | non empty data points
                val data = emit(data) ?: return@content

                // only show other content; if data is available.
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

                // Shortcut Row
                // First row represents shortcuts
                item(contentType = "shortcut") {
                    Shortcut(
                        Icons.Outlined.HotelClass,
                        stringResource(R.string.favourites),
                        onClick = { navController.navigate(RouteLiked()) }
                    )
                }
                // Only available if R and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    item(contentType = "shortcut") {
                        Shortcut(
                            Icons.Outlined.Recycling,
                            stringResource(R.string.recycle_bin),
                            onClick = { navController.navigate(RouteBin()) },
                        )
                    }

                // Actual content
                for ((header, items) in data) {
                    if (header.isNotEmpty())
                        stickyHeader(
                            lazyGridState,
                            header,
                            contentType = "header",
                            content = {
                                Box(
                                    modifier = Modifier
                                        .animateItem()
                                        .padding(horizontal = 6.dp),
                                    content = {
                                        TonalHeader(header)
                                    }
                                )
                            }
                        )
                    else
                        item(contentType = "group-space", span = fullLineSpan) {
                            Header(
                                stringResource(R.string.folders),
                                Modifier.padding(horizontal = 6.dp, vertical = CP.normal),
                                color = AppTheme.colors.accent,
                                style = AppTheme.typography.label2
                            )
                        }

                    // rest of the items
                    items(
                        items,
                        key = Folder::path,
                        contentType = { "album" },
                        itemContent = {
                            Folder(
                                value = it,
                                modifier = Modifier
                                    .clickable() { navController.navigate(RouteFiles(it.path)) }
                                    .animateItem()
                            )
                        }
                    )

                    // space
                    item(span = fullLineSpan, contentType = "group-space") {
                        Spacer(modifier = Modifier.padding(top = CP.large))
                    }
                }
            }

            // The complete screen insets.
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive(DEF_MIN_TILE_SIZE * multiplier.coerceAtLeast(0.9f)),
                horizontalArrangement = TileArrangement,
                verticalArrangement = TileArrangement,
                contentPadding = inAppNavInsets + WindowInsets.contentInsets +
                        PaddingValues(end = if (!portrait) CP.large else 0.dp) +
                        PaddingValues(horizontal = CP.medium),
                modifier = Modifier
                    .fadingEdge2(
                        listOf(
                            colors.background(1.dp),
                            colors.background.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        length = 56.dp
                    )
                    .observe(observer)
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                content = content
            )
        }
    )
}

