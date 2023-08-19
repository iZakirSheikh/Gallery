package com.prime.gallery.core.compose.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * Possible results of the [SnackbarHostState.showSnackbar] call
 */
enum class SnackResult {
    /**
     * [Snackbar] that is shown has been dismissed either by timeout of by user
     */
    Dismissed,

    /**
     * Action on the [Snackbar] has been clicked before the time out passed
     */
    ActionPerformed,
}

/**
 * Possible durations of the [Snackbar] in [SnackbarHost]
 */
enum class SnackDuration {
    /**
     * Show the Snackbar for a short period of time
     */
    Short,

    /**
     * Show the Snackbar for a long period of time
     */
    Long,

    /**
     * Show the Snackbar indefinitely until explicitly dismissed or action is clicked
     */
    Indefinite
}

/**
 * Interface to represent one particular [Snackbar] as a piece of the [SnackbarHostState]
 *
 * @property message text to be shown in the [SnackbarHostState]
 * @property actionLabel optional action label to show as button in the [Snackbar]
 * @property duration duration of the [Snackbar]
 * @property accent The accent color of this [Snackbar]. Default [Color.Unspecified]
 * @property leadingIcon optional leading icon for [Snackbar]. Default null. The leading must be a
 *           vector icon or resource drawbale.
 */
interface Snack {
    val message: CharSequence
    val actionLabel: CharSequence?
    val duration: SnackDuration

    // optional
    val accent: Color
    val leadingIcon: Any?

    /**
     * Function to be called when Snackbar action has been performed to notify the listeners.
     */
    fun performAction()

    /**
     * Function to be called when Snackbar is dismissed either by timeout or by the user.
     */
    fun dismiss()
}

private class SnackImpl(
    override val message: CharSequence,
    override val actionLabel: CharSequence?,
    override val duration: SnackDuration,
    override val accent: Color,
    override val leadingIcon: Any?,
    private val continuation: CancellableContinuation<SnackResult>
) : Snack {
    override fun performAction() {
        if (continuation.isActive) continuation.resume(SnackResult.ActionPerformed)
    }

    override fun dismiss() {
        if (continuation.isActive) continuation.resume(SnackResult.Dismissed)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnackImpl

        if (message != other.message) return false
        if (actionLabel != other.actionLabel) return false
        if (duration != other.duration) return false
        if (accent != other.accent) return false
        if (leadingIcon != other.leadingIcon) return false
        return continuation == other.continuation
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (actionLabel?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + accent.hashCode()
        result = 31 * result + (leadingIcon?.hashCode() ?: 0)
        result = 31 * result + continuation.hashCode()
        return result
    }
}

/**
 * State of the [SnackbarHost], which controls the queue and the current [Snackbar] being shown
 * inside the [SnackbarHost].
 *
 * This state is usually [remember]ed and used to provide a [SnackbarHost] to a [Scaffold].
 */
@Stable
class SnackbarHostState2 : SnackbarController {

    /**
     * Only one [Snackbar] can be shown at a time. Since a suspending Mutex is a fair queue, this
     * manages our message queue and we don't have to maintain one.
     */
    private val mutex = Mutex()

    /**
     * The current [SnackbarData] being shown by the [SnackbarHost], or `null` if none.
     */
    var currentSnackbarData by mutableStateOf<Snack?>(null)
        private set

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
    override suspend fun showSnackbar(
        msg: CharSequence,
        action: CharSequence?,
        leading: Any?,
        accent: Color,
        duration: SnackDuration
    ): SnackResult = mutex.withLock {
        try {
            return suspendCancellableCoroutine { continuation ->
                currentSnackbarData =
                    SnackImpl(msg, action, duration, accent, leading, continuation)
            }
        } finally {
            currentSnackbarData = null
        }
    }
}

// TODO: magic numbers adjustment
private fun SnackDuration.toMillis(
    hasAction: Boolean,
    accessibilityManager: AccessibilityManager?
): Long {
    val original = when (this) {
        SnackDuration.Indefinite -> Long.MAX_VALUE
        SnackDuration.Long -> 10000L
        SnackDuration.Short -> 4000L
    }
    if (accessibilityManager == null) {
        return original
    }
    return accessibilityManager.calculateRecommendedTimeoutMillis(
        original,
        containsIcons = true,
        containsText = true,
        containsControls = hasAction
    )
}

/**
 * Host for [Snackbar]s to be used in [Scaffold] to properly show, hide and dismiss items based
 * on Material specification and the [hostState].
 *
 * This component with default parameters comes build-in with [Scaffold], if you need to show a
 * default [Snackbar], use [SnackbarHostState.showSnackbar].
 *
 * @sample androidx.compose.material3.samples.ScaffoldWithSimpleSnackbar
 *
 * If you want to customize appearance of the [Snackbar], you can pass your own version as a child
 * of the [SnackbarHost] to the [Scaffold]:
 *
 * @sample androidx.compose.material3.samples.ScaffoldWithCustomSnackbar
 *
 * @param hostState state of this component to read and show [Snackbar]s accordingly
 * @param modifier the [Modifier] to be applied to this component
 * @param snackbar the instance of the [Snackbar] to be shown at the appropriate time with
 * appearance based on the [SnackbarData] provided as a param
 */
@Composable
fun SnackbarHost2(
    hostState: SnackbarHostState2,
    modifier: Modifier = Modifier,
    snackbar: @Composable (Snack) -> Unit = { TODO("Not Implemented yet.") }
) {
    val currentSnackbarData = hostState.currentSnackbarData
    val manager = LocalAccessibilityManager.current
    LaunchedEffect(currentSnackbarData) {
        if (currentSnackbarData != null) {
            val duration = currentSnackbarData.duration.toMillis(
                currentSnackbarData.actionLabel != null,
                manager
            )
            delay(duration)
            currentSnackbarData.dismiss()
        }
    }
    FadeInFadeOutWithScale(
        current = hostState.currentSnackbarData,
        modifier = modifier,
        content = snackbar
    )
}


