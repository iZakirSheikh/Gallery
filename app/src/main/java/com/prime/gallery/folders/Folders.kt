package com.prime.gallery.folders

import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prime.gallery.BuildConfig
import com.prime.gallery.Material
import com.prime.gallery.R
import com.prime.gallery.core.ContentElevation
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.billing.purchased
import com.prime.gallery.core.compose.Image
import com.prime.gallery.core.compose.LocalSystemFacade
import com.prime.gallery.core.compose.LocalWindowSizeClass
import com.prime.gallery.core.compose.Placeholder
import com.prime.gallery.core.compose.purchase
import com.prime.gallery.core.db.Folder
import com.primex.core.withStyle
import com.primex.material3.IconButton
import com.primex.material3.Label

private const val TAG = "Folders"

/**
 * The min size of the single cell in grid.
 */
private val MIN_TILE_SIZE = 120.dp
private val GridItemPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
private val DEFAULT_TILE_SHAPE = RoundedCornerShape(16)

@Composable
@NonRestartableComposable
private fun Folder(
    value: Folder,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .clip(DEFAULT_TILE_SHAPE)  // clip the ripple
            .then(modifier)
            .padding(GridItemPadding) // add padding after size.
            .wrapContentHeight(),  // wrap the height of the content
    ) {
        val alpha = if (kotlin.random.Random.nextBoolean()) 2 * 0.04f else 0.04f
        // Artwork
        // The representation image
        Image(
            data = "file://${value.artwork}",
            modifier = Modifier
                .aspectRatio(1.0f)
                .padding(ContentPadding.small)
                .shadow(ContentElevation.medium, shape = DEFAULT_TILE_SHAPE)
                .background(
                    Color.Black
                        .copy(alpha)
                        .compositeOver(Material.colorScheme.background),
                    DEFAULT_TILE_SHAPE
                ),
            error = com.primex.core.rememberVectorPainter(
                image = ImageVector.vectorResource(id = R.drawable.ic_error_image_placeholder),
                tintColor = Material.colorScheme.onSurface.copy(0.76f)
            ),
        )
        val ctx = LocalContext.current
        // Label
        Text(
            maxLines = 2,
            modifier = Modifier
                .padding(top = ContentPadding.medium, start = ContentPadding.medium)
                .align(Alignment.Start),
            style = Material.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            text = buildAnnotatedString {
                append(value.name)
                withStyle(Material.typography.labelSmall) {
                    append(
                        "" + value.count + " items - "
                                + Formatter.formatShortFileSize(ctx, value.size.toLong())
                    )
                }
            }
        )
    }
}

@Composable
@NonRestartableComposable
private fun OrderBy(
    current: Int,
    onRequestChange: (new: Int) -> Unit
) {
    // orderBy
    var expanded by remember { mutableStateOf(false) }
    FilledTonalButton(
        onClick = { expanded = !expanded },
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier
            .padding(end = ContentPadding.small)
            .scale(0.85f)
    ) {
        // The leading icon of the Button
        Icon(
            imageVector = Icons.Outlined.Tune,
            contentDescription = null,
            modifier = Modifier.padding(end = ButtonDefaults.IconSpacing)
        )
        // Trailing Icon.
        Label(
            text = stringResource(
                id = when (current) {
                    Folders.ORDER_BY_SIZE -> R.string.size
                    Folders.ORDER_BY_NAME -> R.string.name
                    Folders.ORDER_BY_DATE_MODIFIED -> R.string.date_modified
                    else -> error("Oops error!")
                }
            )
        )

        // The drop down menu.
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            content = {
                // Sort by size
                DropdownMenuItem(
                    text = { Label(text = stringResource(R.string.size)) },
                    onClick = { onRequestChange(Folders.ORDER_BY_SIZE); expanded = false },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Memory, contentDescription = null)
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 190.dp),
                    enabled = current != Folders.ORDER_BY_SIZE
                )
                // Sort by name
                DropdownMenuItem(
                    text = { Label(text = stringResource(R.string.name)) },
                    onClick = { onRequestChange(Folders.ORDER_BY_NAME); expanded = false },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.TextFields, contentDescription = null)
                    },
                    enabled = current != Folders.ORDER_BY_NAME
                )
                // Sort by date modified
                DropdownMenuItem(
                    text = { Label(text = stringResource(R.string.date_modified)) },
                    onClick = { onRequestChange(Folders.ORDER_BY_DATE_MODIFIED); expanded = false },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null)
                    },
                    enabled = current != Folders.ORDER_BY_DATE_MODIFIED
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TopBar(
    state: Folders,
    modifier: Modifier = Modifier,
    behavior: TopAppBarScrollBehavior? = null
) {
    SmallTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.folders),
                style = Material.typography.bodyLarge
            )
        },
        scrollBehavior = behavior,
        modifier = modifier,
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.FolderCopy,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = ContentPadding.normal)
            )
        },
        actions = {
            // Show if not purchased.
            val provider = LocalSystemFacade.current
            val purchased by purchase(id = BuildConfig.IAP_NO_ADS)
            if (purchased.purchased)
                IconButton(
                    icon = Icons.Outlined.ShoppingCart,
                    contentDescription = "buy full version",
                    onClick = { provider.launchBillingFlow(BuildConfig.IAP_NO_ADS) },
                )
            val ascending = state.ascending
            IconButton(
                icon = Icons.Outlined.Sort,
                contentDescription = null,
                modifier = Modifier.rotate(if (ascending) 180f else 0f),
                onClick = { state.ascending = !ascending }
            )

            // Change orderBy button
            val order = state.order
            OrderBy(current = order, onRequestChange = { state.order = it })
        }
    )
}

@Composable
private fun SideBar() {

}

private val fullLineSpan: (LazyGridItemSpanScope.() -> GridItemSpan) = { GridItemSpan(maxLineSpan) }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Content(
    state: Folders,
    modifier: Modifier = Modifier
) {
    val values by state.folders.collectAsState()
    LazyVerticalGrid(columns = GridCells.Adaptive(MIN_TILE_SIZE), modifier) {
        // null means loading
        if (values == null)
            return@LazyVerticalGrid item(span = fullLineSpan) {
                Placeholder(
                    title = stringResource(R.string.loading),
                    iconResId = R.raw.lt_loading_dots_blue,
                    modifier = Modifier.fillMaxSize()
                )
            }
        val data = values ?: return@LazyVerticalGrid
        // empty means empty
        if (data.isEmpty())
            return@LazyVerticalGrid item(span = fullLineSpan) {
                Placeholder(
                    title = "Oops Empty!!",
                    iconResId = R.raw.lt_empty_box,
                    modifier = Modifier.fillMaxSize()
                )
            }
        // place the actual items on the screen.
        items(data, key = { it.path }) {
            Folder(
                value = it,
                modifier = Modifier
                    .animateItemPlacement()
                    .clickable { }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Compact(
    state: Folders,
    modifier: Modifier = Modifier
) {
    val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = { TopBar(state = state, behavior = behavior) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection)
    ) {
        Content(state = state, modifier = Modifier.padding(it))
    }
}

@Composable
private fun Medium(
    state: Folders,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        SideBar()
        Content(state = state, modifier = Modifier.weight(1f))
    }
}

@Composable
private inline fun Expanded(
    state: Folders,
    modifier: Modifier = Modifier
) {
    Medium(state = state, modifier)
}

@Composable
@NonRestartableComposable
fun Folders(state: Folders) {
    when (LocalWindowSizeClass.current.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Compact(state = state)
        WindowWidthSizeClass.Medium -> Medium(state = state)
        WindowWidthSizeClass.Expanded -> Expanded(state = state)
    }
}