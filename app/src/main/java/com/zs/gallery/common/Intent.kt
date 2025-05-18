/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 16-05-2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.gallery.common

import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import com.zs.core.store.MediaProvider

/**
 * Creates an Intent to set an image as wallpaper using the system's wallpaper cropper.
 *
 * @param uri The [Uri] of the image to set as wallpaper.
 * @return An [Intent] configured to launch the wallpaper setting activity.
 */
fun WallpaperIntent(uri: Uri): Intent =
    com.zs.core.Intent("android.service.wallpaper.CROP_AND_SET_WALLPAPER") {
        // Sets the data URI and MIME type for the intent. "image/*" indicates any image format.
        setDataAndType(uri, "image/*")
        // Explicitly sets the MIME type as an extra. Some systems might require this.
        putExtra("mimeType", "image/*")
        // Grants the receiving application temporary permission to read the URI.
        // This is crucial for content URIs that are not publicly accessible.
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // Adds the default category to the intent, which is standard for activities that can be launched.
        addCategory(Intent.CATEGORY_DEFAULT)
        // Ensures that this activity is not listed in the recent apps screen.
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }

/**
 * Creates a fallback Intent to set an image as wallpaper if the primary [WallpaperIntent] fails.
 * This intent uses the generic [Intent.ACTION_ATTACH_DATA] which is a more common way to
 * suggest data to be "attached" or used by another application, in this case, as a wallpaper.
 *
 * @param uri The [Uri] of the image to set as wallpaper.
 * @return An [Intent] configured to launch an activity that can handle attaching image data,
 * typically for setting it as wallpaper.
 */
fun FallbackWallpaperIntent(uri: Uri): Intent =
    com.zs.core.Intent(Intent.ACTION_ATTACH_DATA) {
        // Adds the default category to the intent, indicating it's a primary action.
        addCategory(Intent.CATEGORY_DEFAULT)
        // Grants the receiving application temporary permission to read the URI.
        // This is important for Android 7.0 (API level 24) and above when sharing content URIs.
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // Sets the data URI and MIME type for the intent.
        setDataAndType(uri, "image/*")
        // Explicitly sets the MIME type as an extra.
        putExtra("mimeType", "image/*")
    }


// Extracted from manifest of Google App
//            <activity android:theme="resourceId:0x7f160e0a" android:name="com.google.android.apps.search.lens.LensShareEntryPointActivity" android:exported="true" android:process=":search">
//            <intent-filter android:label="Search image">
//            <action android:name="android.intent.action.SEND" />
//            <category android:name="android.intent.category.DEFAULT" />
//            <data android:mimeType="image/jpeg" />
//            <data android:mimeType="image/png" />
//            </intent-filter>
//            </activity>

/**
 * Creates an Intent to share an image with Google Lens for visual search.
 *
 * This function constructs an intent that specifically targets Google Lens's
 * sharing entry point activity within the Google Quick Search Box application.
 *
 * @param uri The [Uri] of the image to be shared with Google Lens.
 * @return An [Intent] configured to launch Google Lens with the provided image.
 */
fun GoogleLensIntent(file: Uri) =
    com.zs.core.Intent(Intent.ACTION_SEND) {
        // Explicitly sets the component to target Google Lens's sharing activity.
        // This ensures that the intent is handled by Google Lens.
        component = ComponentName(
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.search.lens.LensShareEntryPointActivity"
        )
        // Sets the MIME type to "image/*", indicating that an image file is being shared.
        type = "image/*"
        // Puts the image URI into the intent as an extra stream.
        putExtra(Intent.EXTRA_STREAM, file)
        // Grants the receiving application (Google Lens) temporary permission to read the URI.
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // Ensures that this activity is not listed in the recent apps screen.
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }

// Extracted from manifest of QuickShare
// Keep an eye on it.
//    <activity android:theme="resourceId:0x7f160c16" android:label="Quick Share" android:icon="res/782.xml" android:name="com.google.android.gms.nearby.sharing.send.SendActivity" android:enabled="true" android:exported="true" android:process="com.google.android.gms.ui" android:taskAffinity="" android:documentLaunchMode="2" android:maxRecents="1" android:resizeableActivity="true">
//    <intent-filter>
//    <action android:name="android.intent.action.SEND"/>
//    <action android:name="android.intent.action.SEND_MULTIPLE"/>
//    <action android:name="com.google.android.gms.SHARE_NEARBY"/>
//    <category android:name="android.intent.category.DEFAULT"/>
//    <data android:mimeType="*/*"/>
//    </intent-filter>
//    <intent-filter>
//    <action android:name="com.google.android.gms.nearby.SEND_FOLDER"/>
//    <category android:name="android.intent.category.DEFAULT"/>
//    </intent-filter>
//    <meta-data android:name="android.service.chooser.chip_label" android:resource="Quick Share"/>
//    <meta-data android:name="android.service.chooser.chip_icon" android:resource="res/H2T.xml"/>
//    <meta-data android:name="android.service.chooser.chooser_target_service" android:value=".nearby.sharing.DirectShareService"/>
//    </activity>

/**
 * Creates an Intent to share files using Google's Nearby Share (Quick Share) feature.
 *
 * This function constructs an intent that targets the specific activity in Google Play Services
 * responsible for sending files via Nearby Share.
 *
 * @param uri Vararg parameter representing one or more [Uri]s of the files to be shared.
 * @return An [Intent] configured to initiate a Nearby Share operation with the specified files.
 */
fun NearByShareIntent(vararg uri: Uri) =
    com.zs.core.Intent("com.google.android.gms.SHARE_NEARBY") {
        component = ComponentName(
            "com.google.android.gms",
            "com.google.android.gms.nearby.sharing.send.SendActivity"
        )
        // Add the URIs as extras.
        putParcelableArrayListExtra(
            Intent.EXTRA_STREAM,
            uri.toMutableList() as ArrayList<Uri>
        )
        // Set the MIME type to allow sharing of various file types.
        type = "*/*"
        // Specify supported MIME types.
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }

fun NearByShareIntent(vararg id: Long) =
    NearByShareIntent(
        *id.map {
            ContentUris.withAppendedId(MediaProvider.EXTERNAL_CONTENT_URI, it)
        }.toTypedArray()
    )

/**
 * @see ShareFilesIntent
 */
fun ShareFilesIntent(vararg uri: Uri) = Intent.createChooser(
    com.zs.core.Intent(Intent.ACTION_SEND_MULTIPLE) {
        // Map selected IDs to content URIs.
        // TODO - Construct custom content uri.
        // Set the action to send multiple items.
        action = Intent.ACTION_SEND_MULTIPLE
        // Add the URIs as extras.
        putParcelableArrayListExtra(
            Intent.EXTRA_STREAM,
            uri.toMutableList() as ArrayList<Uri>
        )
        // Grant read permission to the receiving app.
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // Set the MIME type to allow sharing of various file types.
        type = "*/*"
        // Specify supported MIME types.
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    },
    "Share files..."
)

/**
 * Creates an Intent to share multiple files using the system's share dialog.
 *
 * This function constructs an intent that allows the user to share one or more files
 * identified by their content URIs. It leverages the system's `Intent.createChooser`
 * to display a list of apps capable of handling the sharing action.
 *
 * @param id Vararg parameter representing the IDs of the media items to be shared.
 *           These IDs are used to construct content URIs.
 * @return An [Intent] configured to share multiple files, wrapped in a chooser dialog.
 */
fun ShareFilesIntent(vararg id: Long) = ShareFilesIntent(*id.map {
    ContentUris.withAppendedId(MediaProvider.EXTERNAL_CONTENT_URI, it)
}.toTypedArray())

/**
 * Creates an Intent to edit an image at the given URI.
 *
 * @param uri The URI of the image to edit.
 * @return An Intent configured for image editing.
 */
fun EditInIntent(uri: Uri) =
    Intent(Intent.ACTION_EDIT).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grants temporary read permission to the editing app
    }