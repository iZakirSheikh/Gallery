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

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.media3.ui.PlayerView

@SuppressLint("UnsafeOptInUsageError")
fun PlayerView(
    context: Context,
    controller: PlayerController,
    useController: Boolean = false,
): View = PlayerView(context).apply {
    this.player = controller.value
    this.useController = useController
    setShowFastForwardButton(true)
    setShowRewindButton(true)
    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
    setShowSubtitleButton(true)
    setShowVrButton(true)
    setShowShuffleButton(true)
    setShowNextButton(false)
    setShowPreviousButton(false)
    if (!useController)
        hideController()
}