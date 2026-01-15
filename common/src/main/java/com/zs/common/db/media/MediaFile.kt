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

package com.zs.common.db.media

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zs.common.util.packFloats
import com.zs.common.util.packInts
import com.zs.common.util.unpackFloat1
import com.zs.common.util.unpackFloat2
import com.zs.common.util.unpackInt1
import com.zs.common.util.unpackInt2
import com.zs.common.db.media.MediaProvider as MP

// This represents the base table in the app database, which points to the actual file.
// It is comprised of IDs and data.

// Identifiers:
// - id: The main identifier, always present.
// - store_id: Local storage ID. Set to -1 for files that are in special private folders or only
//             available remotely.
// - remote_id: (planned for future versions) A string with default value "".
//              Empty means the file is not on a remote source.
//              Non-empty means it points to a remote source (e.g., Google Photos, prefixed with "GP"
//              for Google Photos or another code for other services).
//              A photo can exist either in a single remote source, locally, or in a private local
//              folder.

// Deletion / Trashing:
// - For local files, deletion depends on Android version:
//   • Android > 10 → uses the system trash mechanism.
//   • Android < 10 → uses the in-app trash mechanism.
// - Trashed items are removed automatically after 30 days from the date of trashing.

// Data field:
// - Represents the absolute path of the file stored locally.
// - Primarily used to provide folder view.
// In the future, we may use a hash of metadata to resolve whether the file has been stored remotely
// once the user removes and re-installs the app.

/**
 * Room entity representing a single media file stored in local storage.
 *
 * Designed with a focus on **read performance** and **storage efficiency**:
 * - Frequently accessed attributes are indexed for fast lookups.
 * - Several logical properties are **bit-packed into `LONG` columns** to minimize
 *   schema width while remaining cache-friendly and easy to decode.
 *
 * @property id Auto-generated primary key for the entity.
 * @property data Absolute file path; uniquely identifies the media file on disk.
 * @property name Human-readable display name of the file.
 *
 * @property mimeType MIME type of the media (e.g. `image/jpeg`, `video/mp4`).
 * @property size File size in bytes.
 * @property bitrate Media bitrate in bits per second (if applicable). Use `-1` if not available.
 * @property year Year associated with the media (captured or tagged). Use `-1` if not available.
 *
 * @property description Optional description provided by the user or system.
 *
 * @property dateAdded Timestamp (ms since epoch) when the file was first indexed by the system.
 * @property dateModified Timestamp (ms since epoch) when the file was last modified on disk.
 * @property dateTaken Timestamp (ms since epoch) when the media was originally captured.
 * @property dateExpires Timestamp (ms since epoch) when the entry should be considered expired.
 *
 * ### Packed Columns
 * @property rawExtras Encoded flags and small attributes (privacy, archive state, orientation).
 * @property rawTimeline Encoded temporal metadata for grouping and chronological sorting.
 * @property rawResolution Encoded width/height resolution values.
 * @property rawLocation Encoded latitude/longitude coordinates for geotagging.
 *
 *
 * ⚠️ **Note:** All timestamps are stored as milliseconds since epoch (UTC).
 */
@Entity(tableName = "tbl_media")
class MediaFile(
    // ── Identity ────────────────────────────────────────────
    @PrimaryKey(autoGenerate = true) @JvmField val id: Long,
    @ColumnInfo("store_id", defaultValue = "-1") @JvmField val storeID: Long,

    // ── Source / naming ─────────────────────────────────────
    @JvmField val data: String, // absolute path to file in local storage
    @JvmField val name: String,

    // ── Intrinsic media attributes ──────────────────────────
    @ColumnInfo("mime_type") @JvmField val mimeType: String?,
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
     * @property packed The underlying packed `Long` representation of the location.
     * @property latitude The geographical latitude. `Float.NaN` when not available.
     * @property longitude The geographical longitude. `Float.NaN` when not available.
     */
    @JvmInline
    value class Location internal constructor(internal val packed: Long) {
        val latitude: Float get() = unpackFloat1(packed)
        val longitude: Float get() = unpackFloat2(packed)

        /**
         * Creates a `Location` instance from the given [latitude] and [longitude].
         */
        constructor(latitude: Float = Float.NaN, longitude: Float = Float.NaN) : this(packFloats(latitude, longitude))

        override fun toString(): String {
            return "Location(latitude=$latitude, longitude=$longitude)"
        }

        operator fun component1() = latitude
        operator fun component2() = longitude
    }

    /**
     * Represents the resolution (width and height) of a media item, such as a photo or video.
     *
     * @property width The width of the media item in pixels. Use `-1` when not available.
     * @property height The height of the media item in pixels. Use `-1` when not available.
     */
    @JvmInline
    value class Resolution internal constructor(internal val packed: Long) {
        val width: Int get() = unpackInt1(packed)
        val height: Int get() = unpackInt2(packed)

        /**
         * Creates a `Location` instance from the given [width] and [height].
         */
        constructor(width: Int = -1, height: Int = -1) :
                this(packInts(width, height))

        override fun toString(): String {
            return "Resolution(width=$width, height=$height)"
        }

        operator fun component1() = width
        operator fun component2() = height
    }

    /**
     * Represents a playback timeline, packing position and duration into a single `Long`.
     *
     * @property position Current playback position in milliseconds. `-1` if unavailable.
     * @property duration Total duration of the media in milliseconds. `-1` if unavailable.
     */
    @JvmInline
    value class Timeline internal constructor(internal val packed: Long) {
        val position: Int get() = unpackInt1(packed)
        val duration: Int get() = unpackInt2(packed)


        constructor(position: Int = -1, duration: Int = -1) :
                this(packInts(position, duration))

        fun copy(position: Int = this.position, duration: Int = this.duration) =
            Timeline(position, duration)

        operator fun component1() = position
        operator fun component2() = duration

        override fun toString(): String {
            return "Timeline(position=$position, duration=$duration)"
        }
    }

    /**
     * Represents a set of bit‑packed metadata stored in a single SQLite `LONG` column.
     *
     * Multiple logical attributes (flags and orientation) are encoded into one `Long`
     * for compact storage, efficient indexing, and faster I/O.
     *
     * @property packed The raw SQLite `Long` value containing all encoded attributes.
     * @property isPrivate Represents whether the row is marked as private.
     * @property isArchived Represents whether the row is archived.
     * @property isTrashed Represents whether the row is trashed or deleted.
     * @property isLiked Represents whether the row is liked or favorited.
     * @property orientation Represents the orientation value extracted from masked bits.
     */
    @JvmInline
    value class Extras internal constructor(internal val packed: Int) {

        // Boolean flags decoded from packed bits
        val isPrivate get() = packed and MP.FLAG_PRIVATE != 0
        val isArchived get() = packed and MP.FLAG_ARCHIVED != 0
        val isTrashed get() = packed and MP.FLAG_TRASHED != 0
        val isLiked get() = packed and MP.FLAG_LIKED != 0

        // Integer field stored in a fixed bit range
        val orientation get() = ((packed and MP.MASK_ORIENTATION) shr 4)

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
            var value = MP.DEFAULT_EXTRAS

            // Encode boolean flags
            if (isPrivate) value = value or MP.FLAG_PRIVATE
            if (isArchived) value = value or MP.FLAG_ARCHIVED
            if (isTrashed) value = value or MP.FLAG_TRASHED
            if (isLiked) value = value or MP.FLAG_LIKED

            // Encode orientation into its masked bit segment
            value = value or ((orientation shl 4) and MP.MASK_ORIENTATION)

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

    /**
     * Construct [MediaFile] from the given parameters.
     */
    internal constructor(
        id: Long = 0,
        storeID: Long,
        name: String,
        data: String,

        dateExpires: Long,
        dateAdded: Long,
        dateTaken: Long,
        dateModified: Long,

        mimeType: String? = null,
        size: Long = Long.MIN_VALUE,
        bitrate: Int = Int.MIN_VALUE,
        year: Int = Int.MIN_VALUE,

        description: String? = null,

        timeline: Timeline = Timeline(),
        extras: Extras = Extras(MP.DEFAULT_EXTRAS),
        location: Location = Location(),
        resolution: Resolution = Resolution()
    ) : this(
        id = id,
        storeID =  storeID,
        data = data,
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
        if (data != other.data) return false
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
        result = 31 * result + data.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaFile(dateExpires=$dateExpires, dateTaken=$dateTaken, dateModified=$dateModified, dateAdded=$dateAdded, description=$description, year=$year, bitrate=$bitrate, size=$size, mimeType=$mimeType, name='$name', data='$data', storeID=$storeID, id=$id, extras=$extras, location=$location, resolution=$resolution, timeline=$timeline)"
    }
}