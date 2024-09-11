package com.zs.foundation.shapes

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
            moveTo(w, 0f)
            quadraticTo(w -radiusPx, 0f, w - radiusPx, radiusPx)
            lineTo(w-radiusPx, h- radiusPx)
            quadraticTo(w - radiusPx, h, w, h)
            lineTo(0f, h)
            lineTo(0f, 0f)
            lineTo(w, 0f)
        }
        return Outline.Generic(path)
    }
}