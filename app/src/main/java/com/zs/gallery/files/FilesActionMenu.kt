/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 25-07-2024.
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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.primex.core.findActivity
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.VerticalDivider
import com.zs.gallery.common.FabActionMenu


@Composable
fun FilesActionMenu(
    state: TimelineViewState,
    modifier: Modifier = Modifier
) {
    FabActionMenu(modifier = modifier) {
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
}