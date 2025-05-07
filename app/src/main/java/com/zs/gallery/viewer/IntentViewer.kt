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

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.bundle.Bundle
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.appbar.TopAppBar
import com.zs.core.coil.preferCachedThumbnail
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.Route
import com.zs.gallery.common.WindowStyle
import com.zs.gallery.common.compose.PlayerView
import com.zs.gallery.common.compose.background
import com.zs.gallery.common.compose.observe
import com.zs.gallery.common.compose.rememberBackgroundProvider
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.zoomable
import com.zs.gallery.common.compose.rememberPlayerController as Controller
import me.saket.telephoto.zoomable.rememberZoomableState as ZoomableState

private const val TAG = "IntentViewer"

// Define the Route for this screen.
object RouteIntentViewer : Route {

    const val PARAM_URI = "param_uri"
    const val PARAM_MIME = "param_mime"

    override val route = "$domain/{${PARAM_URI}}/{${PARAM_MIME}}"

    operator fun invoke(uri: Uri, mime: String) =
        "$domain/${Uri.encode(uri.toString())}/${Uri.encode(mime)}"

    fun buildArgs(bundle: Bundle): Pair<Uri, String> {
        val uri = bundle.getString(PARAM_URI)!!.toUri()
        val mime = bundle.getString(PARAM_MIME)
        return uri to (mime ?: "image/*")
    }
}

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
@NonRestartableComposable
fun IntentViewer(value: Pair<Uri, String>) {
    val (contentUri, mime) = value
    when {
        mime.startsWith("video/") -> MediaPlayer(contentUri)
        else -> ImageViewer(contentUri)
    }
}

@Composable
private fun ImageViewer(uri: Uri) {

    // Obtain the system facade for interacting with window properties.
    val facade = LocalSystemFacade.current
    val zoomable = ZoomableState(DEFAULT_ZOOM_SPECS)
        .apply { contentScale = ContentScale.Fit }
    // Remember the background provider for potential background effects.
    val observer = rememberBackgroundProvider()

    // State variable to track immersive mode.
    var immersive by remember { mutableStateOf(false) }

    // Scaffold for basic screen structure.
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            AnimatedVisibility(
                visible = !immersive, // Animations for the top app bar visibility.
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                content = {
                    // Top app bar for navigation and potential actions.
                    TopAppBar(
                        background = AppTheme.colors.background(
                            observer,
                            Color.Transparent,
                            progressive = 1f,
                            blurRadius = 60.dp,
                            tint = Color.Black.copy(0.3f),
                            blendMode = BlendMode.Multiply
                        ),
                        contentColor = Color.SignalWhite,
                        elevation = 0.dp,
                        navigationIcon = {
                            val navController = LocalNavController.current
                            IconButton(
                                icon = Icons.AutoMirrored.Outlined.ArrowBack, // Back icon.
                                contentDescription = null,
                                onClick = navController::navigateUp
                            )
                        },
                        title = { Spacer(Modifier) }
                    )
                }
            )
        },
        content = {
            // AsyncImage for loading and displaying the image from the URI.
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .preferCachedThumbnail(false)
                    .build(),
                contentDescription = null,
                // Scale and center the image after it's loaded.
                onSuccess = { zoomable.scaledInsideAndCenterAlignedFrom(it.painter.intrinsicSize) },
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .observe(observer)
                    .zoomable(
                        state = zoomable,
                        onClick = {
                            immersive = !immersive
                            facade.style += if (immersive) WindowStyle.FLAG_SYSTEM_BARS_HIDDEN else WindowStyle.FLAG_SYSTEM_BARS_VISIBLE
                        },
                        onDoubleClick = DoubleClickToZoomListener.cycle(2f)
                    )
            )
        }
    )
    // DisposableEffect for managing side effects, like changing window styles.
    // This effect runs once when the composable enters the composition and cleans up when it leaves.
    DisposableEffect(Unit) {
        val original = facade.style
        facade.style =
            original + WindowStyle.FLAG_SYSTEM_BARS_APPEARANCE_DARK + WindowStyle.FLAG_SYSTEM_BARS_BG_TRANSPARENT
        onDispose {
            // Reset to default on disposal
            facade.style = original
        }
    }
}