package com.zs.common.coil

import coil3.Extras
import coil3.getExtra
import coil3.request.ImageRequest
import coil3.request.Options

private val MEDIA_MEME_TYPE = Extras.Key<String?>(null)

/**
 * Sets the explicit MIME type for the image request.
 *
 * @param value The MIME type string, e.g., "image/jpeg", "image/png", etc. Can be null to clear it.
 * @return The [ImageRequest.Builder] instance for chaining.
 */
fun ImageRequest.Builder.mimeType(value: String?): ImageRequest.Builder  {
    extras[MEDIA_MEME_TYPE] = value
    return this
}

/**
 * Retrieves the explicit MIME type set for the image request.
 *
 * @see ImageRequest.Builder.mimeType
 */
internal val Options.mimeType: String?
    get() = getExtra(MEDIA_MEME_TYPE)
