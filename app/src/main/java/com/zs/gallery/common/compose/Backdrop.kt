package com.zs.gallery.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.shadow.Shadow

typealias BackdropHandle = LayerBackdrop

@Composable
inline fun rememberBackdropHandle(): BackdropHandle = rememberLayerBackdrop()

inline fun Modifier.backgdrop(handle: BackdropHandle) = layerBackdrop(handle)

inline fun Modifier.backropEffect(
    shape: Shape,
    elevation: Dp,
    handle: BackdropHandle
) = drawBackdrop(
    shadow = { Shadow (elevation )},
    shape = {shape},
    backdrop = handle,
    effects = {
        vibrancy()
        blur(4f.dp.toPx())
        lens(16f.dp.toPx(), 32f.dp.toPx())
    }
)