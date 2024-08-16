package com.zs.domain.store

import androidx.exifinterface.media.ExifInterface
import java.text.SimpleDateFormat
import java.util.Locale

private val DATE_TIME_PATTERN
    get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


@JvmInline
value class Properties(private val data: ExifInterface) {
    val dateTime: Long
        get() = data.getAttribute(ExifInterface.TAG_DATETIME)
            ?.let { DATE_TIME_PATTERN.parse(it)?.time } ?: -1L
}