package com.zs.gallery.common.compose

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
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
import com.zs.compose.foundation.ImageBrush
import com.zs.compose.foundation.visualEffect
import com.zs.gallery.common.Res

typealias BackdropHandle = LayerBackdrop

@Composable
inline fun rememberBackdropHandle(): BackdropHandle = rememberLayerBackdrop()

inline fun Modifier.backgdrop(handle: BackdropHandle) = layerBackdrop(handle)

inline fun Modifier.backropEffect(
    shape: Shape,
    elevation: Dp,
    handle: BackdropHandle
) = drawBackdrop(
    shadow = { Shadow(elevation) },
    shape = { shape },
    backdrop = handle,
    effects = {
        vibrancy()
        blur(4f.dp.toPx())
        lens(16f.dp.toPx(), 32f.dp.toPx())
    }
)

/**
 * Constructs a fallback acrylic surface form [containerColor] and [accent]
 */
fun Modifier.acrylic(
    elevation: Dp,
    shape: Shape = Res.shape.rectangle,
    containerColor: Color,
    accent: Color
) =
    shadow(elevation = elevation, shape = shape)
        .background(containerColor)
        .graphicsLayer(
            compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen,
        )
        .drawWithCache() {
            val paint = Paint().apply {
                style = PaintingStyle.Stroke
                // Add blur effect on the Paint (but check if it renders well on lines)
                this.asFrameworkPaint().maskFilter =
                    BlurMaskFilter(200f, BlurMaskFilter.Blur.NORMAL)
            }
            // Algorithm
            // This effect simulates the appearance of frosted glass or acrylic material. It involves:
            // 1. Drawing a base rectangle with the [containerColor].
            // 2. Drawing a series of blurred circles with varying colors and opacities, based on the [accent] color
            //    and the luminance of the [containerColor].
            // 3. Drawing an overlay rectangle with a slightly transparent version of the [containerColor].
            // 4. Applying a noise texture visual effect on top.
            // The positions and sizes of the circles are determined dynamically based on the dimensions
            // of the drawing area.
            onDrawBehind {
                // Determine if the container color is light based on its luminance.
                val isLight = containerColor.luminance() >= 0.5f

                // Draw the main rectangle with the container color.
                // drawRect(containerColor)

                // Get the width and height of the drawing area and calculate the diameter for circles.
                val (w, h) = size
                val vertical = w < h
                val dp = if (vertical) h / 8 else w / 8

                // Set the paint color to accent and adjust the stroke width.
                paint.color = accent
                paint.strokeWidth = dp * 0.7f
                // Draw the first circle with the accent color.
                var x = if (!vertical) 2 * dp else size.center.x
                var y = if (!vertical) size.center.y else 2 * dp
                this.drawContext.canvas.drawCircle(
                    Offset(x, y),
                    dp,
                    paint
                )

                // Change the paint color based on whether the container color is light or dark.
                paint.color = if (isLight) Color.Black else Color.White.copy(0.5f)
                // Draw the second circle with the modified color.
                x = if (!vertical) 3 * dp else size.center.x
                y = if (!vertical) size.center.y else 3 * dp
                this.drawContext.canvas.drawCircle(
                    Offset(x, y),
                    dp,
                    paint
                )

                // Set the paint color to a slightly transparent version of the accent color.
                paint.color = accent.copy(0.7f)
                // Draw the third circle with the adjusted accent color.
                x = if (!vertical) 5 * dp else size.center.x
                y = if (!vertical) size.center.y else 5 * dp
                this.drawContext.canvas.drawCircle(
                    Offset(x, y),
                    dp,
                    paint
                )

                // Reset the paint color to the original accent color.
                paint.color = accent

                // Draw the fourth circle with the accent color.
                x = if (!vertical) 8 * dp else size.center.x
                y = if (!vertical) size.center.y else 8 * dp
                this.drawContext.canvas.drawCircle(
                    Offset(x, y),
                    dp,
                    paint
                )

                // Set the paint color to black.
                paint.color = Color.Black
                // Draw the fifth circle with black color.
                x = if (!vertical) 10 * dp else size.center.x
                y = if (!vertical) size.center.y else 10 * dp
                this.drawContext.canvas.drawCircle(
                    Offset(x, y),
                    dp,
                    paint
                )

                // Draw the top rectangle with a slightly transparent version of the container color.
                // drawRect(containerColor.copy(if (isLight) 0.73f else 0.60f))
                // Finally, apply the luminosity effect.
                drawRect(
                    color = Color.White.copy(alpha = if (isLight) 0.80f else 0.90f),
                    // DstOut blend mode creates a cutout effect, enhancing luminosity.
                    blendMode = BlendMode.DstOut
                ) // Alpha is adjusted based on light/dark theme.
            }
        }
        .visualEffect(ImageBrush.NoiseBrush, if (containerColor.luminance() > 0.5f) 0.1f else 0.05f)


