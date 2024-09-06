package com.zs.foundation

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Checks if a given permission is granted for the application in the current context.
 *
 * @param permission The permission string tocheck.
 * @return `true` if the permission is granted, `false` otherwise.
 */
fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED


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