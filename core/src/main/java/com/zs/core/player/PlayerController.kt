/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 28-04-2025.
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

package com.zs.core.player


import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer.Builder as ExoPlayer
import com.zs.core.player.PlayerController.Companion.STATE_BUFFERING
import com.zs.core.player.PlayerController.Companion.STATE_ENDED
import com.zs.core.player.PlayerController.Companion.STATE_IDLE
import com.zs.core.player.PlayerController.Companion.STATE_READY
import com.zs.core.player.PlayerController.Companion.TIME_UNSET
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A value class that wraps a [Player] instance and provides convenient access to its properties and methods.
 *
 * This class is designed to be used as a lightweight wrapper around the `Player` object, allowing you to interact with it
 * in a more concise and expressive way.
 *
 * @property value The underlying [Player] instance.
 * @property duration The duration of the media content in milliseconds.
 * @property position The current playback position in milliseconds.
 * @property state The current playback state of the [Player].
 * @property playWhenReady A boolean indicating whether the player should start playing when it becomes ready.
 * @property isPlaying A boolean indicating whether the player is currently playing.
 * @property onMediaStateChanged A [Flow] that emits a Unit value whenever the [Player.getPlaybackState] changes.
 *
 * @see rememberPlayerController
 */
@JvmInline
value class PlayerController internal constructor(internal val value: Player) {

    constructor(
        ctx: Context,
        handleAudioBecomingNoisy: Boolean = false,
        handleAudioFocus: Boolean = false,
    ) : this(
        value = ExoPlayer(ctx).apply {
            // Create a DefaultRenderersFactory with decoder fallback and extension renderers enabled
            val renderers = DefaultRenderersFactory(ctx).apply {
                setEnableDecoderFallback(true)
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            }

            // Create audio attributes for media playback
            val attrib = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build()
            //
            setRenderersFactory(renderers)
            setHandleAudioBecomingNoisy(handleAudioBecomingNoisy)
            setAudioAttributes(attrib, handleAudioFocus)
        }.build()
    )

    /**
     * @property TIME_UNSET Represents an unset time value.
     * @property STATE_IDLE Player is idle.
     * @property STATE_BUFFERING Player is buffering.
     * @property STATE_READY Player is ready to play.
     * @property STATE_ENDED Playback has ended.
     */
    companion object {
        val TIME_UNSET = C.TIME_UNSET
        val STATE_IDLE = Player.STATE_IDLE
        val STATE_BUFFERING = Player.STATE_BUFFERING
        val STATE_READY = Player.STATE_READY
        val STATE_ENDED = Player.STATE_ENDED
    }


    val duration get() = value.duration
    val position get() = value.currentPosition
    val state get() = value.playbackState
    var playWhenReady
        get() = value.playWhenReady
        set(value) {
            this.value.playWhenReady = value
        }
    val isPlaying get() = value.isPlaying

    /**
     * Emits a Unit value whenever the [Player.getPlaybackState] changes.
     *
     * **Note:** This property must be stored in a variable to be active, as it creates a
     * new [Flow] instance each time it's accessed.
     *
     * Usage:
     * ```
     * val mediaStateFlow = onMediaStateChanged
     * ```
     *
     * @see state
     */
    val onMediaStateChanged: Flow<Unit>
        get() = callbackFlow {
            // Listener to capture playback state changes
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    // Emit a unit value whenever the playback state changes
                    trySend(Unit)
                }
            }

            // Add the listener to the player
            value.addListener(listener)

            // Remove the listener when the flow is closed
            awaitClose { value.removeListener(listener) }
        }

    fun setMediaItem(url: Uri) = value.setMediaItem(MediaItem.fromUri(url))

    fun play(playWhenReady: Boolean = true) {
        this.playWhenReady = playWhenReady
        value.play()
    }

    fun pause() = value.pause()
    fun stop() = value.stop()
    fun release() = value.release()
    fun prepare() = value.prepare()
    fun clear() = value.clearMediaItems()
}
