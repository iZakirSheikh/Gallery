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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Icon
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
    enabled: Boolean = true,
) {
    // Base container for the shortcut with styling and click handling
    val colors = AppTheme.colors
    val accent = colors.onBackground
    Box(
        modifier = modifier
            .aspectRatio(16 / 11f)
            .clip(Folder) // Shape the shortcut like a folder
            .background(colors.background(1.5.dp), Folder)
            // .border(1.dp, accent.copy(0.5f), Folder) // Light border
            //  .background(colors.backgroundColorAtElevation(0.4.dp), FolderShape)
            .clickable(
                null,
                ripple(true, color = AppTheme.colors.accent), // Ripple effect on click
                role = Role.Button, // Semantically indicate a button
                onClick = onClick, // Trigger the action on click
                enabled = enabled
            )
            .padding(horizontal = 12.dp, vertical = 8.dp) // Add internal padding
        // then modifier // Apply additional modifiers
    ) {
        // Icon at the top
        Icon(
            imageVector = icon,
            contentDescription = null, // Ensure a content description is provided elsewhere
            tint = accent,
            modifier = Modifier.align(AbsoluteAlignment.TopLeft)
        )

        // Label at the bottom
        Label(
            text = label,
            style = AppTheme.typography.label3,
            color = accent,
            modifier = Modifier.align(AbsoluteAlignment.BottomLeft)
        )
    }
}
