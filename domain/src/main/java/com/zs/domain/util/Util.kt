package com.zs.domain.util

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import coil3.Uri

/**
 * Shows a platform Toast message with the given text.
 *
 * This function uses the standard Android Toast class to display a short message to the user.
 *
 * @param message The text message to display in the Toast.
 * @param priority The duration of the Toast.
 */
fun Context.showPlatformToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * @see showPlatformToast
 */
fun Context.showPlatformToast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


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


private val previewUri = Uri("gallery", "media", "preview").newBuilder()

/**
 * Constructs a custom [Uri] for media thumbnails using a specific internal scheme.
 *
 * The resulting URI follows the format: `gallery://media/preview?id={id}&mimetype={mimeType}`.
 * This is typically used to provide a consistent identifier for image loading libraries.
 *
 * @param id The unique identifier of the media item.
 * @param mimeType The optional MIME type of the media item to assist in decoding.
 * @return A [Uri] configured for the media preview.
 */
internal fun buildMediaThumbnailUri(id: String, mimeType: String? = null): Uri =
    previewUri.query("id=$id&mimetype=$mimeType").build()