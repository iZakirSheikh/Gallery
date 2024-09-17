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

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.zs.foundation.R
import com.zs.foundation.player.PlayerController

private const val TAG = "PlayerView"

/**
 * A composable function that displays a [PlayerView] for media playback.
 *
 * This function provides a convenient way to integrate an ExoPlayer view into your Jetpack Compose UI.
 * It takes a [PlayerController] instance as input and configures the `PlayerView` accordingly.
 *
 * @param controller The [PlayerController] instance to use for media playback.
 * @param modifier The modifier to apply to the `PlayerView`.
 * @param background The background color of the `PlayerView`. Defaults to black.
 * @param keepScreenOn Whether to keep the screen on during playback. Defaults to false.
 * @param useController Whether to display the default playback controls. Defaults to false.
 */
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun PlayerView(
    controller: PlayerController,
    modifier: Modifier = Modifier,
    background: Color = Color.Black,
    keepScreenOn: Boolean = false,
    useController: Boolean = false,
) = AndroidView(
    modifier = modifier,
    onRelease = {
        Log.d(TAG, "PlayerView: releasing")
        it.player = null
    },
    factory = { context ->
        Log.d(TAG, "PlayerView: creating")
        PlayerView(context).apply {
            player = controller.value
            clipToOutline = true
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    },
    update = { playerView ->
        Log.d(TAG, "PlayerView: updating")
        Log.d(TAG, "PlayerView: updating")
        playerView.setBackgroundColor(background.toArgb())
        playerView.keepScreenOn = keepScreenOn
        playerView.useController = useController
        playerView.setShowFastForwardButton(true)
        playerView.setShowRewindButton(true)
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
        playerView.setShowSubtitleButton(true)
        playerView.setShowVrButton(true)
        playerView.setShowShuffleButton(true)
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)

        playerView.fitsSystemWindows = true
        if (!useController)
            playerView.hideController()
    }
)