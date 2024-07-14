package com.zs.gallery.folders


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.primex.core.drawHorizontalDivider
import com.primex.core.textResource
import com.primex.material2.DropDownMenuItem
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.menu.DropDownMenu2
import com.primex.material2.neumorphic.NeumorphicTopAppBar
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.ContentPadding
import com.zs.compose_ktx.None
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.Placeholder
import com.zs.gallery.common.preference
import com.zs.gallery.files.FilesViewState
import com.zs.gallery.settings.SettingsViewState

private const val TAG = "Folders"

@Composable
private fun OrderByDropDown(
    current: Int,
    onRequestChange: (new: Int) -> Unit
) {
    // orderBy
    var expanded by remember { mutableStateOf(false) }
    Button(
        onClick = { expanded = !expanded },
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier
            .padding(end = AppTheme.padding.small)
            .scale(0.90f),
        shape = CircleShape,
        border = ButtonDefaults.outlinedBorder,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.background(elevation = 1.dp),
            contentColor = AppTheme.colors.onBackground,
        ),
        content = {
            // The leading icon of the Button
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Sort,
                contentDescription = null,
                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing)
            )
            // Trailing Icon.
            Label(
                text = stringResource(
                    id = when (current) {
                        FoldersViewState.ORDER_BY_SIZE -> R.string.size
                        FoldersViewState.ORDER_BY_NAME -> R.string.name
                        FoldersViewState.ORDER_BY_DATE_MODIFIED -> R.string.recent
                        else -> error("Oops error!")
                    }
                )
            )

            // Menu
            DropDownMenu2(
                expanded = expanded,
                onDismissRequest = { expanded = !expanded },
                shape = AppTheme.shapes.compact,
                backgroundColor = AppTheme.colors.background(elevation = 2.dp),
                contentColor = AppTheme.colors.onBackground,
                content = {
                    // Sort by size
                    DropDownMenuItem(
                        title = stringResource(R.string.size),
                        onClick = {
                            onRequestChange(FoldersViewState.ORDER_BY_SIZE); expanded = false
                        },
                        icon = rememberVectorPainter(image = Icons.Outlined.Memory),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 190.dp)
                            .drawHorizontalDivider(
                                AppTheme.colors.onBackground,
                                indent = PaddingValues(horizontal = AppTheme.padding.normal)
                            ),
                        enabled = current != FoldersViewState.ORDER_BY_SIZE
                    )
                    // Sort by name
                    DropDownMenuItem(
                        title = stringResource(R.string.name),
                        onClick = {
                            onRequestChange(FoldersViewState.ORDER_BY_NAME); expanded = false
                        },
                        icon = rememberVectorPainter(image = Icons.Outlined.TextFields),
                        enabled = current != FoldersViewState.ORDER_BY_NAME,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 190.dp)
                            .drawHorizontalDivider(
                                AppTheme.colors.onBackground,
                                indent = PaddingValues(horizontal = AppTheme.padding.normal)
                            ),
                    )

                    // Sort by date modified
                    DropDownMenuItem(
                        title = stringResource(R.string.recent),
                        onClick = {
                            onRequestChange(FoldersViewState.ORDER_BY_DATE_MODIFIED); expanded =
                            false
                        },
                        icon = rememberVectorPainter(image = Icons.Outlined.CalendarMonth),
                        enabled = current != FoldersViewState.ORDER_BY_DATE_MODIFIED,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 190.dp)
                            .drawHorizontalDivider(
                                AppTheme.colors.onBackground,
                                indent = PaddingValues(horizontal = AppTheme.padding.normal)
                            ),
                    )
                },
            )
        }
    )
}

@Composable
@NonRestartableComposable
private fun TopBar(
    viewState: FoldersViewState,
    modifier: Modifier = Modifier
) {
    NeumorphicTopAppBar(
        title = { Label(text = textResource(id = R.string.folders)) },
        elevation = AppTheme.elevation.low,
        shape = CircleShape,
        modifier = modifier.padding(top = ContentPadding.medium),
        lightShadowColor = AppTheme.colors.lightShadowColor,
        darkShadowColor = AppTheme.colors.darkShadowColor,
        navigationIcon = {
            IconButton(imageVector = Icons.Default.FolderCopy, onClick = {})
        },
        actions = {
            OrderByDropDown(
                current = viewState.order,
                onRequestChange = { viewState.order = it })
        }
    )
}

/**
 * The min size of the single cell in grid.
 */
private val MIN_TILE_SIZE = 100.dp
private val fullLineSpan: (LazyGridItemSpanScope.() -> GridItemSpan) = { GridItemSpan(maxLineSpan) }

@Composable
private fun Content(
    state: FoldersViewState,
    modifier: Modifier = Modifier
) {
    val values by state.data.collectAsState()
    val multiplier by preference(key = SettingsViewState.GRID_ITEM_SIZE_MULTIPLIER)
    val navController = LocalNavController.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(MIN_TILE_SIZE * multiplier),
        modifier,
        contentPadding = PaddingValues(
            vertical = AppTheme.padding.normal,
            horizontal = AppTheme.padding.medium
        ),
        content = {
            // null means loading
            if (values == null)
                return@LazyVerticalGrid item(span = fullLineSpan, key = "key_loading_placeholder") {
                    Placeholder(
                        title = stringResource(R.string.loading),
                        iconResId = R.raw.lt_loading_dots_blue,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            val data = values ?: return@LazyVerticalGrid
            // empty means empty
            if (data.isEmpty())
                return@LazyVerticalGrid item(
                    span = fullLineSpan,
                    key = "key_empty_placeholder..."
                ) {
                    Placeholder(
                        title = stringResource(R.string.oops_empty),
                        iconResId = R.raw.lt_empty_box,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            // place the actual items on the screen.
            items(data, key = { it.path }) {
                Folder(
                    value = it,
                    modifier = Modifier
                        .clickable { navController.navigate(FilesViewState.direction()) }
                        .animateItem()
                )
            }
        }
    )
}

@Composable
fun Folders(state: FoldersViewState) {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            TopBar(
                viewState = state,
                modifier = Modifier
                    .statusBarsPadding()
                    .drawHorizontalDivider(color = AppTheme.colors.onBackground)
                    .padding(bottom = ContentPadding.medium),

                )
        },
        contentWindowInsets = WindowInsets.None,
        content = {
            Content(state = state, modifier = Modifier.padding(it))
        }
    )
}