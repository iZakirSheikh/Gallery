/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 04-04-2025.
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

package com.zs.gallery.viewer

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.material.icons.outlined.ReplyAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.transformations
import com.zs.compose.foundation.ImageBrush
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.foundation.findActivity
import com.zs.compose.foundation.foreground
import com.zs.compose.foundation.thenIf
import com.zs.compose.foundation.visualEffect
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.FabPosition
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.core.coil.ReBlurTransformation
import com.zs.core.coil.RsBlurTransformation
import com.zs.core.coil.preferCachedThumbnail
import com.zs.core.store.MediaProvider
import com.zs.gallery.common.WindowStyle
import com.zs.gallery.common.compose.ContentPadding
import com.zs.gallery.common.compose.FloatingActionMenu
import com.zs.gallery.common.compose.LocalNavController
import com.zs.gallery.common.compose.LocalSystemFacade
import com.zs.gallery.common.compose.OverflowMenu
import com.zs.gallery.common.compose.background
import com.zs.gallery.common.compose.preference
import com.zs.gallery.common.compose.rememberAcrylicSurface
import com.zs.gallery.common.compose.shine
import com.zs.gallery.common.compose.source
import com.zs.gallery.common.scaledInsideAndCenterAlignedFrom
import com.zs.gallery.files.RouteFiles
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.zoomable
import androidx.compose.foundation.pager.rememberPagerState as PagerState
import androidx.compose.ui.graphics.vector.rememberVectorPainter as VectorPainter
import coil3.compose.rememberAsyncImagePainter as Painter
import coil3.request.ImageRequest.Builder as ImageRequest
import me.saket.telephoto.zoomable.rememberZoomableState as ZoomableState

private const val TAG = "MediaViewer"

private val MediaViewerViewState.index
    get() = data.indexOfFirst { focused == it.id }

private const val EVENT_BACK_PRESS = 0
private const val EVENT_SHOW_INFO = 1
private const val EVENT_IMMERSIVE_VIEW = 2

private val DEFAULT_ZOOM_SPECS = ZoomSpec(5f)

// FixMe - This is not the best solution; find another.
private val ZoomableState.isZoomedOut get() = (zoomFraction ?: 0f) <= 0.0001f


@Composable
private fun Carousel(
    viewState: MediaViewerViewState,
    onRequest: (event: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = viewState.data
    val state = PagerState(initialPage = viewState.index, pageCount = { data.size })
    val scope = rememberCoroutineScope()
    //
    val zoomable =
        ZoomableState(DEFAULT_ZOOM_SPECS).apply { contentScale = ContentScale.None }
    // Modifier for zoomable images,
    // triggering immersive mode on click and handling double-tap zoom
    val zModifier = Modifier.zoomable(
        zoomable,
        onClick = { onRequest(EVENT_IMMERSIVE_VIEW) },
        onDoubleClick = DoubleClickToZoomListener.cycle(2f)
    )
    // TODO - Remove this once inBuilt Video Player is available.
    val vector = VectorPainter(Icons.Outlined.PlayCircleFilled)
    val playIconModifier = Modifier.drawWithCache {
        onDrawWithContent {
            drawContent()
            // overlay icon on it.
            val iconSize = Size(74.dp.toPx(), 74.dp.toPx())
            // draw at the centre of the screen.
            translate(size.width / 2 - iconSize.width / 2, size.height / 2 - iconSize.height / 2) {
                with(vector) {
                    draw(iconSize, alpha = 1f, colorFilter = ColorFilter.tint(Color.White))
                }
            }
        }
    }

    // Handle BackPress
    BackHandler {
        when {
            // reset zoom
            !zoomable.isZoomedOut -> scope.launch { zoomable.resetZoom() }
            // else ask parent to handle back-press
            else -> onRequest(EVENT_BACK_PRESS)
        }
    }
    // Horizontal pager to display the images/videos
    // Disable swipe when zoomed in
    // Preload adjacent pages for smoother transitions
    Log.d(TAG, "MainContent - ZoomFraction: ${zoomable.zoomFraction}")
    val navController = LocalNavController.current
    val ctx = LocalContext.current
    HorizontalPager(
        state = state,
        key = { data[it].id },
        pageSpacing = ContentPadding.normal,
        modifier = modifier,
        userScrollEnabled = zoomable.isZoomedOut,
        beyondViewportPageCount = 1,
        pageContent = { index ->
            val item = data[index]
            // isFocused indicates whether this item is currently the focused item in the viewpager.
            // It's used to selectively apply properties (like shared element modifiers) only to the
            // focused item,
            // ensuring smooth animations and optimized performance by avoiding unnecessary modifications
            // to other items.
            val isFocused = state.currentPage == index
            val painter = Painter(
                ImageRequest(ctx).apply {
                    memoryCacheKey("${item.id}")
                    data(item.mediaUri)
                    if (item.isImage)  // Make sure that image is not loaded from Thumbnail repo.
                        preferCachedThumbnail(false)
                }.build(),
                onSuccess = {
                    val size = it.painter.intrinsicSize
                    Log.d(TAG, "Carousel success: ${size}")
                    if (isFocused)
                        scope.launch { zoomable.scaledInsideAndCenterAlignedFrom(size) }
                },
            )
            // if the user navigated to this item
            // TODO - maybe move to launched- effect.
            if (isFocused) {
                viewState.focused = item.id
                zoomable.scaledInsideAndCenterAlignedFrom(painter.intrinsicSize)
                Log.d(TAG, "Carousel focused: ${painter.intrinsicSize}")
            }
            // The content
            Image(
                painter = painter,
                contentDescription = item.name,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .thenIf(isFocused) {
                        when {
                            item.isImage -> zModifier
                            else -> playIconModifier.clickable(null, null) {
                                navController.navigate(
                                    RouteIntentViewer(
                                        item.mediaUri,
                                        item.mimeType
                                    )
                                )
                            }
                        } then RouteFiles.sharedElement(viewState.focused)
                    }
            )
        }
    )
}

/** Represents the MediaViewer Screen. */
@Composable
fun MediaViewer(viewState: MediaViewerViewState) {
    //
    var immersive by remember { mutableStateOf(false) }
    val compact = LocalWindowSize.current.width < Category.Medium
    val surface = rememberAcrylicSurface()
    val colors = AppTheme.colors
    //
    val facade = LocalSystemFacade.current
    val navController = LocalNavController.current
    val onRequest: (Int) -> Unit = { request: Int ->
        when (request) {
            // Toggle the visibility of detailed information.
            EVENT_SHOW_INFO -> viewState.showDetails = !viewState.showDetails;
            // Toggle immersive mode and update system UI accordingly.
            EVENT_IMMERSIVE_VIEW -> {
                immersive = !immersive
                facade.style += if (immersive) WindowStyle.FLAG_SYSTEM_BARS_HIDDEN else WindowStyle.FLAG_SYSTEM_BARS_VISIBLE
            }
            // Handle back press events, prioritizing focused states (immersive, details)
            // before navigating up.
            EVENT_BACK_PRESS if viewState.showDetails -> viewState.showDetails = false
            EVENT_BACK_PRESS if immersive -> {
                immersive = false
                facade.style += WindowStyle.FLAG_SYSTEM_BARS_VISIBLE
            }
            // Navigate up if no focused states
            EVENT_BACK_PRESS -> navController.navigateUp()
            // This must not happen.
            else -> error("Received unexpected event: $request")
        }
    }
    //
    DetailsViewDialog(
        viewState.details,
        colors.background(surface),
        onDismissRequest = { viewState.showDetails = false }
    )

    val isLoading by remember { derivedStateOf { viewState.data.isEmpty() } }
    val ctx = LocalContext.current
    Scaffold(
        fabPosition = if (compact) FabPosition.Center else FabPosition.End,
        contentColor = Color.SignalWhite,
        //
        containerColor = Color.Black,
        // TopAppBar
        topBar = {
            MediaViewerTopAppBar(
                visible = !immersive && !isLoading,
                title = viewState.title,
                provider = surface,
                navigationIcon = {
                    IconButton(
                        Icons.Outlined.ReplyAll,
                        contentDescription = "Back",
                        onClick = { onRequest(EVENT_BACK_PRESS) }
                    )
                },
                actions = {
                    IconButton(
                        icon = Icons.Default.Info,
                        contentDescription = null,
                        onClick = { onRequest(EVENT_SHOW_INFO) }
                    )
                },
            )
        },
        // FloatingActionMenu
        floatingActionButton = {
            val actions = viewState.actions
            FloatingActionMenu(
                visible = !immersive && !isLoading,
                background = colors.background(surface),
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
                ),
                border = colors.shine,
                content = {
                    OverflowMenu(
                        actions,
                        onItemClicked = { viewState.onPerformAction(it, ctx.findActivity()) },
                        collapsed = 5
                    )
                }
            )
        },
        // Carousal
        content = {
            // Ambient mode: applies a blur effect and noise overlay for an immersive background.
            // since content in placed inside of Box; so doing this is pretty normal
            val isAmbientModeEnabled by preference(Settings.KEY_VISUAL_EFFECT_MODE)
            if (isAmbientModeEnabled == 1) {
                // Transformation for blur effect, chosen based on Android version.
                val transformation = remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        ReBlurTransformation(25f, 2.1f)
                    else
                        RsBlurTransformation(ctx, 300f, 2.1f)
                }
                Crossfade(
                    // Crossfade animation for the ambient background when the focused item changes.
                    viewState.focused,
                    modifier = Modifier
                        .foreground(Color.Black.copy(0.82f))
                        .visualEffect(ImageBrush.NoiseBrush, 0.04f, true, BlendMode.Luminosity),
                    // Content of the Crossfade: an AsyncImage with blur and noise effects.
                    content = {
                        AsyncImage(
                            ImageRequest(ctx)
                                .data(MediaProvider.buildContentUri(it))
                                .size(256)
                                .transformations(transformation).build(),
                            null,
                            contentScale = ContentScale.Crop,
                            filterQuality = FilterQuality.None,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
            }
            // Modifier for the main content, applying surface properties and filling the maximum size.
            val modifier = Modifier.source(surface).fillMaxSize()
            // If data is loading, display a placeholder image with shared element transition.
            if (isLoading)
                AsyncImage(
                    contentDescription = null,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    model = ImageRequest(ctx)
                        .memoryCacheKey("${viewState.focused}")
                        .data(MediaProvider.buildContentUri(viewState.focused))
                        .preferCachedThumbnail(false)
                        .build(),
                    placeholder = Painter(
                        model = ImageRequest(ctx)
                            .data(MediaProvider.buildContentUri(viewState.focused))
                            .build(),
                    ),
                    modifier = modifier.then(RouteFiles.sharedElement(viewState.focused)),
                )
            // If data is loaded, display the Carousel with the media items.
            else Carousel(viewState, onRequest, modifier = modifier)
        }
    )

    // set/reset
    DisposableEffect(key1 = Unit) {
        //facade.enableEdgeToEdge(dark = false, translucent = false)
        val original = facade.style
        facade.style =
            original + WindowStyle.FLAG_SYSTEM_BARS_APPEARANCE_DARK + WindowStyle.FLAG_SYSTEM_BARS_BG_TRANSPARENT
        onDispose {
            // Reset to default on disposal
            facade.style = original
        }
    }
}