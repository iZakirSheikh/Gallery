/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
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
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

package com.zs.common.db.album

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zs.common.util.packFloats
import com.zs.common.util.packInts
import com.zs.common.util.unpackFloat1
import com.zs.common.util.unpackFloat2
import com.zs.common.util.unpackInt1
import com.zs.common.util.unpackInt2
import com.zs.common.db.album.MediaProvider as I

/**
 * Room entity representing a single media file stored on local storage.
 *
 * This table is optimized for read performance and storage efficiency.
 * Several logical attributes are **bit-packed into single `LONG` columns**
 * (e.g. extras, timeline, resolution, location) to reduce column count
 * while keeping frequently queried data indexed and cache-friendly.
 *
 * @property id Auto-generated primary key.
 * @property data Absolute file path; uniquely identifies the media file.
 * @property name Display name of the file.
 *
 * @property mimeType MIME type of the media (e.g. `image/jpeg`, `video/mp4`).
 * @property size File size in bytes.
 * @property bitrate Media bitrate (if applicable).
 * @property year Year associated with the media (captured or tagged).
 *
 * @property thumbnail Cached thumbnail path or key.
 * @property description Optional user or system description.
 *
 * @property dateAdded Timestamp (ms since epoch) when the file was added.
 * @property dateModified Timestamp (ms since epoch) when the file was last modified.
 * @property dateTaken Timestamp (ms since epoch) when the media was captured.
 * @property dateExpires Timestamp (ms since epoch) when the entry should expire.
 *
 * @property rawExtras Packed flags and small attributes stored in a single `LONG`
 * (e.g. privacy, archive state, orientation).
 * @property rawTimeline Packed temporal metadata used for grouping and sorting.
 * @property rawResolution Packed width/height information.
 * @property rawLocation Packed latitude/longitude coordinates.
 */
@Entity(tableName = "tbl_media", indices = [Index(value = ["source"], unique = true)])
class MediaFile(
    // ── Identity ────────────────────────────────────────────
    @PrimaryKey(autoGenerate = true)
    @JvmField val id: Long,
    @ColumnInfo("store_id") @JvmField val storeID: Long,

    // ── Source / naming ─────────────────────────────────────
    @JvmField val source: String, // absolute path to file in local storage
    @JvmField val name: String,

    // ── Intrinsic media attributes ──────────────────────────
    @JvmField val mimeType: String?,
    @JvmField val size: Long,
    @JvmField val bitrate: Int,
    @JvmField val year: Int,

    // ── User / cached data ──────────────────────────────────
    @JvmField val description: String?,

    // ── Timestamps - milliseconds since epoch ────────────────────
    @ColumnInfo(name = "date_added") @JvmField val dateAdded: Long,
    @ColumnInfo(name = "date_modified") @JvmField val dateModified: Long,
    @ColumnInfo(name = "date_taken") @JvmField val dateTaken: Long,
    @ColumnInfo(name = "date_expired") @JvmField val dateExpires: Long,

    // ── Packed -  ───────────────
    @ColumnInfo(name = "extras") @JvmField internal val rawExtras: Int, // embedded packed extra attributes (collapsed columns)
    @ColumnInfo(name = "timeline") @JvmField internal val rawTimeline: Long, // Temporal metadata
    @ColumnInfo(name = "resolution") @JvmField val rawResolution: Long, // width / height
    @ColumnInfo(name = "location") @JvmField internal val rawLocation: Long, // lat / lon
) {
    /**
     * Represents a geographical location with latitude and longitude.
     *
     * This is a value class that efficiently stores latitude and longitude
     * as a single `Long` value by packing two `Float` values.
     * It is annotated with `@JvmInline` for performance benefits, avoiding
     * object allocation overhead where possible.
     *
     * Use the primary constructor to create an instance from latitude and longitude values.
     *
     * @property packed The underlying packed `Long` representation of the location.
     * @property latitude The geographical latitude.
     * @property longitude The geographical longitude.
     *
     * @constructor Creates a `Location` instance from the given latitude and longitude.
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     */
    @JvmInline
    value class Location internal constructor(internal val packed: Long) {
        val latitude: Float get() = unpackFloat1(packed)
        val longitude: Float get() = unpackFloat2(packed)

        constructor(latitude: Float, longitude: Float) : this(packFloats(latitude, longitude))

        override fun toString(): String {
            return "Location(latitude=$latitude, longitude=$longitude)"
        }
    }

    /**
     * Represents the resolution (width and height) of a media item, such as a photo or video.
     *
     * This class is used to store dimension information for media stored in a database
     * or retrieved from a content provider. It provides a simple data structure for holding
     * the width and height as integer values.
     *
     * @property width The width of the media item in pixels.
     * @property height The height of the media item in pixels.
     */
    @JvmInline
    value class Resolution internal constructor(internal val packed: Long) {
        val width: Int get() = unpackInt1(packed)
        val height: Int get() = unpackInt2(packed)

       constructor(width: Int = Int.MIN_VALUE, height: Int = Int.MIN_VALUE) : this(
            packInts(
                width,
                height
            )
        )

        override fun toString(): String {
            return "Resolution(width=$width, height=$height)"
        }
    }

    /**
     * Represents a playback timeline, packing position and duration into a single `Long`.
     *
     * This value class efficiently stores playback progress for media like videos.
     *
     * @property position Current playback position in milliseconds.
     * @property duration Total duration of the media in milliseconds.
     * @property isSpecified True if the timeline has been set (i.e., not the default value).
     */
    @JvmInline
    value class Timeline internal constructor(internal val packed: Long) {
        val position: Int get() = unpackInt1(packed)
        val duration: Int get() = unpackInt2(packed)


        constructor(position: Int = Int.MIN_VALUE, duration: Int = Int.MIN_VALUE) :
                this(packInts(position, duration))

        fun copy(position: Int = this.position, duration: Int = this.duration) =
            Timeline(position, duration)

        override fun toString(): String {
            return "Timeline(position=$position, duration=$duration)"
        }
    }

    /**
     * Bit-packed extras stored in a single SQLite `LONG` column.
     *
     * Multiple logical columns (flags + orientation) are encoded into one
     * `Long` value for compact storage, indexing, and faster I/O.
     *
     * @property packed Raw SQLite value containing all encoded extras.
     * @property isPrivate Whether the row is marked as private.
     * @property isArchived Whether the row is archived.
     * @property isTrashed Whether the row is trashed.
     * @property isLiked Whether the row is liked/favorited.
     * @property orientation Orientation value extracted from masked bits.
     */
    @JvmInline
    value class Extras internal constructor(internal val packed: Int) {

        // Boolean flags decoded from packed bits
        val isPrivate get() = packed and I.FLAG_PRIVATE != 0
        val isArchived get() = packed and I.FLAG_ARCHIVED != 0
        val isTrashed get() = packed and I.FLAG_TRASHED != 0
        val isLiked get() = packed and I.FLAG_LIKED != 0

        // Integer field stored in a fixed bit range
        val orientation get() = ((packed and I.MASK_ORIENTATION) shr 4).toInt()

        /**
         * Creates a new [Extras] by updating selected fields.
         *
         * Acts like copying multiple SQLite columns while writing back
         * a single packed `LONG` value.
         */
        fun copy(
            isPrivate: Boolean = this.isPrivate,
            isArchived: Boolean = this.isArchived,
            isTrashed: Boolean = this.isTrashed,
            isLiked: Boolean = this.isLiked,
            orientation: Int = this.orientation
        ): Extras {
            // Start from default/cleared bit layout
            var value = I.DEFAULT_EXTRAS

            // Encode boolean flags
            if (isPrivate) value = value or I.FLAG_PRIVATE
            if (isArchived) value = value or I.FLAG_ARCHIVED
            if (isTrashed) value = value or I.FLAG_TRASHED
            if (isLiked) value = value or I.FLAG_LIKED

            // Encode orientation into its masked bit segment
            value = value or ((orientation shl 4) and I.MASK_ORIENTATION)

            return Extras(value)
        }

        override fun toString(): String {
            return "Extras(isPrivate=$isPrivate, isArchived=$isArchived, isTrashed=$isTrashed, isLiked=$isLiked, orientation=$orientation)"
        }
    }


    val extras get() = Extras(rawExtras)
    val location get() = Location(rawLocation)
    val resolution get() = Resolution(rawResolution)
    val timeline get() = Timeline(rawTimeline)

    internal constructor(
        id: Long = 0,
        storeID: Long,
        name: String,
        source: String,

        dateExpires: Long,
        dateAdded: Long,
        dateTaken: Long,
        dateModified: Long,

        mimeType: String? = null,
        size: Long = Long.MIN_VALUE,
        bitrate: Int = Int.MIN_VALUE,
        year: Int = Int.MIN_VALUE,

        description: String? = null,

        timeline: Timeline = Timeline(Int.MIN_VALUE, Int.MIN_VALUE),
        extras: Extras = Extras(I.DEFAULT_EXTRAS),
        location: Location = Location(Float.NaN, Float.NaN),
        resolution: Resolution = Resolution(Int.MIN_VALUE, Int.MIN_VALUE)
    ) : this(
        id = id,
        storeID =  storeID,
        source = source,
        name = name,
        mimeType = mimeType,
        size = size,
        bitrate = bitrate,
        year = year,
        description = description,
        dateAdded = dateAdded,
        dateModified = dateModified,
        dateTaken = dateTaken,
        dateExpires = dateExpires,
        rawExtras = extras.packed,
        rawTimeline = timeline.packed,
        rawResolution = resolution.packed,
        rawLocation = location.packed
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaFile

        if (id != other.id) return false
        if (storeID != other.storeID) return false
        if (size != other.size) return false
        if (bitrate != other.bitrate) return false
        if (year != other.year) return false
        if (dateAdded != other.dateAdded) return false
        if (dateModified != other.dateModified) return false
        if (dateTaken != other.dateTaken) return false
        if (dateExpires != other.dateExpires) return false
        if (rawExtras != other.rawExtras) return false
        if (rawTimeline != other.rawTimeline) return false
        if (rawResolution != other.rawResolution) return false
        if (rawLocation != other.rawLocation) return false
        if (source != other.source) return false
        if (name != other.name) return false
        if (mimeType != other.mimeType) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + storeID.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + bitrate
        result = 31 * result + year
        result = 31 * result + dateAdded.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + dateTaken.hashCode()
        result = 31 * result + dateExpires.hashCode()
        result = 31 * result + rawExtras
        result = 31 * result + rawTimeline.hashCode()
        result = 31 * result + rawResolution.hashCode()
        result = 31 * result + rawLocation.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaFile(id=$id, storeID=$storeID, source='$source', name='$name', mimeType=$mimeType, size=$size, bitrate=$bitrate, year=$year, description=$description, dateAdded=$dateAdded, dateModified=$dateModified, dateTaken=$dateTaken, dateExpires=$dateExpires, rawExtras=$rawExtras, rawTimeline=$rawTimeline, rawResolution=$rawResolution, rawLocation=$rawLocation, extras=$extras, location=$location, resolution=$resolution, timeline=$timeline)"
    }
}