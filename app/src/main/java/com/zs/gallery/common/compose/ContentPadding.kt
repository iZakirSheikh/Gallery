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

package com.zs.gallery.common.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zs.gallery.common.compose.ContentPadding.large
import com.zs.gallery.common.compose.ContentPadding.medium
import com.zs.gallery.common.compose.ContentPadding.normal
import com.zs.gallery.common.compose.ContentPadding.small
import com.zs.gallery.common.compose.ContentPadding.xLarge

/**
 * Provides a setof standard content padding values to ensure consistency across the application.
 *
 * @property small A small 4 [Dp] padding.
 * @property medium A medium 8 [Dp] padding.
 * @property normal Normal 16 [Dp] padding.
 * @property large Large22 [Dp] padding.
 * @property xLarge Extra large 32 [Dp] padding.
 */
object ContentPadding {
    val small: Dp = 4.dp
    val medium: Dp = 8.dp
    val normal: Dp = 16.dp
    val large: Dp = 22.dp
    val xLarge: Dp = 32.dp

    val LargeArrangement = Arrangement.spacedBy(large)
    val MediumArrangement = Arrangement.spacedBy(medium)
    val SmallArrangement = Arrangement.spacedBy(small)
}