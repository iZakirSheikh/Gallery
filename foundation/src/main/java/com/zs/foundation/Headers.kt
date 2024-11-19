/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 19-11-2024.
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

package com.zs.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.primex.material2.Label
import androidx.compose.foundation.layout.PaddingValues as Padding
import com.zs.foundation.ContentPadding as CP

/**
 * Item header.
 * //TODO: Handle padding in parent composable.
 */
private val HEADER_MARGIN = Padding(CP.medium, CP.large, CP.medium, CP.normal)
private val CHAR_HEADER_SHAPE = RoundedCornerShape(50, 25, 25, 25)

/**
 * Represents header for list/grid item groups.
 * Displays a single-character header as a circular "dew drop" or a multi-character header in a
 * two-line circular shape with a Material 3 background and subtle border.
 *
 * @param value The header text.
 * @param modifier The [Modifier] to apply.
 */
@NonRestartableComposable
@Composable
fun ListHeader(
    value: CharSequence,
    modifier: Modifier = Modifier
) {
    when {
        // If the value has only one character, display it as a circular header.
        // Limit the width of the circular header
        value.length == 1 -> Label(
            text = value,
            style = AppTheme.typography.headlineLarge,
            modifier = modifier
                .padding(HEADER_MARGIN)
                .border(0.5.dp, Color.Gray.copy(0.12f), CHAR_HEADER_SHAPE)
                .background(AppTheme.colors.background(1.dp), CHAR_HEADER_SHAPE)
                .padding(horizontal = CP.large, vertical = CP.medium),
        )
        // If the value has more than one character, display it as a label.
        // Limit the label to a maximum of two lines
        // Limit the width of the label
        else -> Label(
            text = value,
            maxLines = 2,
            fontWeight = FontWeight.Normal,
            style = AppTheme.typography.titleSmall,
            modifier = modifier
                .padding(HEADER_MARGIN)
                .widthIn(max = 220.dp)
                .border(0.5.dp, Color.Gray.copy(0.12f), CircleShape)
                .background(AppTheme.colors.background(1.dp), CircleShape)
                .padding(horizontal = CP.normal, vertical = CP.small)
        )
    }
}

@NonRestartableComposable
@Composable
fun ListHeader(
    value: CharSequence,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit,
)  {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        when {
            // If the value has only one character, display it as a circular header.
            // Limit the width of the circular header
            value.length == 1 -> Label(
                text = value,
                style = AppTheme.typography.headlineLarge,
                modifier = modifier
                    .padding(HEADER_MARGIN)
                    .border(0.5.dp, AppTheme.colors.background(5.dp), CHAR_HEADER_SHAPE)
                    .background(AppTheme.colors.background(1.dp), CHAR_HEADER_SHAPE)
                    .padding(horizontal = CP.large, vertical = CP.medium),
            )
            // If the value has more than one character, display it as a label.
            // Limit the label to a maximum of two lines
            // Limit the width of the label
            else -> Label(
                text = value,
                maxLines = 2,
                fontWeight = FontWeight.Normal,
                style = AppTheme.typography.titleSmall,
                modifier = modifier
                    .padding(HEADER_MARGIN)
                    .widthIn(max = 220.dp)
                    .border(0.5.dp, AppTheme.colors.background(5.dp), CircleShape)
                    .background(AppTheme.colors.background(1.dp), CircleShape)
                    .padding(horizontal = CP.normal, vertical = CP.medium)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        trailing()
    }
}