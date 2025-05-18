/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 07-05-2025.
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

package com.zs.gallery.viewer

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReplyAll
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.FabPosition
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.core.coil.preferCachedThumbnail
import com.zs.gallery.common.FallbackWallpaperIntent
import com.zs.gallery.common.GoogleLensIntent
import com.zs.gallery.common.NearByShareIntent
import com.zs.gallery.common.ShareFilesIntent
import com.zs.gallery.common.WallpaperIntent
import com.zs.gallery.common.WindowStyle
import com.zs.gallery.common.compose.FloatingActionMenu
import com.zs.gallery.common.compose.LocalNavController
import com.zs.gallery.common.compose.LocalSystemFacade
import com.zs.gallery.common.compose.PlayerView
import com.zs.gallery.common.compose.background
import com.zs.gallery.common.compose.rememberAcrylicSurface
import com.zs.gallery.common.compose.shine
import com.zs.gallery.common.compose.source
import com.zs.gallery.common.icons.NearbyShare
import com.zs.gallery.common.scaledInsideAndCenterAlignedFrom
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.zoomable
import coil3.request.ImageRequest.Builder as ImageRequest
import com.zs.gallery.common.compose.rememberPlayerController as Controller
import me.saket.telephoto.zoomable.rememberZoomableState as ZoomableState

private const val TAG = "IntentViewer"

/**
 * Represents the media player screen for playing local/non-local media files.
 */
@Composable
private fun MediaPlayer(uri: Uri) {
    // Create a PlayerController instance.
    val controller =
        Controller(true, true)
    // Display the video using PlayerView.
    PlayerView(
        controller,
        Modifier.fillMaxSize(),
        Color.Black,
        true,
        true
    )
    val facade = LocalSystemFacade.current
    // Use DisposableEffect to manage resources that need to be set up and torn down.
    DisposableEffect(key1 = Unit) {
        // This block runs when the composable is first displayed.
        //facade.enableEdgeToEdge(dark = false, translucent = false)
        // Store the original window style to restore it later.
        val original = facade.style
        // Hide the system bars (status bar and navigation bar) for a full-screen video experience.
        facade.style = original + WindowStyle.FLAG_SYSTEM_BARS_HIDDEN
        // Set the media item (the video URI) to the controller.
        // Prepare the player for playback.
        // Start playing the video.
        controller.setMediaItem(uri)
        controller.prepare()
        controller.play(true)
        // This block runs when the composable leaves the composition (e.g., the screen is closed).
        onDispose {
            // Reset to default on disposal
            // Release the player resources to prevent memory leaks.
            facade.style = original
            controller.release()
        }
    }
}

private val DEFAULT_ZOOM_SPECS = ZoomSpec(5f)

/**  Indicates whether the content is currently at its default zoom level (not zoomed in). */
private val ZoomableState.isZoomedOut get() = (zoomFraction ?: 0f) <= 0.0001f

@Composable
private fun ImageViewer(uri: Uri) {
    // Obtain the system facade for interacting with window properties.
    val facade = LocalSystemFacade.current
    val zoomable =
        ZoomableState(DEFAULT_ZOOM_SPECS).apply { contentScale = ContentScale.Fit }
    val surface = rememberAcrylicSurface()
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    // Handle back presses
    var immersive by remember { mutableStateOf(false) }
    val onBackClick: () -> Unit = {
        when {
            !zoomable.isZoomedOut -> scope.launch { zoomable.resetZoom() }
            immersive -> immersive = false
            navController.previousBackStackEntry == null -> (facade as? Activity)?.finish()
            else -> navController.navigateUp()
        }
    }

    BackHandler(immersive || !zoomable.isZoomedOut, onBack = onBackClick)

    // Scaffold for basic screen structure.
    val (width, _) = LocalWindowSize.current
    val compact = width < Category.Medium
    Scaffold(
        fabPosition = if (compact) FabPosition.Center else FabPosition.End,
        // TODO - Provide a way to add dynamic blur to this.
        containerColor = Color.Black,
        topBar = {
            MediaViewerTopAppBar(
                !immersive,
                "",
                surface,
                navigationIcon = {
                    IconButton(
                        icon = Icons.AutoMirrored.Outlined.ReplyAll, // Back icon.
                        contentDescription = null,
                        onClick = onBackClick
                    )
                }
            )
        },
        content = {
            // AsyncImage for loading and displaying the image from the URI.
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest(context)
                    .data(uri)
                    .size(coil3.size.Size.ORIGINAL)
                    .preferCachedThumbnail(false)
                    .build(),
                contentDescription = null,
                // Scale and center the image after it's loaded.
                onSuccess = { zoomable.scaledInsideAndCenterAlignedFrom(it.painter.intrinsicSize) },
                placeholder = rememberAsyncImagePainter(
                    model = ImageRequest(context)
                        .data(uri)
                        .build(),
                ),
                alignment = Alignment.Center,
                contentScale = ContentScale.None,
                modifier = Modifier
                    .fillMaxSize()
                    .source(surface)
                    .zoomable(
                        state = zoomable,
                        onClick = {
                            immersive = !immersive
                            facade.style += if (immersive) WindowStyle.FLAG_SYSTEM_BARS_HIDDEN else WindowStyle.FLAG_SYSTEM_BARS_VISIBLE
                        },
                        onDoubleClick = DoubleClickToZoomListener.cycle(2f)
                    )
            )

        },
        floatingActionButton = {
            val colors = AppTheme.colors
            FloatingActionMenu(
                visible = !immersive,
                background = colors.background(surface),
                contentColor = AppTheme.colors.onBackground,
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
                ),
                border = colors.shine,
                content = {
                    // Nearby share
                    IconButton(
                        Icons.Default.NearbyShare,
                        contentDescription = "share",
                        onClick = { facade.launch(NearByShareIntent(uri)) }
                    )
                    // wallpaper
                    IconButton(
                        Icons.Default.Wallpaper,
                        contentDescription = "Use as",
                        onClick = {
                            val res = runCatching {
                                facade.launch(WallpaperIntent(uri))
                            }
                            if (res.isFailure) facade.launch(FallbackWallpaperIntent(uri))
                        }
                    )
                    // scan
                    IconButton(
                        Icons.Default.DocumentScanner,
                        contentDescription = "Visual search",
                        onClick = { facade.launch(GoogleLensIntent(uri)) }
                    )

                    //share
                    IconButton(
                        Icons.Outlined.Share,
                        contentDescription = "Share",
                        onClick = { facade.launch(ShareFilesIntent(uri)) }
                    )
                }
            )
        }
    )

    DisposableEffect(key1 = Unit) {
        // This block runs when the composable is first displayed.
        // Store the original window style to restore it later.
        val original = facade.style
        // Hide the system bars (status bar and navigation bar) for a full-screen video experience.
        facade.style = original + WindowStyle.FLAG_SYSTEM_BARS_APPEARANCE_DARK
        // This block runs when the composable leaves the composition (e.g., the screen is closed).
        onDispose {
            // Reset to default on disposal
            // Release the player resources to prevent memory leaks.
            facade.style = original
        }
    }
}

@Composable
@NonRestartableComposable
fun IntentViewer(value: Pair<Uri, String>) {
    val (contentUri, mime) = value
    when {
        mime.startsWith("video/") -> MediaPlayer(contentUri)
        else -> ImageViewer(contentUri)
    }
}