/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 18-07-2024.
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

@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zs.gallery.preview

import android.net.Uri
import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.times
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImage
import com.primex.material2.DropDownMenuItem
import com.primex.material2.IconButton
import com.primex.material2.menu.DropDownMenu2
import com.zs.api.store.MediaProvider
import com.zs.compose_ktx.sharedElement
import com.zs.gallery.common.LocalNavController
import kotlin.math.cos

private const val TAG = "Viewer"

@Composable
@NonRestartableComposable
private fun Page(
    uri: Uri,
    modifier: Modifier = Modifier
) {

    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier
    )
}

context(RowScope)
@Composable
private inline fun Actions(
) {
    IconButton(imageVector = Icons.TwoTone.Delete, onClick = {})
    IconButton(imageVector = Icons.Outlined.StarOutline, onClick = { /*TODO*/ })
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
                title = "Share", onClick = { /*TODO*/ }, icon = rememberVectorPainter(
                    image = Icons.Filled.Share
                )
            )
        }
    }
}

@Composable
fun Pager(
    viewState: ViewerViewState,
    modifier: Modifier = Modifier
) {

    val state = rememberPagerState(viewState.index, pageCount = { viewState.size })

    // set up all transformation states
    var zoom by remember { mutableFloatStateOf(1f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                Log.d(TAG, "onPreScroll: Available - $available")
                return if (zoom > 1f) Offset.Zero else available
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                Log.d(TAG, "onPostScroll: ")
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    var offset by remember { mutableStateOf(Offset.Zero) }
    var sizeI by remember { mutableStateOf(IntSize.Zero) }
    var sizeP by remember { mutableStateOf(IntSize.Zero) }

    val transformState =     rememberTransformableState { gestureZoom, pan, gestureRotate ->
        zoom = (zoom * gestureZoom).coerceIn(1f, 3f)
        // Calculate the new offset with bounds
        // Calculate new offset with bounds
        val scaledW = sizeI.width * zoom
        val scaledH = sizeI.height * zoom
        val maxX = ((scaledW - sizeP.width) / 2f).coerceAtLeast(0f)
        val maxY = ((scaledH - sizeP.height) / 2f).coerceAtLeast(0f)
        val newOffset = offset + pan * zoom
        offset = Offset(
            x = (newOffset.x).coerceIn(-maxX, maxX),
            y = (newOffset.y).coerceIn(-maxY, maxY)
        )
        Log.d(TAG, "size - $sizeI, sizeP - $sizeP, maxX - $maxX, maxY - $maxY")
    }

    HorizontalPager(
        state = state,
        key = { it },
        modifier = modifier
            //     .nestedScroll(connection = nestedScrollConnection)
            .background(Color.Black)
            .onSizeChanged { sizeP = it },
        userScrollEnabled = !transformState.isTransformInProgress
        //  pageNestedScrollConnection = nestedScrollConnection,
    ) { index ->
        viewState.index = index
        val uri = viewState.fetchUriForIndex()
        val id = viewState.fetchIdForIndex()
        Box(modifier = Modifier) {
            AsyncImage(
                model = uri,
                onSuccess = {sizeI = it.painter.intrinsicSize.toIntSize()},
                contentDescription = null,
                modifier = Modifier
                    .nestedScroll(nestedScrollConnection)
                    .sharedElement(RouteViewer.buildSharedFrameKey(id))
            )
        }
    }
}

@Composable
fun Viewer(viewState: ViewerViewState) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        Pager(viewState, modifier = Modifier.fillMaxSize())
        TopAppBar(
            title = {},
            navigationIcon = {
                val navController = LocalNavController.current
                IconButton(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    onClick = navController::navigateUp
                )
            },
            actions = { Actions()},
            contentColor = Color.White,
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            modifier = Modifier.align(Alignment.TopCenter),
            windowInsets = WindowInsets.statusBars
        )
    }
}