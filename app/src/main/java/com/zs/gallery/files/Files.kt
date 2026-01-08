/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

package com.zs.gallery.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.zs.compose.foundation.plus
import com.zs.compose.foundation.stickyHeader
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.TonalIconButton
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.FabPosition
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.text.Text
import com.zs.compose.theme.text.TonalHeader
import com.zs.gallery.common.AppConfig
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.Res
import com.zs.gallery.common.compose.acrylic
import com.zs.gallery.common.compose.fadingEdge2
import com.zs.gallery.common.compose.rememberBackdropProvider
import com.zs.gallery.common.compose.shine
import com.zs.gallery.common.compose.source
import com.zs.gallery.common.runIf
import com.zs.gallery.common.vectorResource

private val HeaderPadding = PaddingValues(2.dp, 4.dp, 2.dp, 4.dp)

@Composable
fun Snapshot(modifier: Modifier = Modifier) {

}


@Composable
fun Files(viewState: FilesViewState) {
    // prioritise clearing of selection mode if back is pressed
//    BackHandler(
//        viewState.isInSelectionMode,
//        viewState::clear
//    )
    // The top nav insets
    val (width, _) = LocalWindowSize.current
    val compact = width < Category.Medium
    val inAppNavInsets = WindowInsets.content

    //
    val topAppBarScrollBehavior = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val provider = runIf(AppConfig.isBackgroundBlurEnabled) { rememberBackdropProvider() }
    val colors = AppTheme.colors
    // actions
    // val actions = viewState.actions
    val ctx = LocalContext.current
    Scaffold(
        fabPosition = if (compact) FabPosition.Center else FabPosition.End,
        topBar = {

            com.zs.gallery.common.compose.FloatingLargeTopAppBar(
                title = {
                    Text(
                        textResource(Res.string.scr_timeline_title),
                        maxLines = 2,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                background = colors.acrylic(provider),
                insets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
                scrollBehavior = topAppBarScrollBehavior,
                navigationIcon = {
                    IconButton(
                        icon = vectorResource(Res.drawable.ic_calendar_month_two_tone_outline),
                        onClick = {},
                        contentDescription = null
                    )
                },
                actions = {
                    IconButton(
                        icon = vectorResource(Res.drawable.ic_settings_outline),
                        onClick = {},
                        contentDescription = null
                    )
                }
            )


        },
        floatingActionButton = { Spacer(Modifier) },
        content = {
            val state = rememberLazyGridState()
            //val multiplier by preference(key = Res.key.)
            val navController = LocalNavController.current
            val source = viewState.files.collectAsLazyPagingItems()
            // Data
            val content: LazyGridScope.() -> Unit = content@{

                for (index in 0 until source.itemCount) {
                    val item = source[index] ?: continue
                    val header = item.header
                    if (header != null) {
                        stickyHeader(state = state, key = header, contentType = "header") {
                            Row(
                                Modifier.padding(HeaderPadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                content = {
                                    TonalHeader(header)
                                    // toggle
                                    TonalIconButton(
                                        icon = vectorResource(Res.drawable.ic_circle_outline),
                                        contentDescription = null,
                                        border = colors.shine,
                                        shape = AppTheme.shapes.small,
                                        //tint = /*if (level == SelectionTracker.Level.FULL) AppTheme.colors.accent*/  LocalContentColor.current,
                                        onClick = { /*viewState.select(header.toString()*/ }
                                    )
                                }
                            )
                        }
                    }

                    item(
                        key = item.id,
                        contentType = "media_file",
                        content = {
                            Snapshot(
                                data = item,
                                checked = 0 /*when {
                                    selected.isEmpty() -> -1
                                    selected.contains(item.id) -> 1
                                    else -> 0
                                }*/,
                                modifier = Modifier
                                    .animateItem()
                                //  .then(RouteFiles.sharedElement(item.id))
                                //  .then(clickable)
                            )
                        }
                    )

                }


            }

            // Content
            LazyVerticalGrid(
                state = state,
                columns = GridCells.Adaptive(
                    100.dp
                    /** multiplier*/
                ),
                horizontalArrangement = Res.dimen.spacing_x_small,
                verticalArrangement = Res.dimen.spacing_x_small,
                contentPadding = (inAppNavInsets.add(WindowInsets.content)
                    .union(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))).asPaddingValues() +
                        (PaddingValues(end = if (!compact) Res.dimen.large else 0.dp) + PaddingValues(
                            horizontal = Res.dimen.medium
                        )),
                modifier = Modifier
                    .fillMaxSize()
                    .fadingEdge2(length = 56.dp)
                    .source(provider)
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                content = content
            )
        }
    )
}