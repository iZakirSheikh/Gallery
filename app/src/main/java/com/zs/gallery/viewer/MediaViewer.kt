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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.foundation.UmbraGrey
import com.zs.compose.foundation.findActivity
import com.zs.compose.foundation.plus
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.adaptive.FabPosition
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.contentInsets
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.appbar.TopAppBar
import com.zs.compose.theme.sharedBounds
import com.zs.compose.theme.sharedElement
import com.zs.compose.theme.text.Label
import com.zs.core.coil.preferCachedThumbnail
import com.zs.core.player.PlayerController
import com.zs.core.store.MediaProvider
import com.zs.gallery.common.DefaultBoundsTransform
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.WindowStyle
import com.zs.gallery.common.compose.FloatingActionMenu
import com.zs.gallery.common.compose.OverflowMenu
import com.zs.gallery.common.compose.PlayerView
import com.zs.gallery.common.compose.background
import com.zs.gallery.common.compose.rememberBackgroundProvider
import com.zs.gallery.common.compose.rememberPlayerController
import com.zs.gallery.files.RouteFiles
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.zoomable
import androidx.compose.foundation.pager.rememberPagerState as PagerState
import androidx.compose.ui.graphics.vector.rememberVectorPainter as VectorPainter
import coil3.request.ImageRequest.Builder as ImageRequest
import me.saket.telephoto.zoomable.rememberZoomableState as ZoomableState

private const val TAG = "MediaViewer"

private val MediaViewerViewState.index
    get() = data.indexOfFirst { focused == it.id }

private const val EVENT_BACK_PRESS = 0
private const val EVENT_SHOW_INFO = 1
private const val EVENT_IMMERSIVE_VIEW = 2

/**
 * The background color of the app-bar
 */
private val AppBarOverlay = Brush.verticalGradient(listOf(Color.Black, Color.Transparent))
private val DEFAULT_ZOOM_SPECS = ZoomSpec(5f)

/**  Indicates whether the content is currently at its default zoom level (not zoomed in). */
private val ZoomableState.isZoomedOut get() = (zoomFraction ?: 0f) <= 0.0001f

/**  Scales and centers content based on size. */
private fun ZoomableState.scaledInsideAndCenterAlignedFrom(size: Size) {
    // Do nothing if intrinsic size is unknown
    if (size.isUnspecified) return

    // Scale and center content based on intrinsic size
    // TODO - Make this suspend fun instead of runBlocking
    setContentLocation(
        ZoomableContentLocation.scaledInsideAndCenterAligned(
            size
        )
    )
}

@Composable
private fun Carousel(
    viewState: MediaViewerViewState,
    controller: PlayerController,
    onRequest: (request: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = viewState.data
    val ctx = LocalContext.current
    // if data is still being loaded;
    // early return with showing the focused image only.
    if (data.isEmpty())
        return Image(
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            painter = rememberAsyncImagePainter(
                model = ImageRequest(ctx)
                    .data(MediaProvider.contentUri(viewState.focused))
                    .build(),
            ),
            modifier = modifier.sharedBounds(
                RouteFiles.buildSharedFrameKey(viewState.focused),
                boundsTransform = AppTheme.DefaultBoundsTransform
            ),
        )

    // else load real data- wtih current being the focused one.
    // Construct the state variables for pager.
    val pager = PagerState(initialPage = viewState.index, pageCount = { data.size })
    val scope = rememberCoroutineScope()
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

    // State to control the visibility of the PlayerView
    // Lazily create the PlayerView content
    var showVideoView by remember { mutableStateOf(false) }
    val videoView by remember {
        lazy {
            movableContentOf {
                PlayerView(
                    controller,
                    Modifier.fillMaxSize(),
                    Color.Transparent,
                    true,
                    true
                )
            }
        }
    }

    // Handle BackPress
    BackHandler {
        when {
            // pause if playing
            controller.isPlaying -> controller.pause()
            // hide videoView
            showVideoView -> {
                onRequest(EVENT_IMMERSIVE_VIEW) // reset immersive-view
                controller.pause()
                showVideoView = false // hide video-view.
            }
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
    HorizontalPager(
        state = pager,
        key = { data[it].id },
        pageSpacing = 16.dp,
        modifier = modifier,
        userScrollEnabled = zoomable.isZoomedOut && !showVideoView,
        beyondViewportPageCount = 1,
        pageContent = { index ->
            val item = data[index]
            // isFocused indicates whether this item is currently the focused item in the viewpager.
            // It's used to selectively apply properties (like shared element modifiers) only to the
            // focused item,
            // ensuring smooth animations and optimized performance by avoiding unnecessary modifications
            // to other items.
            val isFocused = pager.currentPage == index
            // if the user navigated to this item
            // TODO - maybe move to launched- effect.
            if (isFocused) {
                viewState.focused = item.id
                zoomable.scaledInsideAndCenterAlignedFrom(
                    Size(item.width.toFloat(), item.height.toFloat())
                )
                controller.clear()
                if (!item.isImage) {
                    controller.setMediaItem(item.mediaUri)
                    controller.prepare()
                }
            }
            // The content
            when {
                isFocused && showVideoView -> videoView()
                else -> AsyncImage(
                    model = ImageRequest(ctx).apply {
                        memoryCacheKey("${item.id}")
                        data(item.mediaUri)
                        if (item.isImage)  // Make sure that image is not loaded from Thumbnail repo.
                            preferCachedThumbnail(false)
                    }.build(),
                    onSuccess = {
                        val size = it.painter.intrinsicSize
                        if (isFocused)
                            scope.launch { zoomable.scaledInsideAndCenterAlignedFrom(size) }
                    },
                    placeholder = rememberAsyncImagePainter(
                        model = ImageRequest(ctx)
                            .apply {
                                data(MediaProvider.contentUri(item.id))
                            }.build(),
                    ),
                    contentDescription = item.name,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .thenIf(isFocused) {
                            when {
                                item.isImage -> zModifier
                                else -> playIconModifier.clickable(null, null) {
                                    onRequest(EVENT_IMMERSIVE_VIEW)
                                    controller.play(true)
                                    showVideoView = true
                                }
                            } then Modifier.sharedBounds(
                                RouteFiles.buildSharedFrameKey(
                                    item.id
                                ),
                                boundsTransform = AppTheme.DefaultBoundsTransform
                            )
                        }
                        .fillMaxSize()
                )
            }
        }
    )
}

@Composable
fun AppBar(
    visible: Boolean,
    title: CharSequence,
    state: HazeState,
    onRequest: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        content = {
            TopAppBar(
                null,
                navigationIcon = {
                    IconButton(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        onClick = { onRequest(EVENT_BACK_PRESS) }
                    )
                },
                title = { Label(title, maxLines = 2) },
                actions = {
                    IconButton(
                        icon = Icons.Default.Info,
                        contentDescription = null,
                        onClick = { onRequest(EVENT_SHOW_INFO) }
                    )
                },
                style = AppBarDefaults.topAppBarStyle(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    contentColor = Color.SignalWhite,
                    scrolledContentColor = Color.SignalWhite
                ),
                modifier = modifier.pointerInput(Unit, {}).background(
                    provider = state,
                    Color.Transparent,
                    progressive = 1f,
                    blurRadius = 60.dp,
                    tint = Color.Black.copy(0.3f),
                    blendMode = BlendMode.Multiply
                ),
            )
        },
    )
}

/** Represents the MediaViewer Screen. */
@Composable
fun MediaViewer(viewState: MediaViewerViewState) {
    // Define required state variables
    val clazz = LocalWindowSize.current
    var immersive by remember { mutableStateOf(false) }
    val facade = LocalSystemFacade.current
    val navController = LocalNavController.current
    val onRequest: (Int) -> Unit = { request: Int ->
        when (request) {
            // Toggle the visibility of detailed information.
            EVENT_SHOW_INFO -> {
                viewState.showDetails = !viewState.showDetails;
                immersive = viewState.showDetails
            }
            // Toggle immersive mode and update system UI accordingly.
            EVENT_IMMERSIVE_VIEW -> {
                immersive = !immersive
                facade.style += if (immersive) WindowStyle.FLAG_SYSTEM_BARS_HIDDEN else WindowStyle.FLAG_SYSTEM_BARS_VISIBLE
            }
            // Handle back press events, prioritizing focused states (immersive, details)
            // before navigating up.
            EVENT_BACK_PRESS -> {
                when {
                    // consume in making not immersive
                    immersive -> {
                        immersive = false
                        facade.style += WindowStyle.FLAG_SYSTEM_BARS_VISIBLE
                    }
                    // consume in hiding the details this action
                    viewState.showDetails -> viewState.showDetails = false
                    // Navigate up if no focused states
                    else -> navController.navigateUp()
                }
            }
            // Handle unexpected events
            else -> error("Unknown event: $request")
        }
    }

    val portrait = clazz.width < clazz.height
    val ctx = LocalContext.current

    // The player controller
    // initialized here because we can destroy it once the view is destroyed.
    val controller =
        rememberPlayerController(true, true)
    val observer = rememberBackgroundProvider()

    // Actual content
    Scaffold(
        fabPosition = if (portrait) FabPosition.Center else FabPosition.End,
        onColor = Color.SignalWhite,
        background = Color.Black,
        topBar = { AppBar(!immersive, viewState.title, observer, onRequest) },
        primary = {
            Carousel(
                viewState = viewState,
                controller = controller,
                onRequest = onRequest,
                modifier = Modifier
                    .hazeSource(observer)
                    .fillMaxSize()
            )
        },
        floatingActionButton = {
            val actions = viewState.actions
            FloatingActionMenu(
                visible = !immersive && !actions.isEmpty(),
                background = Color.Transparent,
                contentColor = Color.UmbraGrey,
                insets = WindowInsets.contentInsets + WindowInsets.safeContent.asPaddingValues(),
                modifier = Modifier.background(observer, Color.SignalWhite),
                content = {
                    val ctx = LocalContext.current
                    OverflowMenu(
                        actions,
                        onItemClicked = { viewState.onAction(it, ctx.findActivity()) },
                        collapsed = 5
                    )
                }
            )
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
            controller.release()
        }
    }
}