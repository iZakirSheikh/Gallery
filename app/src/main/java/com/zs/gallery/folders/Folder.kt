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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zs.compose.foundation.runCatching
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.Icon
import com.zs.compose.theme.text.Label
import com.zs.core.store.Folder
import com.zs.gallery.R
import java.io.File
import com.zs.gallery.common.compose.ContentPadding as CP

private const val TAG = "Folder"

/**
 * The default shape for tiles in the grid.
 */
private val DEFAULT_SHAPE = RoundedCornerShape(16)

/**
 * Formats a file size in bytes to a human-readable string.
 *
 * @param bytes The file size in bytes.
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
private fun isRemovableStorage(path: String): Boolean =
    runCatching(TAG) {
        val externalStorageDirectory = Environment.getExternalStorageDirectory().absolutePath
        !path.startsWith(externalStorageDirectory) && Environment.isExternalStorageRemovable(
            File(path)
        )
    } == true

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
                        .padding(CP.small)
                        .clip(DEFAULT_SHAPE)
                        .background(AppTheme.colors.background(elevation = elevation)),
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
                top = CP.medium,
                start = CP.medium
            ),
            style = AppTheme.typography.body2,
            fontWeight = FontWeight.Normal,
            text = value.name
        )

        // More Info
        Label(
            text = textResource(
                id = R.string.folders_scr_folder_name_sds, value.count,
                ctx.formattedFileSize(value.size.toLong())
            ),
            style = AppTheme.typography.label3.copy(fontSize = 10.sp),
            color = AppTheme.colors.onBackground.copy(ContentAlpha.medium),
            modifier = Modifier.padding(start = CP.medium),
        )
    }
)