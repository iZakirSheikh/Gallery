package com.zs.foundation

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.zs.foundation.toast.Duration
import com.zs.foundation.toast.Toast
import android.widget.Toast as AndroidWidgetToast

/**
 * Checks if a given permission is granted for the application in the current context.
 *
 * @param permission The permission string tocheck.
 * @return `true` if the permission is granted, `false` otherwise.
 */
fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * @see isPermissionGranted
 */
fun Context.checkSelfPermissions(values: List<String>) =
    values.all { isPermissionGranted(it) }

/**
 * Conditionally applies another [Modifier] if the given [condition] is true.
 *
 * @param condition The condition to evaluate.
 * @param other The [Modifier] to apply if the condition is true.
 * @return This [Modifier] if the condition is false, otherwise this [Modifier] combined with [other].
 */
inline fun Modifier.thenIf(condition: Boolean, other: Modifier): Modifier {
    return if (condition) this then other else this
}

/**
 * Another version of [thenIf]
 */
inline fun Modifier.thenIf(condition: Boolean, value: Modifier.() -> Modifier): Modifier {
    // FixMe - Keep an eye on this; here Modifier is required otherwise value is twice applied.
    return if (condition) this then Modifier.value() else this
}

/*@ExperimentalContracts
inline fun Modifier.thenIf(condition: Boolean, crossinline value: Modifier.() -> Modifier): Modifier {
    contract {
        callsInPlace(value, InvocationKind.EXACTLY_ONCE)
        returns() implies (condition)
    }
    return if (condition) this then Modifier.value() else this
}*/

/**
 * Shows a platform Toast message with the given text.
 *
 * This function uses the standard Android Toast class to display a short message to the user.
 *
 * @param message The text message to display in the Toast.
 * @param duration The duration of the Toast. Must be either [Toast.DURATION_SHORT] or [Toast.DURATION_LONG].
 */
fun Context.showPlatformToast(message: String, @Duration duration: Int = Toast.DURATION_SHORT) {
    // Ensure the duration is valid
    require(duration == Toast.DURATION_SHORT || duration == Toast.DURATION_LONG) {
        "Duration must be either Toast.DURATION_SHORT or Toast.DURATION_LONG"
    }
    // Create and show the Toast
    val toastDuration = if (duration == Toast.DURATION_SHORT) AndroidWidgetToast.LENGTH_SHORT else AndroidWidgetToast.LENGTH_LONG
    AndroidWidgetToast.makeText(this, message, toastDuration).show()
}

/**
 * @see showPlatformToast
 */
fun Context.showPlatformToast(
    @StringRes message: Int,
    @Duration duration: Int = Toast.DURATION_SHORT
) {
    require(duration == Toast.DURATION_SHORT || duration == Toast.DURATION_LONG) {
        "Duration must be either Toast.DURATION_SHORT or Toast.DURATION_LONG"
    }
    // Create and show the Toast
    val toastDuration = if (duration == Toast.DURATION_SHORT) AndroidWidgetToast.LENGTH_SHORT else AndroidWidgetToast.LENGTH_LONG
    AndroidWidgetToast.makeText(this, message, toastDuration).show()
}
