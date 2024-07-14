/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 19-07-2024.
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zs.api.store.MediaFile
import com.zs.compose_ktx.AppTheme
import com.zs.gallery.R
import com.zs.gallery.common.Placeholder
import com.zs.gallery.common.SelectionTracker
import com.zs.gallery.common.fullLineSpan
import com.zs.gallery.common.preference
import com.zs.gallery.settings.Settings

private const val TAG = "FilesGrid"

/**
 * The min size of the single cell in grid.
 */
private val MIN_TILE_SIZE = 100.dp

private val GridItemsArrangement = Arrangement.spacedBy(2.dp)

@Composable
fun LazyDataGrid(
    provider: DataProvider,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(vertical = AppTheme.padding.normal),
    itemContent: @Composable LazyGridItemScope.(value: MediaFile) -> Unit
) {
    val values = provider.data
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

        // place the actual items on the screen.
        data.forEach { (header, list) ->
            item(
                span = fullLineSpan,
                key = "key_header_$header",
                content = {
                    val viewState = provider as SelectionTracker
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