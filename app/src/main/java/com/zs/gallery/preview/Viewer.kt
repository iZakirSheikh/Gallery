@file:OptIn(ExperimentalSharedTransitionApi::class)
@file:Suppress("NOTHING_TO_INLINE")

package com.zs.gallery.preview

import android.app.Activity
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.primex.core.findActivity
import com.primex.material2.DropDownMenuItem
import com.primex.material2.IconButton
import com.primex.material2.appbar.TopAppBar
import com.primex.material2.menu.DropDownMenu2
import com.zs.api.store.MediaProvider
import com.zs.api.store.mediaUri
import com.zs.compose_ktx.lottieAnimationPainter
import com.zs.compose_ktx.sharedElement
import com.zs.compose_ktx.thenIf
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import kotlinx.coroutines.runBlocking
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable


context(RowScope)
@Composable
private inline fun Actions(
    viewState: ViewerViewState,
) {
    val context = LocalContext.current
    IconButton(imageVector = Icons.TwoTone.Delete, onClick = {viewState.remove(context.findActivity())})

    IconButton(
        imageVector = if (!viewState.favourite) Icons.Outlined.StarOutline else Icons.Outlined.Star,
        onClick = viewState::toggleLike
    )
    IconButton(imageVector = Icons.Outlined.Info, onClick = { /*TODO*/ })

    // DropDownMenu for more options
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material.IconButton(onClick = { expanded = true }) {
        Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
        DropDownMenu2(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 180.dp)
        ) {
            DropDownMenuItem(
                title = "Edit in",
                onClick = { /*TODO*/ },
                icon = rememberVectorPainter(
                    image = Icons.Outlined.Edit
                ),

                )
            DropDownMenuItem(
                title = "Use as", onClick = { /*TODO*/ }, icon = rememberVectorPainter(
                    image = Icons.AutoMirrored.Outlined.OpenInNew
                )
            )

            DropDownMenuItem(
                title = "Share", onClick = { viewState.share(context.findActivity()) }, icon = rememberVectorPainter(
                    image = Icons.Filled.Share
                )
            )
        }
    }
}

private val DEFAULT_ZOOM_SPECS = ZoomSpec(5f)

@Composable
fun Content(viewState: ViewerViewState, modifier: Modifier = Modifier) {
    val values = viewState.data
    if (values.isEmpty()) return
    val statePager =
        rememberPagerState(initialPage = viewState.index, pageCount = { values.size })
    val stateTransformable = rememberZoomableState(
        DEFAULT_ZOOM_SPECS
    )
    stateTransformable.contentScale = ContentScale.None
    HorizontalPager(
        state = statePager,
        key = { values[it].id },
        pageSpacing = 16.dp,
        modifier = modifier,
        userScrollEnabled = stateTransformable.zoomFraction == 0f,
        beyondViewportPageCount = 1
    ) { index ->
        val item = values[index]
        val sharedFrameKey = RouteViewer.buildSharedFrameKey(item.id)
        val isFocused = statePager.currentPage == index
        val uri = item.mediaUri
        if (isFocused)
            viewState.focused = item.id
        AsyncImage(
            model = uri,
            contentDescription = item.name,
            modifier = Modifier
                .fillMaxSize()
                .thenIf(
                    isFocused,
                    Modifier
                        .zoomable(stateTransformable)
                        .sharedElement(sharedFrameKey)
                ),
            onSuccess = {
                runBlocking {
                    stateTransformable.setContentLocation(
                        ZoomableContentLocation.scaledInsideAndCenterAligned(
                            it.painter.intrinsicSize
                        )
                    )
                }
            },
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None
        )
    }
}

@Composable
fun Viewer(viewState: ViewerViewState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Content(viewState, modifier = Modifier.fillMaxSize())
        androidx.compose.material.TopAppBar(
            title = {},
            navigationIcon = {
                val navController = LocalNavController.current
                IconButton(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    onClick = navController::navigateUp
                )
            },
            actions = { Actions(viewState)},
            contentColor = Color.White,
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            modifier = Modifier.align(Alignment.TopCenter),
            windowInsets = WindowInsets.statusBars,
        )
    }
}