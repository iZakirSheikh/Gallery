/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 11-07-2024.
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

package com.zs.compose_ktx.shapes

import androidx.compose.foundation.shape.GenericShape

private const val TAG = "Folder"

/**
 * Defines a [GenericShape] simpler to [Icons.Rounded.Folder]
 */
val FolderShape = GenericShape { (x, y), _ ->
    val radius = 0.18f * x
    val stepAt = 0.40f * x
    moveTo(radius, 0f)
    lineTo(stepAt, 0f)
    lineTo(stepAt + radius, radius)
    lineTo(x - radius, radius)
    quadraticTo(x, radius, x, 2 * radius)
    lineTo(x, y - radius)
    quadraticTo(x, y, x - radius, y)
    lineTo(radius, y)
    quadraticTo(0f, y, 0f, y - radius)
    lineTo(0f, radius)
    quadraticTo(0f, 0f, radius, 0f)
    close()
}