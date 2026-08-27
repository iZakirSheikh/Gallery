package com.zs.gallery.files

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.zs.compose.foundation.stickyHeader
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.Surface
import com.zs.compose.theme.TonalIconButton
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.FabPosition
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.text.Text
import com.zs.compose.theme.text.TonalHeader
import com.zs.gallery.common.AppConfig
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.Res
import com.zs.gallery.common.compose.fadingEdge2
import com.zs.gallery.common.runIf
import com.zs.gallery.common.vectorResource

private const val TAG = "Files"

@Composable
fun Files(viewState: FilesViewState) {
    // Content
    val source = viewState.data.collectAsLazyPagingItems()
    Surface() {
        LazyVerticalGrid(
            //  state = state,
            columns = GridCells.Adaptive(
                100.dp
                /** multiplier*/
            ),
            // horizontalArrangement = Res.dimen.spacing_x_small,
            //  verticalArrangement = Res.dimen.spacing_x_small,
            contentPadding = (WindowInsets.content
                .union(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))).asPaddingValues() +
                    (PaddingValues(end = if (!false) Res.dimen.large else 0.dp) + PaddingValues(
                        horizontal = Res.dimen.medium
                    )),
            modifier = Modifier
                .fillMaxSize()
                .fadingEdge2(length = 56.dp),
            //.source(provider)
            // .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            content = {
                for (index in 0 until source.itemCount) {
                    val item = source[index] ?: continue
                    item(
                        key = item.id,
                        contentType = "media_file",
                        content = {
                            FileView(
                                value = item,
                                checked = 0 /*when {
                                    selected.isEmpty() -> -1
                                    selected.contains(item.id) -> 1
                                    else -> 0
                                }*/,
                                modifier = Modifier
                                    .animateItem()
                                //  .then(RouteFiles.sharedElement(item.id))
                                //  .then(clickable)
                            )
                        }
                    )
                }
            }
        )
    }
}