/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 04-05-2025.
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

package com.zs.gallery.common.icons


import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.zs.gallery.common.icons._NearbyShare


public val Icons.Filled.NearbyShare: ImageVector
    get() {
        if (_NearbyShare != null) {
            return _NearbyShare!!
        }
        _NearbyShare = ImageVector.Builder(
            name = "Nearby_Share",
            defaultWidth = 24.dp,
            defaultHeight = 11.22.dp,
            viewportWidth = 31.173876f,
            viewportHeight = 14.572845f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF1A73E8)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(29.244187f, 0.00600321f)
                curveToRelative(-0.490f, 0.00470f, -0.97680f, 0.19610f, -1.33980f, 0.57810f)
                lineTo(24.043015f, 4.5431132f)
                lineToRelative(-2.677734f, 2.732421f)
                lineToRelative(-1.662109f, 1.701172f)
                arcToRelative(5.65f, 5.65f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.111328f, 1.7402348f)
                arcToRelative(5.555f, 5.555f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.726563f, -1.4140628f)
                lineTo(9.2070781f, 12.0353f)
                curveToRelative(1.760f, 1.6060f, 3.99480f, 2.50590f, 6.38480f, 2.50590f)
                curveToRelative(2.60f, 00f, 5.02780f, -1.01270f, 6.84380f, -2.88670f)
                lineToRelative(1.607421f, -1.644532f)
                lineToRelative(2.675782f, -2.7343748f)
                lineToRelative(3.919921f, -4.013671f)
                arcToRelative(1.94f, 1.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.03906f, -2.71484399f)
                curveToRelative(-0.3730f, -0.3630f, -0.86550f, -0.54580f, -1.35550f, -0.5410f)
                close()
                moveToRelative(-13.652343f, 0.0059f)
                arcTo(9.437f, 9.437f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.7480941f, 2.8986212f)
                lineToRelative(-5.9843755f, 6.136719f)
                lineTo(0.54692168f, 11.312684f)
                curveToRelative(-0.7270f, 0.7450f, -0.72790f, 1.96930f, 0.03710f, 2.69530f)
                curveToRelative(0.7450f, 0.7270f, 1.96930f, 0.72590f, 2.69530f, -0.03910f)
                lineToRelative(3.8613289f, -3.957073f)
                lineToRelative(2.677734f, -2.7343758f)
                lineToRelative(1.6621089f, -1.701171f)
                arcToRelative(5.65f, 5.65f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.111329f, -1.740235f)
                curveToRelative(1.3960f, 00f, 2.67650f, 0.4980f, 3.72850f, 1.4160f)
                lineToRelative(2.65625f, -2.734375f)
                curveTo(20.21760f, 0.91170f, 17.98180f, 0.01380f, 15.59180f, 0.01380f)
                close()
                moveToRelative(-13.9492191f, 0.007772f)
                arcToRelative(1.887f, 1.887f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.05664072f, 0.527344f)
                arcToRelative(1.887f, 1.887f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.039063f, 2.69531199f)
                lineToRelative(1.92968802f, 2.007813f)
                lineToRelative(2.6582029f, -2.753907f)
                lineTo(3.2812967f, 0.58608121f)
                arcToRelative(1.887f, 1.887f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.6386718f, -0.566406f)
                close()
                moveTo(28.726609f, 9.3419412f)
                lineToRelative(-2.65625f, 2.7519528f)
                lineToRelative(1.833985f, 1.892578f)
                curveToRelative(0.7270f, 0.7650f, 1.92930f, 0.78510f, 2.69530f, 0.03910f)
                arcToRelative(1.94f, 1.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.574219f, -1.376953f)
                curveToRelative(00f, -0.4780f, -0.17220f, -0.97490f, -0.53520f, -1.33790f)
                close()
            }
        }.build()
        return _NearbyShare!!
    }

private var _NearbyShare: ImageVector? = null