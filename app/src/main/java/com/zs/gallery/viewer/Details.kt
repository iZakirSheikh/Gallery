/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 30-04-2025.
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

package com.zs.gallery.viewer

import android.text.format.DateUtils
import android.text.format.Formatter
import android.view.Gravity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.zs.compose.foundation.Background
import com.zs.compose.theme.AlertDialog
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.appbar.TopAppBar
import com.zs.compose.theme.minimumInteractiveComponentSize
import com.zs.compose.theme.text.Header
import com.zs.compose.theme.text.Label
import com.zs.core.store.MediaFile
import com.zs.gallery.R
import com.zs.gallery.common.compose.ContentPadding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatter
    get() = SimpleDateFormat("EEE, dd MMM yyyy, h:mm aa", Locale.getDefault())

private val MediaFile.megapixels
    get() = if (width > 0 && height > 0) (width * height) / 1_000_000f else -1f

@Composable
@NonRestartableComposable
private fun Info(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) = Row(
    modifier = modifier,
    horizontalArrangement = ContentPadding.LargeArrangement,
    verticalAlignment = Alignment.Top
) {
    Icon(icon, contentDescription = title.toString(), modifier = Modifier.size(20.dp))
    Label(
        buildAnnotatedString {
            append(title)
            val style = AppTheme.typography.body2
            withStyle(style.toSpanStyle()) {
                withStyle(style.toParagraphStyle()) {
                    appendLine(value)
                }
            }
        }, style = AppTheme.typography.label3,
        maxLines = 2
    )
}

@Composable
fun DetailsViewDialog(
    of: MediaFile?,
    background: Background,
    onDismissRequest: () -> Unit,
) {
    val compact = LocalWindowSize.current.width < Category.Medium
    val value = of ?: return
    AlertDialog(
        expanded = true,
        onDismissRequest = onDismissRequest,
        contentColor = AppTheme.colors.onBackground,
        shape = AppTheme.shapes.xLarge,
        properties = DialogProperties(usePlatformDefaultWidth = !compact),
        background = background,
        topBar = {
            TopAppBar(
                title = { Label(stringResource(R.string.properties)) },
                navigationIcon = {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                },
                elevation = 0.dp,
                shape = AppTheme.shapes.xLarge,
                modifier = Modifier.padding(2.dp),
                actions = {
                    IconButton(
                        Icons.Outlined.Close,
                        contentDescription = null,
                        onClick = onDismissRequest
                    )
                },
                background = Background(AppTheme.colors.accent.copy(ContentAlpha.indication))
            )

        },
        content = {
            val view = LocalView.current
            SideEffect {
                val window = (view.parent as DialogWindowProvider).window
                if (compact)
                    window.setGravity(Gravity.BOTTOM)
                else {
                    window.setGravity(Gravity.END)
                }
            }

            //
            // Date Modified
            Header(
                remember(value.id) { formatter.format(Date(value.dateModified)) },
                style = AppTheme.typography.title2,
                contentPadding = PaddingValues(
                    horizontal = ContentPadding.medium,
                    vertical = ContentPadding.medium
                )
            )

            // Section header
            Header(
                stringResource(R.string.details),
                style = AppTheme.typography.label3,
//                contentPadding = PaddingValues(
//                    horizontal = ContentPadding.medium,
//                    vertical = ContentPadding.small
//                ),
            )

            // Title
            Info(
                icon = Icons.Outlined.Image,
                title = stringResource(R.string.title),
                value = value.name
            )

            // Path
            Info(
                icon = Icons.Outlined.Memory,
                title = stringResource(R.string.path),
                value = value.path.substringBeforeLast("/"),
            )

            // Metadata
            val context = LocalContext.current
            Info(
                icon = Icons.Outlined.ImageSearch,
                title = stringResource(R.string.metadata),
                value = buildString {
                    append(Formatter.formatFileSize(context, value.size))
                    if (value.isImage && value.megapixels > 0)
                        append(String.format(" • %.1f MP", value.megapixels))
                    if (value.width > 0 && value.height > 0)
                        append(" • ${value.width} x ${value.height}")
                    if (value.duration > 0)
                        append(" • " + DateUtils.formatElapsedTime(value.duration / 1000L))
                }
            )
        }
    )
}