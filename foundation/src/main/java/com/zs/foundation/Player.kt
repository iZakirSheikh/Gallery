package com.zs.foundation

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import kotlin.math.roundToInt

private inline fun MediaItem(uri: Uri, id: String) = MediaItem.Builder().setUri(uri).setMediaId(id).build();


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun Player(
    uri: Uri,
    modifier: Modifier = Modifier,
    playOnReady: Boolean = true,
    useBuiltInController: Boolean = true,
) {
    val density = LocalDensity.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                player = ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem(uri, uri.toString()))
                    prepare()
                    playWhenReady = playOnReady
                    useController = useBuiltInController
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    clipToOutline = true
                    // Set the Background Color of the player as Solid Black Color.
                    setBackgroundColor(Color.Black.toArgb())
                    val controller = findViewById<PlayerControlView>(androidx.media3.ui.R.id.exo_controller)
                    val padding =  with(density){
                        16.dp.roundToPx()
                    }
                    controller.setPadding(padding, padding, padding, padding)
                }
            }
        },
        onRelease = {
            it.player?.release()
            it.player = null
        },
        update = {view ->
            view.useController = useBuiltInController
            val player = view.player ?: return@AndroidView
            if (player.currentMediaItem?.mediaId != uri.toString()) {
                player.clearMediaItems()
                player.setMediaItem(MediaItem(uri, uri.toString()))
                player.prepare()
            }
            player.playWhenReady = playOnReady
        }
    )
}