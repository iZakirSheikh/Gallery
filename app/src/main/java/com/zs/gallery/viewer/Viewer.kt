@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zs.gallery.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.primex.core.SignalWhite
import com.primex.core.findActivity
import com.primex.material2.IconButton
import com.zs.domain.store.isImage
import com.zs.domain.store.mediaUri
import com.zs.foundation.AppTheme
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.Range
import com.zs.foundation.WindowSize
import com.zs.foundation.adaptive.HorizontalTwoPaneStrategy
import com.zs.foundation.adaptive.StackedTwoPaneStrategy
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.TwoPaneStrategy
import com.zs.foundation.adaptive.VerticalTwoPaneStrategy
import com.zs.foundation.menu.Menu
import com.zs.foundation.menu.MenuItem
import com.zs.foundation.sharedElement
import com.zs.foundation.thenIf
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import kotlinx.coroutines.runBlocking
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
private fun FloatingActionMenu(
    actions: List<MenuItem>,
    modifier: Modifier = Modifier,
    onAction: (action: MenuItem) -> Unit
) {
    Surface(
        modifier = modifier.scale(0.85f),
        color = AppTheme.colors.background(elevation = 2.dp),
        contentColor = AppTheme.colors.onBackground,
        shape = CircleShape,
        border = BorderStroke(1.dp, AppTheme.colors.background(elevation = 4.dp)),
        elevation = 12.dp,
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.animateContentSize()
            ) {
                Menu(actions, onItemClicked = onAction)
            }
        },
    )
}

@Composable
private fun TopAppBar(
    isInfoShowing: Boolean,
    onToggleInfo: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material.TopAppBar(
        title = {},
        navigationIcon = {
            val navController = LocalNavController.current
            IconButton(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                onClick = navController::navigateUp
            )
        },
        actions = {
            IconToggleButton(isInfoShowing, onToggleInfo) {
                Icon(
                    if (isInfoShowing) Icons.Filled.Info else Icons.Outlined.Info,
                    contentDescription = "info"
                )
            }
        },
        contentColor = Color.White,
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        modifier = modifier,
        windowInsets = WindowInsets.statusBars,
    )
}

private val ViewerViewState.index
    get() = if (data.isEmpty()) 0 else data.indexOfFirst { focused == it.id }
private val DEFAULT_ZOOM_SPECS = ZoomSpec(5f)

/**
 * Returns the strategy to use for displaying two panes based on the window size.
 */
private val WindowSize.strategy: TwoPaneStrategy
    get() {
        val (wClazz, hClazz) = this
        return when {
            // If the window is compact (e.g., 360 x 360 || 400 x 400),
            // use a stacked strategy with the dialog at the center.
            wClazz == Range.Compact && hClazz == Range.Compact -> StackedTwoPaneStrategy(0.5f)

            // If the width is greater than the height, use a horizontal strategy
            // that splits the window at 50% of the width.
            wClazz > hClazz -> HorizontalTwoPaneStrategy(0.5f)

            // If the height is greater than the width, use a vertical strategy
            // that splits the window at 50% of the height.
            else -> VerticalTwoPaneStrategy(0.5f)
        }
    }

@Composable
private fun Details(
    modifier: Modifier = Modifier
) {

}

@Composable
fun Viewer(
    viewState: ViewerViewState
) {

    val facade = LocalSystemFacade.current
    DisposableEffect(key1 = Unit) {
        facade.enableEdgeToEdge(dark = false, translucent = true)
        onDispose {
            // Reset to default on disposal
            facade.enableEdgeToEdge()
        }
    }

    var immersive by remember { mutableStateOf(false) }
    LaunchedEffect(immersive) {
        facade.enableEdgeToEdge(immersive, true, false)
    }

    val clazz = LocalWindowSize.current
    var showDetails by remember { mutableStateOf(false) }
    TwoPane(
        fabPosition = FabPosition.Center,
        strategy = clazz.strategy,
        background = Color.Black,
        onColor = Color.SignalWhite,
        content = {
            MainContent(
                viewState = viewState,
                onRequestImmersive = { immersive = !immersive },
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            )
        },
        topBar = {
            AnimatedVisibility(
                visible = !immersive,
                enter = fadeIn(),
                exit = fadeOut(),
                content = {
                    TopAppBar(showDetails, { showDetails = it })
                }
            )
        },
        details = details@{
            if (!showDetails) return@details
            Details()
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !immersive,
                modifier = Modifier.padding(bottom = AppTheme.padding.normal),
                enter = fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                content = {
                    val context = LocalContext.current
                    FloatingActionMenu(actions = viewState.actions) {
                        viewState.onAction(it, context.findActivity())
                    }
                }
            )
        }
    )
}


/**
 * Scales and centers content based on Painter's size.
 * @param painter Provides intrinsic size for scaling.
 */
private fun ZoomableState.scaledInsideAndCenterAlignedFrom(painter: Painter) {
    runBlocking {
        // Do nothing if intrinsic size is unknown
        if (painter.intrinsicSize.isUnspecified) return@runBlocking

        // Scale and center content based on intrinsic size
        // TODO - Make this suspend fun instead of runBlocking
        setContentLocation(
            ZoomableContentLocation.scaledInsideAndCenterAligned(
                painter.intrinsicSize
            )
        )
    }
}

@Composable
private fun MainContent(
    viewState: ViewerViewState,
    onRequestImmersive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO - Properly handle case, when data is empty
    val values = viewState.data
    if (values.isEmpty()) return

    // Construct the state variables for pager.
    val state = rememberPagerState(initialPage = viewState.index, pageCount = { values.size })
    val zoomableState = rememberZoomableState(DEFAULT_ZOOM_SPECS).apply {
        contentScale = ContentScale.None
    }
    // Modifier for zoomable images,
    // triggering immersive mode on click and handling double-tap zoom
    val zoomableModifier = Modifier.zoomable(
        zoomableState,
        onClick = { onRequestImmersive() },
        onDoubleClick = DoubleClickToZoomListener.cycle(2f)
    )

    // Horizontal pager to display the images/videos
    // Disable swipe when zoomed in
    // Preload adjacent pages for smoother transitions
    HorizontalPager(
        state = state,
        key = { values[it].id },
        pageSpacing = 16.dp,
        modifier = modifier,
        userScrollEnabled = zoomableState.zoomFraction == 0f,
        beyondViewportPageCount = 1,
    ) { index ->
        val item = values[index]

        // isFocused indicates whether this item is currently the focused item in the viewpager.
        // It's used to selectively apply properties (like shared element modifiers) only to the
        // focused item,
        // ensuring smooth animations and optimized performance by avoiding unnecessary modifications
        // to other items.
        val isFocused = state.currentPage == index

        // Constructs the painter for this item, handling both images and videos.
        // TODO: Allow user to choose image quality/filter for optimized loading.
        // In success state, update intrinsic size only if specified AND the item is focused.
        // This prevents glitches when transitioning zoom from a non-focused item.
        // For videos, load the preview image; otherwise, load the original image.
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .apply {
                    data(item.mediaUri)
                    if (item.isImage) {
                        size(Size.ORIGINAL)
                        // Cache images for better performance
                        memoryCacheKey("index:$index")
                    }
                    // Placeholder for errors
                    error(R.drawable.ic_error_image_placeholder)
                }.build(),
            filterQuality = FilterQuality.Low,
            onState = {
                if (it is AsyncImagePainter.State.Success && isFocused)
                    zoomableState.scaledInsideAndCenterAlignedFrom(it.painter)
            },
        )

        // if the user navigated to this item
        if (isFocused) {
            viewState.focused = item.id
            zoomableState.scaledInsideAndCenterAlignedFrom(painter)
        }
        // Display the image/video
        // Shared element transition for focused item
        // Apply zoom behavior only to focused, non-error images
        val sharedFrameKey = RouteViewer.buildSharedFrameKey(item.id)
        Image(
            painter = painter,
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .thenIf(isFocused, Modifier.sharedElement(sharedFrameKey))
                .thenIf(
                    item.isImage && isFocused && painter.state !is AsyncImagePainter.State.Error,
                    zoomableModifier
                )
                .fillMaxSize()
        )
    }
}