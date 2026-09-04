/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 14-09-2024.
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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.linearGradient
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Icon
import com.zs.compose.theme.Surface
import com.zs.compose.theme.ripple
import com.zs.compose.theme.text.Label
import com.zs.gallery.common.shapes.Folder as FolderShape


private val Folder = FolderShape(radius = 16.dp)

/**
 * Composable function to create a clickable shortcut with an icon and label.
 *
 * @param icon: The ImageVector representing the shortcut's icon.
 * @param label: The CharSequence representing the shortcut's label.
 * @param onAction: The action to perform when the shortcut is clicked.
 * @param modifier: Optional modifier to apply to the shortcut's layout.
 */
@Composable
fun Shortcut(
    icon: ImageVector,
    label: CharSequence,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Surface (
    shape = Folder,
    color = AppTheme.colors.background(4.dp),
    // border = BorderStroke(1.dp, AppTheme.colors.onBackground.copy(0.4f)),
    border = BorderStroke(
        1.dp,
        Brush.linearGradient(
            0.0f  to Color.White.copy(alpha = 0.55f),
            0.45f to Color.White.copy(alpha = 0.08f),
            0.55f to Color.Transparent,
            1.0f  to Color.Black.copy(alpha = 0.15f),
            angle =  0f
        )
    ),
    onClick = onClick,
    contentColor = AppTheme.colors.onBackground,
    modifier = modifier,
    content = {
        Column(
            modifier = Modifier.aspectRatio(1.35f).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            content = {
                Icon(// Icon at the top
                    imageVector = icon,
                    // Ensure a content description is provided elsewhere
                    contentDescription = null,
                )

                Label(// Label at the bottom
                    text = label,
                    style = AppTheme.typography.title3,
                )
            }
        )
    }
)
