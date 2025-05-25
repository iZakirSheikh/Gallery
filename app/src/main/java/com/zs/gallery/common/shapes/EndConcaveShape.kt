package com.zs.gallery.common.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

private const val TAG = "EndConcaveShape"

/**
 * A shape that represents a rectangle with a concave top.
 *
 * @param radius the radius of the concave curve at the top of the shape.
 */
class EndConcaveShape(private val radius: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val (w, h) = size
        val radiusPx = with(density) { radius.toPx() }

        val path = Path().apply {
            if (layoutDirection == LayoutDirection.Ltr) {
                // Concave on the right side
                moveTo(w, 0f)
                quadraticTo(w - radiusPx, 0f, w - radiusPx, radiusPx)
                lineTo(w - radiusPx, h - radiusPx)
                quadraticTo(w - radiusPx, h, w, h)
                lineTo(0f, h)
                lineTo(0f, 0f)
                close()
            } else {
                // Concave on the left side (mirrored)
                moveTo(0f, 0f)
                quadraticTo(radiusPx, 0f, radiusPx, radiusPx)
                lineTo(radiusPx, h - radiusPx)
                quadraticTo(radiusPx, h, 0f, h)
                lineTo(w, h)
                lineTo(w, 0f)
                close()
            }
        }
        return Outline.Generic(path)
    }
}