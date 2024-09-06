@file:OptIn(ExperimentalMaterialApi::class)

package com.zs.gallery.viewer

import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.rounded.Minimize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.primex.core.fadeEdge
import com.primex.core.textResource
import com.primex.material2.Button2
import com.primex.material2.Label
import com.primex.material2.ListTile
import com.zs.domain.store.MediaFile
import com.zs.domain.store.isImage
import com.zs.foundation.AppTheme
import com.zs.foundation.ContentPadding
import com.zs.foundation.Header
import com.zs.foundation.menu.MenuItem
import com.zs.gallery.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatter
    get() = SimpleDateFormat("EEE, dd MMM yyyy, h:mm aa", Locale.getDefault())

private val MediaFile.megapixels
    get() = if (width > 0 && height > 0) (width * height) / 1_000_000f else -1f

@Composable
private inline fun MainMenu(
    actions: List<MenuItem>,
    crossinline onAction: (action: MenuItem) -> Unit
) {
    val state = rememberScrollState()
    Row(
        modifier = Modifier
            .fadeEdge(AppTheme.colors.background(2.dp), state, length = 16.dp)
            .horizontalScroll(state)
            .padding(vertical = ContentPadding.medium, horizontal = ContentPadding.normal)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { action ->
            Button2(
                label = textResource(action.label),
                crown = action.icon?.let { rememberVectorPainter(it) },
                onClick = { onAction(action) },
                enabled = action.enabled,
                elevation = null,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = LocalContentColor.current,
                ),
                shape = AppTheme.shapes.compact
            )
        }
    }
}

@Composable
@NonRestartableComposable
private fun Detail(
    icon: ImageVector,
    title: CharSequence,
    value: CharSequence,
    modifier: Modifier = Modifier
) {
    ListTile(
        leading = { Icon(icon, contentDescription = title.toString()) },
        headline = { Label(title, fontWeight = FontWeight.Bold) },
        subtitle = {
            Label(
                value,
                style = AppTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(ContentAlpha.medium),
                maxLines = 2
            )
        },
        modifier = modifier
    )
}

@Composable
fun Details(
    value: MediaFile,
    actions: List<MenuItem>,
    onAction: (action: MenuItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    shape: Shape = RoundedCornerShape(topStartPercent = 8, topEndPercent = 8)
) {
    Surface(
        modifier = modifier,
        shape = shape,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
            ) {
                // Handle
                Icon(
                    Icons.Rounded.Minimize,
                    modifier = Modifier
                        .scale(2.0f)
                        .offset(y = -12.dp)
                        .align(Alignment.CenterHorizontally),
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                )

                // FileActions
                MainMenu(actions, onAction = onAction)

                Divider()

                val state = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fadeEdge(AppTheme.colors.background(2.dp), state, length = 16.dp, horizontal = false)
                        .verticalScroll(state)
                        .padding(horizontal = ContentPadding.small)
                ) {
                    Header(
                        remember(value.id) { formatter.format(Date(value.dateModified)) },
                        style = AppTheme.typography.titleLarge,
                        contentPadding = PaddingValues(
                            horizontal = ContentPadding.medium,
                            vertical = ContentPadding.normal
                        )
                    )

                    // Section header
                    Header(
                        stringResource(R.string.details),
                        style = AppTheme.typography.titleSmall,
                        contentPadding = PaddingValues(
                            horizontal = ContentPadding.medium,
                            //  vertical = ContentPadding.medium
                        ),
                    )

                    // Title
                    Detail(
                        icon = Icons.Outlined.Image,
                        title = stringResource(R.string.title),
                        value = value.name
                    )

                    // Path
                    Detail(
                        icon = Icons.Outlined.Memory,
                        title = stringResource(R.string.path),
                        value = value.path.substringBeforeLast("/"),
                    )

                    // Metadata
                    val context = LocalContext.current
                    Detail(
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
            }
        }
    )
}