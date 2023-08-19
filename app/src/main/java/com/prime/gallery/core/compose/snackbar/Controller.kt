package com.prime.gallery.core.compose.snackbar

import androidx.compose.ui.graphics.Color

interface SnackbarController {

    /**
     * Shows or queues to be shown a [Snackbar] at the bottom of the [Scaffold] to which this state
     * is attached and suspends until the snackbar has disappeared.
     *
     * [SnackbarHostState] guarantees to show at most one snackbar at a time. If this function is
     * called while another snackbar is already visible, it will be suspended until this snackbar is
     * shown and subsequently addressed. If the caller is cancelled, the snackbar will be removed
     * from display and/or the queue to be displayed.
     * @see [SnackbarData2]
     */
    suspend fun showSnackbar(
        message: CharSequence,
        action: CharSequence? = null,
        leading: Any? = null,
        accent: Color = Color.Unspecified,
        duration: SnackDuration = if (action == null) SnackDuration.Short else SnackDuration.Indefinite
    ): SnackResult
}