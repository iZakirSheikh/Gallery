package com.prime.gallery.core.compose

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.core.content.res.ResourcesCompat
import com.primex.core.Text
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

private const val TAG = "Snackbar2"

/**
 * Possible results of the [SnackbarHostState.showSnackbar] call
 */
enum class SnackbarResult2 {
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
enum class SnackbarDuration2 {
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

// TODO: magic numbers adjustment
internal fun SnackbarDuration2.toMillis(
    hasAction: Boolean,
    accessibilityManager: AccessibilityManager?
): Long {
    val original = when (this) {
        SnackbarDuration2.Indefinite -> Long.MAX_VALUE
        SnackbarDuration2.Long -> 10000L
        SnackbarDuration2.Short -> 4000L
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
 * Interface to represent one particular [Snackbar] as a piece of the [SnackbarHostState]
 *
 * @property message text to be shown in the [SnackbarHostState]
 * @property actionLabel optional action label to show as button in the [Snackbar]
 * @property duration duration of the [Snackbar]
 * @property accent The accent color of this [Snackbar]. Default [Color.Unspecified]
 * @property leadingIcon optional leading icon for [Snackbar]. Default null. The leading must be a
 *           vector icon or resource drawbale.
 */
interface SnackbarData2 {
    val message: Text
    val actionLabel: Text?
    val duration: SnackbarDuration2

    // optional
    val accent: Color
    val leadingIcon: Any?
    val title: Text?

    /**
     * Function to be called when Snackbar action has been performed to notify the listeners.
     */
    fun performAction()

    /**
     * Function to be called when Snackbar is dismissed either by timeout or by the user.
     */
    fun dismiss()
}

// TODO: to be replaced with the public customizable implementation
// it's basically tweaked nullable version of Crossfade
@Composable
private fun FadeInFadeOutWithScale(
    current: SnackbarData2?,
    modifier: Modifier = Modifier,
    content: @Composable (SnackbarData2) -> Unit
) {
    val state = remember { FadeInFadeOutState<SnackbarData2?>() }
    if (current != state.current) {
        state.current = current
        val keys = state.items.map { it.key }.toMutableList()
        if (!keys.contains(current)) {
            keys.add(current)
        }
        state.items.clear()
        keys.filterNotNull().mapTo(state.items) { key ->
            FadeInFadeOutAnimationItem(key) { children ->
                val isVisible = key == current
                val duration = if (isVisible) SnackbarFadeInMillis else SnackbarFadeOutMillis
                val delay = SnackbarFadeOutMillis + SnackbarInBetweenDelayMillis
                val animationDelay = if (isVisible && keys.filterNotNull().size != 1) delay else 0
                val opacity = animatedOpacity(
                    animation = tween(
                        easing = LinearEasing,
                        delayMillis = animationDelay,
                        durationMillis = duration
                    ),
                    visible = isVisible,
                    onAnimationFinish = {
                        if (key != state.current) {
                            // leave only the current in the list
                            state.items.removeAll { it.key == key }
                            state.scope?.invalidate()
                        }
                    }
                )
                val scale = animatedScale(
                    animation = tween(
                        easing = FastOutSlowInEasing,
                        delayMillis = animationDelay,
                        durationMillis = duration
                    ),
                    visible = isVisible
                )
                Box(
                    Modifier
                        .graphicsLayer(
                            scaleX = scale.value,
                            scaleY = scale.value,
                            alpha = opacity.value
                        )
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            dismiss { key.dismiss(); true }
                        }
                ) {
                    children()
                }
            }
        }
    }
    Box(modifier) {
        state.scope = currentRecomposeScope
        state.items.forEach { (item, opacity) ->
            key(item) {
                opacity {
                    content(item!!)
                }
            }
        }
    }
}

private class FadeInFadeOutState<T> {
    // we use Any here as something which will not be equals to the real initial value
    var current: Any? = Any()
    var items = mutableListOf<FadeInFadeOutAnimationItem<T>>()
    var scope: RecomposeScope? = null
}

private data class FadeInFadeOutAnimationItem<T>(
    val key: T,
    val transition: FadeInFadeOutTransition
)

private typealias FadeInFadeOutTransition = @Composable (content: @Composable () -> Unit) -> Unit

@Composable
private fun animatedOpacity(
    animation: AnimationSpec<Float>,
    visible: Boolean,
    onAnimationFinish: () -> Unit = {}
): State<Float> {
    val alpha = remember { Animatable(if (!visible) 1f else 0f) }
    LaunchedEffect(visible) {
        alpha.animateTo(
            if (visible) 1f else 0f,
            animationSpec = animation
        )
        onAnimationFinish()
    }
    return alpha.asState()
}

@Composable
private fun animatedScale(animation: AnimationSpec<Float>, visible: Boolean): State<Float> {
    val scale = remember { Animatable(if (!visible) 1f else 0.8f) }
    LaunchedEffect(visible) {
        scale.animateTo(
            if (visible) 1f else 0.8f,
            animationSpec = animation
        )
    }
    return scale.asState()
}

private const val SnackbarFadeInMillis = 150
private const val SnackbarFadeOutMillis = 75
private const val SnackbarInBetweenDelayMillis = 0

private class SnackbarDataImpl(
    override val message: Text,
    override val actionLabel: Text?,
    override val duration: SnackbarDuration2,
    override val accent: Color,
    override val leadingIcon: Any?,
    override val title: Text?,
    private val continuation: CancellableContinuation<SnackbarResult2>
) : SnackbarData2 {


    override fun performAction() {
        if (continuation.isActive) continuation.resume(SnackbarResult2.ActionPerformed)
    }

    override fun dismiss() {
        if (continuation.isActive) continuation.resume(SnackbarResult2.Dismissed)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnackbarDataImpl

        if (message != other.message) return false
        if (actionLabel != other.actionLabel) return false
        if (duration != other.duration) return false
        if (accent != other.accent) return false
        if (leadingIcon != other.leadingIcon) return false
        if (title != other.title) return false
        return continuation == other.continuation
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (actionLabel?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + accent.hashCode()
        result = 31 * result + (leadingIcon?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
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
class SnackbarHostState2 {

    /**
     * Only one [Snackbar] can be shown at a time. Since a suspending Mutex is a fair queue, this
     * manages our message queue and we don't have to maintain one.
     */
    private val mutex = Mutex()

    /**
     * The current [SnackbarData] being shown by the [SnackbarHost], or `null` if none.
     */
    var currentSnackbarData by mutableStateOf<SnackbarData2?>(null)
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
    suspend fun showSnackbar(
        message: Text,
        title: Text? = null,
        action: Text? = null,
        leading: Any? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration2 = if (action == null) SnackbarDuration2.Short else SnackbarDuration2.Indefinite
    ): SnackbarResult2 = mutex.withLock {
        try {
            return suspendCancellableCoroutine { continuation ->
                currentSnackbarData = SnackbarDataImpl(
                    message,
                    action,
                    duration,
                    accent,
                    leading,
                    title,
                    continuation
                )
            }
        } finally {
            currentSnackbarData = null
        }
    }

    /**
     * @see [showSnackbar]
     */
    suspend fun showSnackbar(
        message: CharSequence,
        title: CharSequence? = null,
        action: CharSequence? = null,
        leading: Any? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration2 = if (action == null) SnackbarDuration2.Short else SnackbarDuration2.Indefinite
    ) = showSnackbar(
        Text(message),
        title?.let { Text(it) },
        action = action?.let { Text(it) },
        leading = leading,
        accent = accent,
        duration = duration
    )

    /**
     * Show a Toast Message with string resource.
     * ** Note: The html version isn't supported.
     */
    suspend fun showSnackbar(
        @StringRes message: Int,
        @StringRes title: Int = ResourcesCompat.ID_NULL,
        @StringRes action: Int = ResourcesCompat.ID_NULL,
        leading: Any? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration2 = if (action == ResourcesCompat.ID_NULL) SnackbarDuration2.Short else SnackbarDuration2.Indefinite
    ) = showSnackbar(
        title = if (title == ResourcesCompat.ID_NULL) null else Text(title),
        message = Text(message),
        action = if (action == ResourcesCompat.ID_NULL) null else Text(action),
        leading = leading,
        accent = accent,
        duration = duration
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
    snackbar: @Composable (SnackbarData2) -> Unit = { TODO("Not Implemented yet.") }
) {
    val currentSnackbarData = hostState.currentSnackbarData
    val accessibilityManager = LocalAccessibilityManager.current
    LaunchedEffect(currentSnackbarData) {
        if (currentSnackbarData != null) {
            val duration = currentSnackbarData.duration.toMillis(
                currentSnackbarData.actionLabel != null,
                accessibilityManager
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