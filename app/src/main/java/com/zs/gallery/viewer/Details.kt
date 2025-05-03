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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.Dialog
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.foundation.UmbraGrey
import com.zs.compose.foundation.background
import com.zs.compose.foundation.fadingEdge
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.ListItem
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.LocalWindowSize
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
    title: CharSequence,
    value: CharSequence,
    modifier: Modifier = Modifier,
) = ListItem(
    leading = { Icon(icon, contentDescription = title.toString()) },
    heading = {
        Label(
            title, /*fontWeight = FontWeight.Bold,*/
            style = AppTheme.typography.body2,
        )
    },
    subheading = {
        Label(
            value,
            style = AppTheme.typography.body3,
            color = LocalContentColor.current.copy(ContentAlpha.medium),
            maxLines = 2
        )
    },
    modifier = modifier,
    padding = PaddingValues(horizontal = ContentPadding.normal, vertical = 0.dp)
)

@Composable
private fun Details(
    value: MediaFile,
    background: Background,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberScrollState()
    Column(
        modifier = Modifier
            .fadingEdge(state, length = 16.dp, horizontal = false)
            .verticalScroll(state)
            .padding(horizontal = ContentPadding.medium)
            .clip(AppTheme.shapes.large)
            .background(background)
            .then(modifier),
        content = {

            // Top-Bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(ContentPadding.small),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )

                    Label(
                        "Properties",
                        style = AppTheme.typography.title2,
                        modifier = Modifier.padding(vertical = ContentPadding.medium)
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        Icons.Outlined.Close,
                        onClick = onDismissRequest,
                        contentDescription = null
                    )
                }
            )

            // Representational Image.
            AsyncImage(
                value.mediaUri,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(16 / 9f)
                    .padding(horizontal = 12.dp, 4.dp)
                    .clip(AppTheme.shapes.medium)
                    .border(Dp.Hairline, Color.SignalWhite.copy(0.1f), AppTheme.shapes.medium)
                    .background(Color.UmbraGrey.copy(0.1f)),
                contentScale = ContentScale.Crop
            )

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
                style = AppTheme.typography.title3,
                contentPadding = PaddingValues(
                    horizontal = ContentPadding.medium,
                    vertical = ContentPadding.small
                ),
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

@Composable
@NonRestartableComposable
fun Details(
    of: MediaFile?,
    background: Background,
    onDismissRequest: () -> Unit,
) {
    val (wClass, hClazz) = LocalWindowSize.current
    Dialog(
        expanded = of != null,
        onDismissRequest = onDismissRequest,
        content = {

//            val view = LocalView.current
//
//            SideEffect {
//                val window = (view.parent as DialogWindowProvider).window
//                if (wClass < hClazz)
//                    window.setGravity(Gravity.CENTER)
//                else
//                    window.setGravity(Gravity.END)
//            }

            CompositionLocalProvider(
                LocalContentColor provides Color.UmbraGrey,
                content = {
                    Details(
                        value = of ?: return@CompositionLocalProvider,
                        background = background,
                        onDismissRequest = onDismissRequest
                    )
                }
            )
        }
    )
}
