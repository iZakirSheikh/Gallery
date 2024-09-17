/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 17-09-2024.
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

package com.zs.foundation.player

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A value class that wraps a [Player] instance and provides convenient access to its properties and methods.
 *
 * This class is designed to be used as a lightweight wrapper around the `Player` object, allowing you to interact with it
 * in a more concise and expressive way.
 *
 * @property value The underlying [Player] instance.
 * @see rememberPlayerController
 */
@JvmInline
value class PlayerController internal constructor(internal val value: Player){

    companion object {
        /**
         * Represents an unset time value.
         * @see C.TIME_UNSET
         */
        val TIME_UNSET = C.TIME_UNSET

        /**
         * Player is idle.
         * @see Player.STATE_IDLE
         */
        val STATE_IDLE = Player.STATE_IDLE

        /**
         * Player is buffering.
         * @see Player.STATE_BUFFERING
         */
        val STATE_BUFFERING = Player.STATE_BUFFERING

        /**
         * Player is ready to play.
         * @see Player.STATE_READY
         */
        val STATE_READY = Player.STATE_READY

        /**
         * Playback has ended.
         * @see Player.STATE_ENDED
         */
        val STATE_ENDED = Player.STATE_ENDED
    }

    /**
     * @see Player.getDuration
     */
    val duration get() = value.duration

    /**
     * @see Player.getCurrentPosition
     */
    val position get() = value.currentPosition

    /**
     * @see Player.getPlaybackState
     */
    val state get() = value.playbackState

    /**
     * @see Player.getPlayWhenReady
     */
    var playWhenReady get() = value.playWhenReady
        set(value) { this.value.playWhenReady = value }

    /**
     * @see Player.isPlaying
     */
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
    val onMediaStateChanged: Flow<Unit> get() = callbackFlow {
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


    fun load(url: Uri){
        value.setMediaItem(MediaItem.fromUri(url))
        value.prepare()
    }

    fun play(playWhenReady: Boolean = true){
        this.playWhenReady = playWhenReady
        value.play()
    }

    fun pause() = value.pause()
    fun stop() = value.stop()
}

/**
 * Creates and remembers a [PlayerController] instance.
 */
@Composable
@NonRestartableComposable
fun rememberPlayerController(): PlayerController {
    val context = LocalContext.current
   return remember {
       PlayerController(
           value = ExoPlayer.Builder(context).build()
       )
   }
}