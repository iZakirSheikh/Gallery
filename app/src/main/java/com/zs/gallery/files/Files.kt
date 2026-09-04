package com.zs.gallery.files

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zs.compose.foundation.stickyHeader
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.TonalIconButton
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.text.TonalHeader
import com.zs.domain.db.media.Snapshot
import com.zs.gallery.common.AppConfig
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.NavKey
import com.zs.gallery.common.Res
import com.zs.gallery.common.compose.backgdrop
import com.zs.gallery.common.compose.fadingEdge2
import com.zs.gallery.common.compose.rememberBackdropHandle
import com.zs.gallery.common.compose.shine
import com.zs.gallery.common.vectorResource
import androidx.compose.foundation.layout.PaddingValues as Padding

private val HeaderPadding = Padding(2.dp, 4.dp, 2.dp, 4.dp)

private fun LazyPagingItems<Snapshot>.getAllItems(from: Int, key: String): List<Long>{
    val list = mutableListOf<Long>()
    for (i in from until itemCount){
        val item = peek(i)
        if (item == null || item.header != key)
            break
        list += item.id
    }
    return list
}

@Composable
fun Files(viewState: FilesViewState) {
    // The top nav insets
    val (width, _) = LocalWindowSize.current
    val compact = width < Category.Medium
    val inAppNavInsets = WindowInsets.content
    //
    val topAppBarScrollBehavior = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val handle = rememberBackdropHandle()
    val colors = AppTheme.colors
    // actions
    val actions = viewState.actions
    val ctx = LocalContext.current
    val navController = LocalNavController.current

    // prioritise clearing of selection mode if back is pressed
    BackHandler(
        viewState.isInSelectionMode,
        viewState::clear
    )

    // content
    Scaffold() {
        val state = rememberLazyGridState()
        val multiplier = AppConfig.gridItemSizeMultiplier
        val navController = LocalNavController.current
        //
        val data = viewState.data.collectAsLazyPagingItems()
        val selected = viewState.selected
        // Content
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Adaptive(100.dp * multiplier),
            horizontalArrangement = Arrangement.spacedBy(Res.dimen.small),
            verticalArrangement = Arrangement.spacedBy(Res.dimen.small),
            contentPadding = (inAppNavInsets.add(WindowInsets.content)
                .union(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))).asPaddingValues() +
                    (Padding(end = if (!compact) Res.dimen.large else 0.dp) + Padding(horizontal = Res.dimen.medium)),
            modifier = Modifier
                .fillMaxSize()
                .fadingEdge2(length = 56.dp)
                .backgdrop(handle)
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            content = {
                if (data.itemCount == 0)
                    return@LazyVerticalGrid // emptystate
                //
                for (index in 0 until data.itemCount) {
                    val item = data[index] ?: continue
                    val header = item.header
                    if (header != null) {
                        stickyHeader(state = state, key = header, contentType = "header") {
                            val level by remember {
                                derivedStateOf {
                                    val all = data.getAllItems(index, header)
                                     when {
                                        selected.containsAll(all) -> 2
                                        selected.any { all.contains(it) } -> 1
                                        else -> 0
                                    }
                                }
                            }
                            Row(
                                Modifier.padding(HeaderPadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                content = {
                                    TonalHeader(header)
                                    // toggle
                                    TonalIconButton(
                                        icon = vectorResource(Res.drawable.ic_circle_outline),
                                        contentDescription = null,
                                        border = colors.shine,
                                        shape = AppTheme.shapes.small,
                                        //tint = /*if (level == SelectionTracker.Level.FULL) AppTheme.colors.accent*/  LocalContentColor.current,
                                        onClick = { viewState.select(header) }
                                    )
                                }
                            )
                        }
                    }

                    item(
                        key = item.id,
                        contentType = "media_file",
                        content = {
                            FileView(
                                value = item,
                                checked = when {
                                    selected.isEmpty() -> -1
                                    selected.contains(item.id) -> 1
                                    else -> 0
                                },
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            if (selected.isNotEmpty())
                                                viewState.select(item.id)
                                            else
                                                navController.navigate(NavKey.Viewer(0, null))
                                        },
                                        onLongClick = { viewState.select(item.id) }
                                    )
                                    .animateItem()
                                  //then(RouteFiles.sharedElement(item.id))
                            )
                        }
                    )
                }
            }
        )
    }
}