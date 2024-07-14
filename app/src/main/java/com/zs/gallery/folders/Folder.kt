/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 17-07-2024.
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

import android.content.Context
import android.os.Environment
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.primex.core.textResource
import com.primex.core.withStyle
import com.primex.material2.Label
import com.primex.material2.Text
import com.zs.api.store.Folder
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.Divider
import com.zs.gallery.R
import java.io.File

private const val TAG = "Folder"

/**
 * The default shape for tiles in the grid.
 */
private val DEFAULT_SHAPE = RoundedCornerShape(16)

/**
 * Formats a file size in bytes to a human-readable string.
 *
 * @param bytes Thefile size in bytes.
 * @return The formatted file size string.
 */
private fun Context.formattedFileSize(bytes: Long) =
    Formatter.formatFileSize(this, bytes)

/**
 * Checks if a given path corresponds to removable storage.
 *
 * @param path The path to check.
 * @return True if the path is on removable storage, false otherwise.
 */
private fun isRemovableStorage(path: String): Boolean {
    val externalStorageDirectory = Environment.getExternalStorageDirectory().absolutePath
    return !path.startsWith(externalStorageDirectory) && Environment.isExternalStorageRemovable(
        File(
            path
        )
    )
}

@Composable
fun Folder(
    value: Folder,
    modifier: Modifier = Modifier
) = Column(
    modifier = Modifier
        .clip(DEFAULT_SHAPE)  // clip the ripple
        .then(modifier),
    horizontalAlignment = Alignment.Start,
    content = {
        val elevation = if (kotlin.random.Random.nextBoolean()) 0.5.dp else 1.dp
        // Image
        Box(
            content = {
                AsyncImage(
                    model = value.artworkUri,
                    contentDescription = value.name,
                    modifier = Modifier
                        .aspectRatio(1.0f)
                        .padding(AppTheme.padding.small)
                        .clip(DEFAULT_SHAPE)
                        .background(AppTheme.colors.background(elevation = elevation)),
                    error = com.primex.core.rememberVectorPainter(
                        image = ImageVector.vectorResource(id = R.drawable.ic_error_image_placeholder),
                        tintColor = AppTheme.colors.onBackground.copy(alpha = ContentAlpha.Divider)
                    ),
                    contentScale = ContentScale.Crop
                )

                val isRemovable = isRemovableStorage(value.path)
                if (!isRemovable) return@Box
                Icon(
                    imageVector = Icons.Outlined.SdStorage,
                    contentDescription = "removable card",
                    modifier = Modifier
                        .scale(0.8f)
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    tint = Color.White
                )
            },
        )
        val ctx = LocalContext.current

        // TextLabel
        Label(
            modifier = Modifier.padding(
                top = AppTheme.padding.medium,
                start = AppTheme.padding.medium
            ),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            text = value.name
        )

        // More Info
        Label(
            text = textResource(
                id = R.string.folders_formatted_folder_name_sds, value.count,
                ctx.formattedFileSize(value.size.toLong())
            ),
            style = AppTheme.typography.caption.copy(fontSize = 10.sp),
            color = AppTheme.colors.onBackground.copy(ContentAlpha.medium),
            modifier = Modifier.padding(start = AppTheme.padding.medium),
        )
    }
)



