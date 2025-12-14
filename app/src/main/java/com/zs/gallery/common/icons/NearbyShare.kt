package com.zs.gallery.common.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Icons.Filled.NearbyShare: ImageVector
    get() {
        if (_nearbyShare != null) {
            return _nearbyShare!!
        }
        _nearbyShare =
            Builder(
                    name = "NearbyShare",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                )
                .apply {
                    path(
                        fill = SolidColor(Color(0xFF505050)),
                        stroke = null,
                        strokeLineWidth = 0.0f,
                        strokeLineCap = Butt,
                        strokeLineJoin = Miter,
                        strokeLineMiter = 4.0f,
                        pathFillType = NonZero,
                    ) {
                        moveTo(19.886f, 8.003f)
                        arcToRelative(1.1f, 1.1f, 0.0f, false, false, -0.774f, 0.318f)
                        lineToRelative(-2.23f, 2.173f)
                        lineToRelative(-1.546f, 1.5f)
                        lineToRelative(-0.96f, 0.934f)
                        arcToRelative(3.3f, 3.3f, 0.0f, false, true, -1.083f, 0.71f)
                        arcToRelative(3.4f, 3.4f, 0.0f, false, true, -1.29f, 0.245f)
                        arcToRelative(3.3f, 3.3f, 0.0f, false, true, -2.152f, -0.776f)
                        lineToRelative(-1.535f, 1.5f)
                        arcToRelative(5.6f, 5.6f, 0.0f, false, false, 3.687f, 1.376f)
                        curveToRelative(1.501f, 0.0f, 2.903f, -0.556f, 3.951f, -1.585f)
                        lineToRelative(0.929f, -0.903f)
                        lineToRelative(1.545f, -1.501f)
                        lineToRelative(2.263f, -2.203f)
                        curveToRelative(0.202f, -0.202f, 0.313f, -0.471f, 0.309f, -0.75f)
                        arcToRelative(1.04f, 1.04f, 0.0f, false, false, -0.332f, -0.74f)
                        curveToRelative(-0.215f, -0.2f, -0.5f, -0.3f, -0.782f, -0.298f)
                        moveToRelative(-7.883f, 0.004f)
                        arcToRelative(5.7f, 5.7f, 0.0f, false, false, -2.147f, 0.407f)
                        arcTo(5.5f, 5.5f, 0.0f, false, false, 8.051f, 9.59f)
                        lineToRelative(-3.455f, 3.37f)
                        lineToRelative(-1.28f, 1.25f)
                        curveToRelative(-0.42f, 0.408f, -0.42f, 1.08f, 0.021f, 1.479f)
                        curveToRelative(0.43f, 0.399f, 1.137f, 0.398f, 1.557f, -0.021f)
                        lineToRelative(2.23f, -2.173f)
                        lineToRelative(1.545f, -1.5f)
                        lineToRelative(0.96f, -0.935f)
                        arcToRelative(3.3f, 3.3f, 0.0f, false, true, 1.083f, -0.71f)
                        arcToRelative(3.4f, 3.4f, 0.0f, false, true, 1.29f, -0.245f)
                        curveToRelative(0.807f, 0.0f, 1.546f, 0.273f, 2.154f, 0.777f)
                        lineToRelative(1.533f, -1.5f)
                        arcToRelative(5.6f, 5.6f, 0.0f, false, false, -3.686f, -1.375f)
                        close()
                        moveTo(3.948f, 8.011f)
                        curveToRelative(-0.23f, 0.032f, -0.444f, 0.133f, -0.61f, 0.29f)
                        arcToRelative(1.0f, 1.0f, 0.0f, false, false, -0.247f, 0.335f)
                        arcToRelative(0.99f, 0.99f, 0.0f, false, false, 0.225f, 1.144f)
                        lineToRelative(1.114f, 1.102f)
                        lineTo(5.965f, 9.37f)
                        lineToRelative(-1.07f, -1.048f)
                        curveToRelative(-0.12f, -0.12f, -0.269f, -0.211f, -0.434f, -0.265f)
                        arcToRelative(1.14f, 1.14f, 0.0f, false, false, -0.513f, -0.046f)
                        moveToRelative(15.639f, 5.117f)
                        lineToRelative(-1.534f, 1.511f)
                        lineToRelative(1.06f, 1.04f)
                        curveToRelative(0.419f, 0.42f, 1.113f, 0.43f, 1.555f, 0.02f)
                        quadToRelative(0.159f, -0.149f, 0.246f, -0.346f)
                        arcToRelative(1.02f, 1.02f, 0.0f, false, false, -0.223f, -1.144f)
                        close()
                    }
                }
                .build()
        return _nearbyShare!!
    }

private var _nearbyShare: ImageVector? = null
