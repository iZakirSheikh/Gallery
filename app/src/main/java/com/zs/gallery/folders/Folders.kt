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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.fullLineSpan
import com.zs.compose.foundation.plus
import com.zs.compose.foundation.stickyHeader
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Icon
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.minimumInteractiveComponentSize
import com.zs.compose.theme.text.Header
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.TonalHeader
import com.zs.core.store.Folder
import com.zs.gallery.R
import com.zs.gallery.common.compose.Filters
import com.zs.gallery.common.compose.FloatingLargeTopAppBar
import com.zs.gallery.common.compose.LocalNavController
import com.zs.gallery.common.compose.background
import com.zs.gallery.common.compose.emit
import com.zs.gallery.common.compose.fadingEdge2
import com.zs.gallery.common.compose.preference
import com.zs.gallery.common.compose.rememberAcrylicSurface
import com.zs.gallery.common.compose.section
import com.zs.gallery.common.compose.source
import com.zs.gallery.files.RouteBin
import com.zs.gallery.files.RouteFiles
import com.zs.gallery.files.RouteFolder
import com.zs.gallery.files.RouteLiked
import com.zs.gallery.settings.Settings
import androidx.compose.foundation.layout.PaddingValues as Padding
import com.zs.gallery.common.compose.ContentPadding as CP

/**
 * The min size of the single cell in grid.
 */
private val DEF_MIN_TILE_SIZE = 100.dp

@Composable
fun Folders(viewState: FoldersViewState) {
    // The top nav insets
    val inAppNavInsets = WindowInsets.content
    val compact = LocalWindowSize.current.width < Category.Medium
    //
    // Define the scroll behavior for the top app bar
    val topAppBarScrollBehavior = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val surface = rememberAcrylicSurface()
    val navController = LocalNavController.current
    // Actual composable
    val colors = AppTheme.colors
    //
    Scaffold(
        topBar = {
            FloatingLargeTopAppBar(
                title = { Label(stringResource(R.string.folders)) },
                background = colors.background(surface),
                scrollBehavior = topAppBarScrollBehavior,
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Outlined.FolderCopy,
                        null,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                },
                insets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
            )
        },
        //
        content = {
            val data by viewState.data.collectAsState()
            val multiplier by preference(key = Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
            val state = rememberLazyGridState()
            //
            val content: LazyGridScope.() -> Unit = content@{
                /// Shortcut Row
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
                // Local Devices
                item(contentType = "simple_header", span = fullLineSpan) {
                    Header(
                        "On this device",
                        Modifier.padding(horizontal = 6.dp, vertical = CP.normal),
                        color = AppTheme.colors.accent,
                        style = AppTheme.typography.label2
                    )
                }
                // show from here only if available.
                // else emit error | empty | Loading
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
                // Actual content
                for ((header, items) in data) {
                    if (header.isNotEmpty())
                        stickyHeader(
                            state,
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
                    // rest of the items
                    items(
                        items,
                        key = Folder::path,
                        contentType = { "folder" },
                        itemContent = {
                            Folder(
                                value = it,
                                modifier = Modifier
                                    .animateItem()
                                    .clickable() { navController.navigate(RouteFolder(it.path)) }
                            )
                        }
                    )

                    // Marks the end of the section.
                    section()
                }
            }
            // Content
            LazyVerticalGrid(
                state = state,
                columns = GridCells.Adaptive(DEF_MIN_TILE_SIZE * multiplier),
                horizontalArrangement = CP.SmallArrangement,
                verticalArrangement = CP.SmallArrangement,
                contentPadding = (inAppNavInsets.add(WindowInsets.content)
                    .union(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))).asPaddingValues() +
                        (Padding(end = if (!compact) CP.large else 0.dp) + Padding(horizontal = CP.medium)),
                modifier = Modifier
                    .fillMaxSize()
                    .fadingEdge2(length = 56.dp)
                    .source(surface)
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                content = content
            )
        }
    )
}