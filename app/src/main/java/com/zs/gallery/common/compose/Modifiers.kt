/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 3 of Jan 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 3 of Jan 2026
 *
 */

package com.zs.gallery.common.compose

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * A convenient [Modifier] for configuring the size and scale of a Lottie animation,
 * particularly for use as button icons.
 *
 * This modifier streamlines the creation of Lottie-based button icons by defaulting
 * to the standard Material Design icon size (24.dp) and allowing for easy scaling.
 */
inline fun Modifier.lottie(scale: Float = 1f) = requiredSize(24.dp).then(Modifier.scale(scale))