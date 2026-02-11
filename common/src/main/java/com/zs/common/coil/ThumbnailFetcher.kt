/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 15 of Jan 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 15 of Jan 2026
 */

package com.zs.common.coil

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.applyCanvas
import coil3.Extras
import coil3.ImageLoader
import coil3.Uri
import coil3.asImage
import coil3.decode.DataSource
import coil3.decode.DecodeUtils
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.getExtra
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.request.bitmapConfig
import coil3.size.Precision
import coil3.size.Size
import coil3.size.pxOrElse
import coil3.toAndroidUri
import com.zs.common.db.media.MediaProvider
import kotlin.math.roundToInt

class ThumbnailFetcher(private val id: Long, private val options: Options) : Fetcher {

    private val TAG = "ThumbnailFetcher"

    /**
     * Check if the bitmap configuration is valid for the given options.
     * - On Android < 26, hardware configs are not an issue.
     * - On Android >= 26, only allow HARDWARE if explicitly requested.
     */
    private fun isConfigValid(bitmap: Bitmap, options: Options): Boolean {
        return Build.VERSION.SDK_INT < 26 || bitmap.config != Bitmap.Config.HARDWARE
                || options.bitmapConfig == Bitmap.Config.HARDWARE
    }

    /**
     * Check if the bitmap size matches the requested size.
     * - If precision is INEXACT, any size is acceptable.
     * - Otherwise, ensure the computed multiplier equals 1.0 (exact match).
     */
    private fun isSizeValid(bitmap: Bitmap, options: Options, size: Size): Boolean {
        if (options.precision == Precision.INEXACT) return true
        val multiplier = DecodeUtils.computeSizeMultiplier(
            srcWidth = bitmap.width,
            srcHeight = bitmap.height,
            dstWidth = size.width.pxOrElse { bitmap.width },
            dstHeight = size.height.pxOrElse { bitmap.height },
            scale = options.scale
        )
        return multiplier == 1.0
    }

    /**
     *  Normalize the input bitmap to ensure it matches the requested config and size.
     * - Fast path: return the original bitmap if already valid.
     * - Slow path: re-render the bitmap with correct scaling and config.
     */
    private fun normalize(inBitmap: Bitmap, size: Size): Bitmap {
        // Fast path: if the input bitmap is valid, return it.
        if (isConfigValid(inBitmap, options) && isSizeValid(inBitmap, options, size)) {
            return inBitmap
        }

        // Slow path: re-render the bitmap with the correct size + config.
        val scale = DecodeUtils.computeSizeMultiplier(
            srcWidth = inBitmap.width,
            srcHeight = inBitmap.height,
            dstWidth = size.width.pxOrElse { inBitmap.width },
            dstHeight = size.height.pxOrElse { inBitmap.height },
            scale = options.scale
        ).toFloat()
        val dstWidth = (scale * inBitmap.width).roundToInt()
        val dstHeight = (scale * inBitmap.height).roundToInt()
        val safeConfig = when {
            Build.VERSION.SDK_INT >= 26 && options.bitmapConfig == Bitmap.Config.HARDWARE -> Bitmap.Config.ARGB_8888
            else -> options.bitmapConfig
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val outBitmap = Bitmap.createBitmap(dstWidth, dstHeight, safeConfig)
        outBitmap.applyCanvas {
            scale(scale, scale)
            drawBitmap(inBitmap, 0f, 0f, paint)
        }
        inBitmap.recycle()

        return outBitmap
    }

    override suspend fun fetch(): FetchResult? {
        // Step 1: Resolve the media ID from the provider
        val provider = MediaProvider.getInstance()
        val mediaId = provider.getLocalStoreId(id)
        if (mediaId == -1L)
            return null   // If no valid ID found, exit early
        // Fetch the raw bitmap based on Android version
        val rawBitmap = let {
            val resolver = options.context.contentResolver
            val mimeType = options.mimeType
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val uri =  ContentUris.withAppendedId(
                        if (mimeType?.startsWith("video/") == true)
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        else
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )
                    // On Android Q and above, use loadThumbnail with a ThumbnailSize object
                    val size = options.size.let {
                        android.util.Size(it.width.pxOrElse { 256 },
                            it.height.pxOrElse { 256 })
                    }
                    resolver.loadThumbnail(uri, size, null)
                }
                else -> {
                    val size =
                        options.size.let { it.width.pxOrElse { 256 } to it.height.pxOrElse { 256 } }
                    val kind = when {
                        size.first <= 96 && size.second <= 96 -> MediaStore.Images.Thumbnails.MICRO_KIND
                        size.first <= 512 && size.second <= 384 -> MediaStore.Images.Thumbnails.MINI_KIND
                        else -> MediaStore.Images.Thumbnails.FULL_SCREEN_KIND
                    }
                    if (mimeType?.startsWith("video/") == true)
                        MediaStore.Video.Thumbnails.getThumbnail(resolver, id, kind, null)
                    else
                        MediaStore.Images.Thumbnails.getThumbnail(resolver, id, kind, null)
                }
            }
        }
        // Step 3: Validate that a bitmap was actually decoded
        checkNotNull(rawBitmap) { "Failed to decode thumbnail of size ${options.size}." }
        // Step 4: Extract dimensions and normalize bitmap to requested size/config
        val srcWidth = rawBitmap.width
        val srcHeight = rawBitmap.height
        val dstSize = options.size
        val bitmap = normalize(rawBitmap, dstSize)

        // Step 5: Determine if the image was sampled (scaled down vs original)
        val isSampled = when {
            srcWidth < 0 && srcHeight < 0 -> true // Unknown video size → assume sampled
            else -> DecodeUtils.computeSizeMultiplier(
                srcWidth = srcWidth,
                srcHeight = srcHeight,
                dstWidth = bitmap.width,
                dstHeight = bitmap.height,
                scale = options.scale
            ) < 1.0
        }

        // Step 6: Log debug info for developers
        Log.d(TAG, "fetch - DstSize: $dstSize | SrcSize: ${srcWidth}x$srcHeight | isSampled: $isSampled")

        // Step 7: Return the final ImageFetchResult with normalized bitmap
        return ImageFetchResult(
            image = bitmap.asImage(),
            isSampled = isSampled,
            dataSource = DataSource.DISK
        )
    }
}