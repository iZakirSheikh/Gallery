package com.zs.compose_ktx

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat

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
fun Modifier.thenIf(condition: Boolean, other: Modifier): Modifier {
    return if (condition) this then other else this
}