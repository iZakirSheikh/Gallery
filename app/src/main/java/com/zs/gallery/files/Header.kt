/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 18-07-2024.
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.zs.compose_ktx.AppTheme
import com.zs.gallery.common.GroupSelectionLevel

/**
 * Item header.
 * //TODO: Handle padding in parent composable.
 */
@Composable
fun GroupHeader(
    label: CharSequence,
    state: GroupSelectionLevel,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Label(
            text = label,
            color = LocalContentColor.current,
            maxLines = 2,
            fontWeight = FontWeight.Normal,
            style = AppTheme.typography.titleLarge,
            modifier = modifier
                .padding(start = AppTheme.padding.normal)
                .padding(vertical = AppTheme.padding.xLarge),
        )

        IconButton(
            imageVector = when(state){
                GroupSelectionLevel.NONE -> Icons.Outlined.Circle
                GroupSelectionLevel.PARTIAL -> Icons.Outlined.RemoveCircle
                GroupSelectionLevel.FULL -> Icons.Outlined.CheckCircleOutline
            },
            tint = if (state == GroupSelectionLevel.FULL) AppTheme.colors.accent else LocalContentColor.current,
            onClick = onToggle
        )
    }
}
