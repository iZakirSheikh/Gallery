/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 17-04-2025.
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

package com.zs.gallery.common.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.text.Label

/**
 * Item header.
 * //TODO: Handle padding in parent composable.
 */
private val HEADER_MARGIN = PaddingValues(0.dp, ContentPadding.normal, 0.dp, ContentPadding.medium)
private val HEADER_SHAPE = RoundedCornerShape(3, 50, 50, 50)
private val CHAR_HEADER_SHAPE = RoundedCornerShape(50, 25, 25, 25)

@Composable
@NonRestartableComposable
fun TonalCharHeader(
    text: CharSequence,
    modifier: Modifier = Modifier,
) = Label(
    text = text,
    style = AppTheme.typography.headline1,
    modifier = modifier
        .padding(HEADER_MARGIN)
        .border(0.5.dp, AppTheme.colors.background(30.dp), CHAR_HEADER_SHAPE)
        .background(AppTheme.colors.background(1.dp), CHAR_HEADER_SHAPE)
        .padding(horizontal = ContentPadding.large, vertical = ContentPadding.medium),
)

@Composable
@NonRestartableComposable
fun TonalHeader(
    text: CharSequence,
    modifier: Modifier = Modifier
) = Label(
    text = text,
    maxLines = 2,
    style = AppTheme.typography.title3,
    modifier = modifier
        .padding(HEADER_MARGIN)
        .widthIn(max = 220.dp)
        .border(0.5.dp, AppTheme.colors.background(30.dp), HEADER_SHAPE)
        .background(AppTheme.colors.background(1.dp), HEADER_SHAPE)
        .padding(horizontal = ContentPadding.normal, vertical = ContentPadding.small)
)