/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 20 of Dec 2025
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
 * Last Modified by sheik on 20 of Dec 2025
 */

package com.zs.common.db.albums

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single media item (image or video) indexed from a local or remote source.
 *
 * This entity is:
 *  *  Path-centric (canonical source path is the logical identity)
 *  *  Room-managed with a Long primary key for performance
 *  *  Independent of MediaStore IDs
 *
 * For local media indexed via MediaStore, [data] is always an absolute file path.
 * For non-local sources, it may represent a canonical URI or remote identifier.
 *
 * Sentinel values are used instead of nulls where possible:
 * - duration, bitrate → -1 when not applicable
 * - latitude, longitude → -1f when unavailable
 *
 * Logical media state is stored as a bitmask in [flags] and exposed via
 * derived boolean properties.
 *
 * @property id Internal database identifier
 * @property name Display name of the media item.
 * @property data Canonical source path; absolute file path for local MediaStore items.
 * @property width Media width in pixels.
 * @property height Media height in pixels.
 * @property orientation EXIF orientation (0, 90, 180, 270).
 * @property size File size in bytes.
 * @property duration Duration in milliseconds, -1 for images.
 * @property mimeType MIME type (e.g. image/jpeg, video/mp4).
 * @property bitrate Bitrate in bits per second, -1 for images.
 * @property longitude Longitude in degrees, -1f if unavailable.
 * @property latitude Latitude in degrees, -1f if unavailable.
 * @property thumbnail Cached thumbnail path (e.g. 512×512).
 * @property dateAdded Timestamp when the media was added (epoch millis).
 * @property dateModified Timestamp when the media was last modified (epoch millis).
 * @property dateTaken Timestamp when the media was captured (epoch millis).
 * @property flags Bitmask describing logical media state: [Albums.FLAG_IS_FAVOURITE],
 *  [Albums.FLAG_IS_PENDING], [Albums.FLAG_IS_PRIVATE], [Albums.FLAG_IS_TRASHED]
 * @property isFavourite Whether this media item is marked as a favourite.
 * @property isPending Whether this media item is pending processing (e.g. indexing).
 * @property isPrivate Whether this media item is private and hidden from public listings.
 * @property isTrashed Whether this media item is marked as trashed.
 * @property year Indexed value of {@link android.media.MediaMetadataRetriever#METADATA_KEY_YEAR}
 * extracted from this media item.
 * @property description Optional user-provided description or caption.
 * @property bookmark Resume position for video playback in milliseconds, or -1 if unset.
 * @property dateExpires Timestamp (epoch millis) after which this media item should be considered
 * expired. This value is typically meaningful only when the media item is marked as [isPending]
 * or [isTrashed].
 *
 * The expiration timestamp is usually derived automatically when the pending or trashed state
 * changes (for example, pending items may expire after ~7 days and trashed items after ~30 days).
 *
 * Expired media items may be automatically deleted during a later cleanup
 * or idle maintenance pass.
 *
 */
@Entity(
    tableName = "tbl_media",
    indices = [Index(value = ["data"], unique = true), Index("date_taken")]
)
class MediaFile internal constructor(
    // ── Identity / source ───────────────────────────────────
    @PrimaryKey(autoGenerate = true) @JvmField val id: Long = 0,
    @JvmField val data: String,
    @JvmField val name: String,

    // ── Core media metadata ─────────────────────────────────
    @JvmField val mimeType: String? = null,
    @JvmField val size: Long,
    @JvmField val duration: Long = -1,
    @JvmField val bitrate: Int = -1,
    @JvmField val year: Int,

    // ── Dimensions / orientation ────────────────────────────
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val orientation: Int,

    // ── Location metadata ───────────────────────────────────
    @JvmField val latitude: Float = -1f,
    @JvmField val longitude: Float = -1f,

    // ── Cached / derived ────────────────────────────────────
    @JvmField val thumbnail: String? = null,

    // ── Timestamps ──────────────────────────────────────────
    @ColumnInfo(name = "date_added") @JvmField val dateAdded: Long,
    @ColumnInfo(name = "date_modified") @JvmField val dateModified: Long,
    @ColumnInfo(name = "date_taken") @JvmField val dateTaken: Long,
    @ColumnInfo(name = "date_expired") @JvmField val dateExpires: Long,

    // ── State / lifecycle ───────────────────────────────────
    internal val flags: Int, // Bitmask describing logical media state

    // ── User / playback metadata ────────────────────────────
    @JvmField val description: String? = null,
    @JvmField val bookmark: Long = -1L,
) {
    val isFavourite: Boolean get() = flags and Albums.FLAG_IS_FAVOURITE != 0
    val isPending: Boolean get() = flags and Albums.FLAG_IS_PENDING != 0
    val isPrivate: Boolean get() = flags and Albums.FLAG_IS_PRIVATE != 0
    val isTrashed: Boolean get() = flags and Albums.FLAG_IS_TRASHED != 0

    /**
     * Returns a new [MediaFile] with updated metadata while preserving identity fields.
     */
    internal fun update(
        name: String = this.name,
        width: Int = this.width,
        height: Int = this.height,
        orientation: Int = this.orientation,
        duration: Long = this.duration,
        mimeType: String? = this.mimeType,
        bitrate: Int = this.bitrate,
        longitude: Float = this.longitude,
        latitude: Float = this.latitude,
        thumbnail: String? = this.thumbnail,
        dateModified: Long = this.dateModified,
        dateTaken: Long = this.dateTaken,
        flags: Int=  this.flags,
        dateExpires: Long = this.dateExpires,
        year: Int = this.year
    ): MediaFile =
        MediaFile(
            id = id,
            name = name,
            data = data,
            width = width,
            height = height,
            orientation = orientation,
            size = size,
            duration = duration,
            mimeType = mimeType,
            bitrate = bitrate,
            longitude = longitude,
            latitude = latitude,
            thumbnail = thumbnail,
            dateAdded = dateAdded,
            dateModified = dateModified,
            dateTaken = dateTaken,
            flags = flags,
            year = year,
            dateExpires = dateExpires
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaFile

        if (id != other.id) return false
        if (size != other.size) return false
        if (duration != other.duration) return false
        if (bitrate != other.bitrate) return false
        if (year != other.year) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (orientation != other.orientation) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (dateAdded != other.dateAdded) return false
        if (dateModified != other.dateModified) return false
        if (dateTaken != other.dateTaken) return false
        if (dateExpires != other.dateExpires) return false
        if (flags != other.flags) return false
        if (bookmark != other.bookmark) return false
        if (data != other.data) return false
        if (name != other.name) return false
        if (mimeType != other.mimeType) return false
        if (thumbnail != other.thumbnail) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + bitrate
        result = 31 * result + year
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + orientation
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + dateAdded.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + dateTaken.hashCode()
        result = 31 * result + dateExpires.hashCode()
        result = 31 * result + flags
        result = 31 * result + bookmark.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaFile(id=$id, data='$data', name='$name', mimeType=$mimeType, size=$size, duration=$duration, bitrate=$bitrate, year=$year, width=$width, height=$height, orientation=$orientation, latitude=$latitude, longitude=$longitude, thumbnail=$thumbnail, dateAdded=$dateAdded, dateModified=$dateModified, dateTaken=$dateTaken, dateExpires=$dateExpires, flags=$flags, description=$description, bookmark=$bookmark, isFavourite=$isFavourite, isPending=$isPending, isPrivate=$isPrivate, isTrashed=$isTrashed)"
    }
}