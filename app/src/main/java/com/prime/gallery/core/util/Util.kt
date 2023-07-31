package com.prime.gallery.core.util

import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.format.DateUtils.*
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primex.core.runCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

private const val TAG = "Util"

context (ViewModel) @Suppress("NOTHING_TO_INLINE")
@Deprecated("find new solution.")
inline fun <T> Flow<T>.asComposeState(initial: T): State<T> {
    val state = mutableStateOf(initial)
    onEach { state.value = it }.launchIn(viewModelScope)
    return state
}

//language=RegExp
private val ISO6709LocationPattern = Pattern.compile("([+\\-][0-9.]+)([+\\-][0-9.]+)")

/**
 * This method parses the given string representing a geographic point location by coordinates in ISO 6709 format
 * and returns the latitude and the longitude in float. If `location` is not in ISO 6709 format,
 * this method returns `null`
 *
 * @param location a String representing a geographic point location by coordinates in ISO 6709 format
 * @return `null` if the given string is not as expected, an array of floats with size 2,
 * where the first element represents latitude and the second represents longitude, otherwise.
 */
val MediaMetadataRetriever.latLong: DoubleArray?
    get() = runCatching(TAG) {
        val location =
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION) ?: return@runCatching null
        val m: Matcher = ISO6709LocationPattern.matcher(location)
        if (m.find() && m.groupCount() == 2) {
            val latstr: String = m.group(1) ?: return@runCatching null
            val lonstr: String = m.group(2) ?: return@runCatching null
            val lat = latstr.toDouble()
            val lon = lonstr.toDouble()
            doubleArrayOf(lat, lon)
        } else null
    }

/**
 * An alternative to [ComponentActivity.registerForActivityResult] which allows to register from any class.
 *
 * This method registers a new callback with the activity result registry. When
 * calling this method, you must call `ActivityResultLauncher.unregister()` on the returned
 * `ActivityResultLauncher` when the launcher is no longer needed to release any values that might
 * be captured in the registered callback.
 *
 * @param contract The [ActivityResultContract] to use for the activity result.
 * @param callback The [ActivityResultCallback] to receive the result of the activity.
 * @return An [ActivityResultLauncher] that can be used to launch the activity and receive the result.
 *
 * @see androidx.activity.result.ActivityResultRegistry.register
 */
fun <I, O> ComponentActivity.registerActivityResultLauncher(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
): ActivityResultLauncher<I> {
    val key = UUID.randomUUID().toString()
    return activityResultRegistry.register(key, contract, callback)
}

/**
 * A compat property that removes any ID from the end of the this content [Uri].
 *
 * @return a new URI with the ID removed from the end of the path
 * @throws IllegalArgumentException when the given URI has no ID to remove
 * from the end of the path
 * Note: The [Uri] must be content uri.
 * @see [ContentUris.removeId]
 */
val Uri.removeId: Uri
    get() {
        val contentUri = this
        // Verify that we have a valid ID to actually remove
        val last = contentUri.lastPathSegment
        last?.toLong() ?: throw IllegalArgumentException("No path segments to remove")
        val segments = contentUri.pathSegments
        val builder = contentUri.buildUpon()
        builder.path(null)
        for (i in 0 until segments.size - 1) {
            builder.appendPath(segments[i])
        }
        return builder.build()
    }
