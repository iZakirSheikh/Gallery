/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 6 of Jan 2026
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
 * Last Modified by sheik on 6 of Jan 2026
 *
 */

package com.zs.gallery.files

import android.text.format.DateUtils
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zs.common.db.media.Snapshot
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Icon
import com.zs.compose.theme.text.Label
import com.zs.gallery.R
import com.zs.gallery.common.Res
import com.zs.gallery.common.vectorResource

private const val TAG = "MediaFile"

/**
 * Displays a [Snapshot] of MediaFile.
 *
 * @param value The MediaFile object to display.
 * @param focused Whether the item is currently focused.
 * @param checked The selection state of the item:
 *                 - -1: Show a selected circle (partially selected).
 *                 - 0: Unchecked.
 *                 - 1: Checked.
 * @param modifier Modifier to be applied to the item.
 */
@Composable
fun Snapshot(
    data: Snapshot,
    focused: Boolean,
    checked: Int, // -1 for show selected circle; pass 0 for unchecked, 1 for checked.
    modifier: Modifier = Modifier,
) {
    val elevation = if (kotlin.random.Random.nextBoolean()) 0.5.dp else 1.dp
    Box(modifier = Modifier.background(AppTheme.colors.background(elevation = elevation)) then modifier) {
        val selected = checked == 1
        val progress by animateFloatAsState(
            targetValue = if (selected) 1f else 0f,
            AppTheme.motionScheme.fastSpatialSpec()
        )
        val ctx = LocalContext.current
        AsyncImage(
            model = remember(key1 = data.id) {
                ImageRequest.Builder(ctx).apply {
                    data(data.thumbnail)
                }.build()
            },
            contentDescription = null,
            error = rememberVectorPainter(
                image = ImageVector.vectorResource(id = R.drawable.ic_error_image_placeholder),
                // tintColor = AppTheme.colors.onBackground.copy(alpha = ContentAlpha.divider)
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                // only apply graphics layer when selected.
                .thenIf(selected) {
                    graphicsLayer {
                        val scale = lerp(start = 1f, 0.8f, progress)
                        scaleX = scale
                        scaleY = scale
                        shape = RoundedCornerShape(lerp(0, 16, progress))
                        clip = true
                    }
                }
                .aspectRatio(1.0f),
        )
        if (!data.isImage)
            Row(
                modifier = Modifier
                    .scale(0.9f)
                    .align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    val duration = data.timeline.duration
                    Label(
                        text = DateUtils.formatElapsedTime(duration / 1000L),
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.label3
                    )
                    Icon(
                        imageVector = vectorResource(Res.drawable.play_circle_outline),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(Res.dimen.medium)
                    )
                }
            )
        if (checked == -1) return@Box

        Icon(
            imageVector = vectorResource(if (selected) Res.drawable.ic_check_circle_filled else Res.drawable.ic_circle_outline),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .background(
                    if (selected) Color.White else Color.Transparent,
                    CircleShape
                ),
            tint = if (selected) AppTheme.colors.accent else Color.Gray
        )
    }
}