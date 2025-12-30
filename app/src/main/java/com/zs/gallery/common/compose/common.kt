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
inline fun Modifier.lottie(scale: Float = 1f) =
    this
        .requiredSize(24.dp)
        .then(Modifier.scale(scale))