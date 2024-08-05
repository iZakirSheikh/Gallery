/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 07-08-2024.
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

package com.zs.gallery.common

import android.text.format.DateUtils
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.primex.material2.Label
import com.zs.api.store.MediaFile
import com.zs.api.store.isImage
import com.zs.api.store.mediaUri
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.ContentPadding
import com.zs.compose_ktx.Divider
import com.zs.gallery.R

private const val TAG = "MediaFile"

/**
 * Displays a single MediaFile item.
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
fun MediaFile(
    value: MediaFile,
    focused: Boolean,
    checked: Int, // -1 for show selected circle; pass 0 for unchecked, 1 for checked.
    modifier: Modifier = Modifier,
) {
    val elevation = if (kotlin.random.Random.nextBoolean()) 0.5.dp else 1.dp
    Box(modifier = Modifier.background(AppTheme.colors.background(elevation = elevation)) then modifier) {
        val selected = checked == 1
        val progress by animateFloatAsState(targetValue = if (selected) 1f else 0f)
        val ctx = LocalContext.current
        AsyncImage(
            model = remember(key1 = value.id) {
                ImageRequest.Builder(ctx).apply {
                    data(value.mediaUri)
                    crossfade(true)
                }.build()
            },
            contentDescription = value.name,
            error = com.primex.core.rememberVectorPainter(
                image = ImageVector.vectorResource(id = R.drawable.ic_error_image_placeholder),
                tintColor = AppTheme.colors.onBackground.copy(alpha = ContentAlpha.Divider)
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .graphicsLayer {
                    val scale = lerp(start = 1f, 0.8f, progress)
                    scaleX = scale
                    scaleY = scale
                    shape = RoundedCornerShape(lerp(0, 16, progress))
                    clip = true
                }
                .aspectRatio(1.0f),
        )
        if (!value.isImage)
            Row(
                modifier = Modifier
                    .scale(0.9f)
                    .align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Label(
                        text = DateUtils.formatElapsedTime(value.duration / 1000L),
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.caption
                    )
                    Icon(
                        imageVector = Icons.Filled.PlayCircleFilled,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(ContentPadding.medium)
                    )
                }
            )
        if (checked == -1) return@Box

        Icon(
            imageVector = if (selected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
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